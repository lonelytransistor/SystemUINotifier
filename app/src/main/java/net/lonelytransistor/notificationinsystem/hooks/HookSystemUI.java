package net.lonelytransistor.notificationinsystem.hooks;

import static net.lonelytransistor.notificationinsystem.Helpers.findAndHookMethod;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

import android.os.Build;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.view.View;

import net.lonelytransistor.notificationinsystem.hooks.reflected.InitReflections;
import net.lonelytransistor.notificationinsystem.hooks.reflected.StatusBarIconControllerImpl;

import java.io.InvalidObjectException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookSystemUI {
    private static final String TAG = "XPosedSystemUIHook";
    static final Map<String, StatusBarNotificationHolder> mNotifications = new HashMap<>();
    static final Map<String, WeakReference<View>> mNotificationHiddenViews = new HashMap<>();
    private static final Set<Integer> mIconUIDs = new HashSet<>();

    private static StatusBarNotification getSbn(Object view) {
        Object notificationEntry = XposedHelpers.getObjectField(
                view, "mEntry");
        StatusBarNotification sbn = (StatusBarNotification) XposedHelpers.getObjectField(
                notificationEntry, "mSbn");
        return sbn;
    }

    private static void isAboveShelf(View view) {
        StatusBarNotification sbn = getSbn(view);
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
            if (!mNotificationHiddenViews.containsKey(key))
                mNotificationHiddenViews.put(key, new WeakReference<>(view));
            view.setVisibility(View.GONE);
        }
    }

    private static boolean shouldShowNotificationIcon(StatusBarNotification sbn) throws InvalidObjectException {
        String key = sbn.getKey();
        if (!mNotifications.containsKey(key))
            return true;

        StatusBarNotificationHolder sbnh = mNotifications.get(key);
        if (sbnh.update(sbn))
            StatusBarIconControllerImpl.setIcon(sbnh.slot, sbnh.iconHolder);
        return false;
    }

    private static boolean onNotificationAdded(StatusBarNotification sbn) {
        PreferencesManager.NotificationFilter filter = PreferencesManager.getFilter(sbn);
        if (filter == null)
            return false;
        String key = sbn.getKey();

        int uid; for (uid=0; mIconUIDs.contains(uid); uid++);
        StatusBarNotificationHolder sbnh = new StatusBarNotificationHolder(
                sbn, uid, filter.slot,
                filter.width, filter.height, filter.hide);
        mNotifications.put(key, sbnh);

        mIconUIDs.add(uid);
        StatusBarIconControllerImpl.setIcon(sbnh.slot, sbnh.iconHolder);
        Log.i(TAG, "add: " + key + " " + sbnh.uid);
        return true;
    }
    private static void onNotificationRemoved(StatusBarNotification sbn) {
        String key = sbn.getKey();
        if (!mNotifications.containsKey(key))
            return;

        StatusBarNotificationHolder sbnh = mNotifications.get(key);
        mNotifications.remove(key);
        mNotificationHiddenViews.remove(key);
        mIconUIDs.remove(sbnh.uid);
        StatusBarIconControllerImpl.removeIcon(sbnh.slot, sbnh.iconHolder);
        Log.i(TAG, "remove: " + key + " " + sbnh.uid);
    }



    static void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        InitReflections.init(lpparam);
        Class<?> klass;

        klass = findClass("com.android.systemui.statusbar.notification.row.ExpandableNotificationRow", lpparam.classLoader);
        findAndHookMethod(klass, "isAboveShelf", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                boolean mustStayOnScreen = (boolean) callMethod(
                        param.thisObject, "mustStayOnScreen");

                if (mustStayOnScreen)
                    isAboveShelf((View) param.thisObject);
            }
        });
        findAndHookMethod(klass, Pattern.compile("get.*height", Pattern.CASE_INSENSITIVE), new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                boolean mustStayOnScreen = (boolean) callMethod(param.thisObject, "mustStayOnScreen");
                if (!mustStayOnScreen) {
                    StatusBarNotification sbn = getSbn(param.thisObject);
                    String key = sbn.getKey();

                    if (mNotificationHiddenViews.containsKey(key))
                        param.setResult(0);
                }
            }
        });

        klass = findClass("com.android.systemui.statusbar.phone.StatusBarIconController", lpparam.classLoader);
        findAndHookMethod(klass, Pattern.compile("IconManager"), "onIconAdded", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                String slot = (String) param.args[1];
                Object holder = param.args[3];
                int tag = (int) callMethod(holder, "getTag");
                if (StatusBarIconControllerImpl.getUid(StatusBarIconControllerImpl.getSlotIndex(slot), tag) >= 0) {
                    param.args[2] = false;
                }
            }
        });

        if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.TIRAMISU) {
            klass = findClass("com.android.systemui.ForegroundServiceNotificationListener", lpparam.classLoader);
            findAndHookMethod(klass, "removeNotification", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    onNotificationRemoved((StatusBarNotification) param.args[0]);
                }
            });
            klass = findClass("com.android.systemui.statusbar.phone.NotificationIconAreaController", lpparam.classLoader);
            findAndHookMethod(klass, "shouldShowNotificationIcon", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    StatusBarNotification sbn = (StatusBarNotification) XposedHelpers.getObjectField(
                            param.args[0], "mSbn");
                    if (!shouldShowNotificationIcon(sbn) || onNotificationAdded(sbn)) {
                        param.setResult(false);
                    }
                }
            });
        } else {



            // Thomas had never seen such bullshit before
            klass = findClass("com.android.systemui.ForegroundServiceNotificationListener", lpparam.classLoader);
            XposedBridge.hookAllConstructors(klass, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    List<Object> mNotifCollectionListeners =
                            (List<Object>) XposedHelpers.getObjectField(XposedHelpers.getObjectField(XposedHelpers.getObjectField(param.thisObject,
                                    "mNotifPipeline"),
                                    "mNotifCollection"),
                                    "mNotifCollectionListeners");
                    Class<?> interfaceClass = findClass(
                            "com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener", lpparam.classLoader);
                    Object proxyInstance = Proxy.newProxyInstance(
                            interfaceClass.getClassLoader(), new Class<?>[] { interfaceClass },
                            (proxy, method, args) -> {
                                if ("onEntryRemoved".equals(method.getName())) {
                                    StatusBarNotification sbn = (StatusBarNotification) XposedHelpers.getObjectField(
                                            args[0], "mSbn");
                                    onNotificationRemoved(sbn);
                                } else if ("onEntryAdded".equals(method.getName())) {
                                    StatusBarNotification sbn = (StatusBarNotification) XposedHelpers.getObjectField(
                                            args[0], "mSbn");
                                    onNotificationAdded(sbn);
                                }
                                return null;
                            });
                    mNotifCollectionListeners.add(proxyInstance);
                }
            });
            klass = findClass("com.android.systemui.statusbar.notification.collection.NotificationEntry", lpparam.classLoader);
            findAndHookMethod(klass, "shouldSuppressVisualEffect", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    StatusBarNotification sbn = (StatusBarNotification) XposedHelpers.getObjectField(
                            param.thisObject, "mSbn");
                    if (!shouldShowNotificationIcon(sbn))
                        param.setResult(true);
                }
            });
        }
    }
}
