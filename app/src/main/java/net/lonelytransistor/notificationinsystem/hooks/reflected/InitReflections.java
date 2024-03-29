package net.lonelytransistor.notificationinsystem.hooks.reflected;

import static net.lonelytransistor.notificationinsystem.Constants.DEBUG;
import static net.lonelytransistor.notificationinsystem.Helpers.findAndHookMethod;

import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedHelpers.findClass;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.Parcel;
import android.os.UserHandle;
import android.util.Log;

import net.lonelytransistor.notificationinsystem.BuildConfig;
import net.lonelytransistor.notificationinsystem.Constants;
import net.lonelytransistor.notificationinsystem.Helpers;
import net.lonelytransistor.notificationinsystem.hooks.PreferencesManager;
import net.lonelytransistor.notificationinsystem.ui.SettingsBroadcastReceiver;

import java.util.regex.Pattern;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class InitReflections {
    private static final String TAG = "InitReflections";
    private static final PreferencesManager.SettingsReceiver receiver = new PreferencesManager.SettingsReceiver();
    public static void init(XC_LoadPackage.LoadPackageParam lpparam) {
        DEBUG("Initializing reflections.");

        Parcel parcel = Parcel.obtain();
        parcel.writeInt(0);
        StatusBarIcon.UserHandle_SYSTEM = new UserHandle(parcel);

        Class<?> klass = findClass(
                "com.android.internal.statusbar.StatusBarIcon", lpparam.classLoader);
        StatusBarIcon.StatusBarIcon_class_constructor_OSOIIS = XposedHelpers.findConstructorBestMatch(
                klass,
                UserHandle.class, String.class, Icon.class, Integer.class, Integer.class, CharSequence.class);

        klass = findClass(
                "com.android.systemui.statusbar.phone.StatusBarIconHolder", lpparam.classLoader);
        StatusBarIconHolder.StatusBarIconHolder_constructor = XposedHelpers.findConstructorBestMatch(klass);

        klass = findClass(
                "com.android.systemui.statusbar.phone.NotificationIconAreaController", lpparam.classLoader);
        hookAllConstructors(klass, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                DEBUG("NotificationIconAreaController created.");
                NotificationIconAreaController.init(param.thisObject);
                DEBUG("NotificationIconAreaController initialized.");
            }
        });

        klass = findClass(
                "com.android.systemui.statusbar.phone.StatusBarIconControllerImpl", lpparam.classLoader);
        findAndHookMethod(klass, Pattern.compile("removeAllIconsForSlot", Pattern.CASE_INSENSITIVE), new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                DEBUG("removeAllIconsForSlot intercepted");
                StatusBarIconControllerImpl.refreshIcons((String) param.args[0]);
                DEBUG("removeAllIconsForSlot exit");
            }
        });
        hookAllConstructors(klass, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                DEBUG("StatusBarIconControllerImpl created.");
                StatusBarIconControllerImpl.init(param.thisObject);

                Context ctx = StatusBarIconControllerImpl.getContext();
                Intent intent = new Intent(ctx, SettingsBroadcastReceiver.class);
                ctx.startService(intent);
                Helpers.registerReceiver(ctx, Constants.BROADCAST_SETTINGS_CHANGED, receiver);
                intent = new Intent(Constants.BROADCAST_SETTINGS_REQUEST);
                intent.setPackage(BuildConfig.APPLICATION_ID);
                ctx.sendBroadcast(intent);
                DEBUG("StatusBarIconControllerImpl initialized.");
            }
        });
    }
}
