package net.lonelytransistor.notificationinsystem.hooks;

import static net.lonelytransistor.notificationinsystem.hooks.XPosedHook.findAndHookMethod;

import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.view.View;

import net.lonelytransistor.notificationinsystem.hooks.reflected.InitReflections;
import net.lonelytransistor.notificationinsystem.hooks.reflected.StatusBarIconControllerImpl;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookSystemUI {
    private static final String TAG = "XPosedSystemUIHook";
    static final Map<String, StatusBarNotificationHolder> mNotifications = new HashMap<>();
    static final Map<String, WeakReference<View>> mNotificationHiddenViews = new HashMap<>();
    private static final Set<Integer> mIconUIDs = new HashSet<>();

    static void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        InitReflections.init(lpparam);
        Class<?> klass;

        Class<?> klass0 = XposedHelpers.findClass("com.android.systemui.statusbar.notification.row.ExpandableNotificationRow", lpparam.classLoader);
        findAndHookMethod(klass0, new String[] {"isAboveShelf"}, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                boolean mustStayOnScreen = (boolean) XposedHelpers.callMethod(
                        param.thisObject, "mustStayOnScreen");
                if (!mustStayOnScreen) {
                    Object notificationEntry = XposedHelpers.callMethod(param.thisObject, "getEntry");
                    StatusBarNotification sbn = (StatusBarNotification) XposedHelpers.getObjectField(
                            notificationEntry, "mSbn");
                    String key = sbn.getKey();
                    boolean hide;

                    if (mNotifications.containsKey(key)) {
                        StatusBarNotificationHolder sbnh = mNotifications.get(key);
                        hide = sbnh.hide;
                    } else {
                        PreferencesManager.NotificationFilter filter = PreferencesManager.getFilter(sbn);
                        hide = filter != null;
                    }
                    if (hide) {
                        if (!mNotificationHiddenViews.containsKey(key)) {
                            mNotificationHiddenViews.put(key, new WeakReference<>((View) param.thisObject));
                        }
                        ((View) param.thisObject).setVisibility(View.GONE);
                    }
                }
            }
        });

        klass = XposedHelpers.findClass(
                "com.android.systemui.statusbar.notification.NotificationEntryManager", lpparam.classLoader);
        findAndHookMethod(klass, "removeNotification", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                String key = (String) param.args[0];
                if (mNotifications.containsKey(key)) {
                    StatusBarNotificationHolder sbnh = mNotifications.get(key);
                    mNotifications.remove(key);
                    mNotificationHiddenViews.remove(key);
                    mIconUIDs.remove(sbnh.uid);
                    StatusBarIconControllerImpl.removeIcon(sbnh.slot, sbnh.iconHolder);
                    Log.i(TAG, "remove: " + key + " " + sbnh.uid);
                }
            }
        });

        klass = XposedHelpers.findClass(
                "com.android.systemui.statusbar.phone.NotificationIconAreaController", lpparam.classLoader);
        findAndHookMethod(klass, "shouldShowNotificationIcon", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                StatusBarNotification sbn = (StatusBarNotification) XposedHelpers.getObjectField(
                        param.args[0], "mSbn");
                String key = sbn.getKey();

                if (mNotifications.containsKey(key)) {
                    StatusBarNotificationHolder sbnh = mNotifications.get(key);

                    sbnh.update(sbn);
                    StatusBarIconControllerImpl.setIcon(sbnh.slot, sbnh.iconHolder);
                    param.setResult(false);
                } else {
                    PreferencesManager.NotificationFilter filter = PreferencesManager.getFilter(sbn);
                    if (filter != null) {
                        int uid; for (uid=0; mIconUIDs.contains(uid); uid++);
                        StatusBarNotificationHolder sbnh = new StatusBarNotificationHolder(
                                sbn, uid, filter.slot,
                                filter.width, filter.height, filter.hide);
                        mNotifications.put(key, sbnh);

                        mIconUIDs.add(uid);
                        StatusBarIconControllerImpl.setIcon(sbnh.slot, sbnh.iconHolder);
                        Log.i(TAG, "add: " + key + " " + sbnh.uid);
                        param.setResult(false);
                    }
                }
            }
        });

        Class<?>[] klasses = {
                XposedHelpers.findClass(
                        "com.android.systemui.statusbar.phone.StatusBarIconController.IconManager", lpparam.classLoader),
                XposedHelpers.findClass(
                        "com.android.systemui.statusbar.phone.StatusBarIconController.TintedIconManager", lpparam.classLoader),
                XposedHelpers.findClass(
                        "com.android.systemui.statusbar.phone.StatusBarIconController.DarkIconManager", lpparam.classLoader)
        };
        findAndHookMethod(klasses, "onIconAdded", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                String slot = (String) param.args[1];
                Object holder = param.args[3];
                int tag = (int) XposedHelpers.callMethod(holder, "getTag");
                if (StatusBarIconControllerImpl.getUid(
                        StatusBarIconControllerImpl.getSlotIndex(slot),
                        tag) >= 0
                ) {
                    param.args[2] = false;
                }
            }
        });

    }
}
