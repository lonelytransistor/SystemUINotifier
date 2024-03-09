package net.lonelytransistor.notificationinsystem.hooks.reflected;

import static net.lonelytransistor.notificationinsystem.hooks.XPosedHook.findAndHookMethod;

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
        Parcel parcel = Parcel.obtain();
        parcel.writeInt(0);
        StatusBarIcon.UserHandle_SYSTEM = new UserHandle(parcel);

        StatusBarIconHolder.StatusBarIconHolder_class = XposedHelpers.findClass(
                "com.android.systemui.statusbar.phone.StatusBarIconHolder", lpparam.classLoader);

        Class<?> klass = XposedHelpers.findClass(
                "com.android.internal.statusbar.StatusBarIcon", lpparam.classLoader);
        StatusBarIcon.StatusBarIcon_class_constructor_OSOIIS = XposedHelpers.findConstructorBestMatch(
                klass,
                UserHandle.class, String.class, Icon.class, Integer.class, Integer.class, CharSequence.class);

        klass = XposedHelpers.findClass(
                "com.android.systemui.statusbar.phone.NotificationIconAreaController", lpparam.classLoader);
        XposedBridge.hookAllConstructors(klass, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                NotificationIconAreaController.init(param.thisObject);
            }
        });

        klass = XposedHelpers.findClass(
                "com.android.systemui.statusbar.phone.StatusBarIconControllerImpl", lpparam.classLoader);
        XposedBridge.hookAllConstructors(klass, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                StatusBarIconControllerImpl.init(param.thisObject);

                StatusBarIconControllerImpl.getContext().startService(
                        new Intent(
                                StatusBarIconControllerImpl.getContext(),
                                SettingsBroadcastReceiver.class));
                Helpers.registerReceiver(
                        StatusBarIconControllerImpl.getContext(),
                        Constants.BROADCAST_SETTINGS_CHANGED,
                        receiver);
                Intent intent = new Intent(Constants.BROADCAST_SETTINGS_REQUEST);
                intent.setPackage(BuildConfig.APPLICATION_ID);
                StatusBarIconControllerImpl.getContext().sendBroadcast(intent);
                }
        });
        findAndHookMethod(klass, Pattern.compile("removeAllIconsForSlot", Pattern.CASE_INSENSITIVE), new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                StatusBarIconControllerImpl.refreshIcons((String) param.args[0]);
            }
        });
    }
}
