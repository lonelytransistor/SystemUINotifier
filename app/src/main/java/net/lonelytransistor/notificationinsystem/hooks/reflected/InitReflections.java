package net.lonelytransistor.notificationinsystem.hooks.reflected;

import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.Parcel;
import android.os.UserHandle;

import net.lonelytransistor.notificationinsystem.BuildConfig;
import net.lonelytransistor.notificationinsystem.Constants;
import net.lonelytransistor.notificationinsystem.hooks.PreferencesManager;

import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class InitReflections {
    public static void init(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            Parcel parcel = Parcel.obtain();
            parcel.writeInt(0);
            StatusBarIcon.UserHandle_SYSTEM = new UserHandle(parcel);

            StatusBarIconHolder.StatusBarIconHolder_class = XposedHelpers.findClass(
                    "com.android.systemui.statusbar.phone.StatusBarIconHolder", lpparam.classLoader);
            StatusBarIconHolder.StatusBarIconHolder_field_tag =
                    StatusBarIconHolder.StatusBarIconHolder_class.getDeclaredField("mTag");
            StatusBarIconHolder.StatusBarIconHolder_field_tag.setAccessible(true);

            StatusBarIcon.StatusBarIcon_class = XposedHelpers.findClass(
                    "com.android.internal.statusbar.StatusBarIcon", lpparam.classLoader);
            StatusBarIcon.StatusBarIcon_class_constructor_OSOIIS = XposedHelpers.findConstructorBestMatch(
                    StatusBarIcon.StatusBarIcon_class,
                    UserHandle.class, String.class, Icon.class, Integer.class, Integer.class, CharSequence.class);
            StatusBarIcon.StatusBarIcon_icon =
                    StatusBarIcon.StatusBarIcon_class.getDeclaredField("icon");
            StatusBarIcon.StatusBarIcon_icon.setAccessible(true);
            StatusBarIcon.StatusBarIcon_pkg =
                    StatusBarIcon.StatusBarIcon_class.getDeclaredField("pkg");
            StatusBarIcon.StatusBarIcon_pkg.setAccessible(true);

            Class<?> klass = XposedHelpers.findClass(
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

                        Constants.registerReceiver(
                                StatusBarIconControllerImpl.getContext(),
                                Constants.BROADCAST_SETTINGS_CHANGED,
                                new PreferencesManager.SettingsReceiver());
                        Intent intent = new Intent(Constants.BROADCAST_SETTINGS_REQUEST);
                        intent.setPackage(BuildConfig.APPLICATION_ID);
                        StatusBarIconControllerImpl.getContext().sendBroadcast(intent);
                    }
            });
            Method mth = klass.getDeclaredMethod("removeAllIconsForSlot", String.class);
            XposedBridge.hookMethod(mth, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    String slot = (String) param.args[0];
                    StatusBarIconControllerImpl.refreshIcons(slot);
                }
            });
        } catch (NoSuchMethodException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}
