package net.lonelytransistor.notificationinsystem.hooks;

import static net.lonelytransistor.notificationinsystem.Constants.DEBUG;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import net.lonelytransistor.commonlib.Preferences;
import net.lonelytransistor.commonlib.apkselect.Store;
import net.lonelytransistor.notificationinsystem.Constants;
import net.lonelytransistor.notificationinsystem.Helpers;
import net.lonelytransistor.notificationinsystem.hooks.reflected.NotificationIconAreaController;
import net.lonelytransistor.notificationinsystem.hooks.reflected.StatusBarIconControllerImpl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

public class PreferencesManager {
    private static final String TAG = "XPosedSystemUIHook";
    private static Map<String, Map<String,NotificationFilter>> mNotificationFilter = new HashMap<>();
    private static final ReentrantLock mutex = new ReentrantLock();

    public static class SettingsReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Preferences prefs;
            try {
                prefs = new Preferences(intent.getByteArrayExtra(Constants.APK_PREF_KEY));
            } catch (Exception e) {
                Log.e(TAG, "Invalid prefs", e);
                return;
            }
            Map<String, Store.Data> filterData = (Map<String, Store.Data>)
                    prefs.getMap(Constants.APK_FILTERS_KEY, new HashMap<>());
            Map<String, Map<String, NotificationFilter>> filter = new HashMap<>();
            for (String pkg : filterData.keySet()) {
                Map<String, NotificationFilter> catFilters = new HashMap<>();
                Store.Data data = filterData.get(pkg);
                for (String catName : data.categories.keySet()) {
                    Map<String, Serializable> catData = data.categories.get(catName);
                    catFilters.put(catName, new NotificationFilter(
                            catName,
                            (String) catData.getOrDefault(Constants.APK_REGEX_SUFFIX, ""),
                            (int) catData.getOrDefault(Constants.APK_SLOT_SUFFIX, 0),
                            (int) catData.getOrDefault(Constants.APK_WIDTH_SUFFIX, 32),
                            (int) catData.getOrDefault(Constants.APK_HEIGHT_SUFFIX, 32),
                            (boolean) catData.getOrDefault(Constants.APK_HIDE_PANEL_SUFFIX, false)
                    ));
                }
                filter.put(pkg, catFilters);
            }
            setNotificationFilters(filter);
        }
    }

    public static class NotificationFilter {
        private final String channel;
        private final Pattern regex;
        final int width;
        final int height;
        final int slot;
        final boolean hide;
        NotificationFilter(String channel, String regex, int slot, int width, int height, boolean hide) {
            this.channel = channel;
            this.regex = Pattern.compile(regex);
            this.slot = slot;
            this.width = width;
            this.height = height;
            this.hide = hide;
        }
        @NonNull @Override
        public String toString() {
            return "NotificationFilter{" +
                    "channel=" + channel +
                    ", regex=" + regex +
                    ", width=" + width +
                    ", height=" + height +
                    ", slot=" + slot +
                    '}';
        }
    }
    static boolean hasNotificationFilter(String pkg) {
        mutex.lock();
        boolean ret = mNotificationFilter.containsKey(pkg);
        mutex.unlock();
        return ret;
    }
    static NotificationFilter getNotificationFilter(String pkg, String channelId) {
        mutex.lock();
        NotificationFilter ret = mNotificationFilter.get(pkg).get(channelId);
        mutex.unlock();
        return ret;
    }
    static void setNotificationFilters(Map<String, Map<String, NotificationFilter>> filters) {
        mutex.lock();
        mNotificationFilter = filters;
        DEBUG("New notification filters: " + filters);
        mutex.unlock();

        StatusBarIconControllerImpl.removeAllOwnIcons();
        for (StatusBarNotificationHolder sbnh : HookSystemUI.mNotifications.values()) {
            NotificationFilter filter = PreferencesManager.getFilter(
                    sbnh.pkgName, sbnh.title, sbnh.desc, sbnh.category);
            if (filter == null && HookSystemUI.mNotificationHiddenViews.containsKey(sbnh.key)) {
                View view = HookSystemUI.mNotificationHiddenViews.get(sbnh.key).get();
                HookSystemUI.mNotificationHiddenViews.remove(sbnh.key);
                if (view != null) {
                    view.setVisibility(View.VISIBLE);
                }
            }
        }
        HookSystemUI.mNotifications.clear();
        NotificationIconAreaController.updateNotificationIcons();
    }
    static NotificationFilter getFilter(String pkg, String title, String desc, String channelId) {
        if (!hasNotificationFilter(pkg))
            return null;
        NotificationFilter filter = getNotificationFilter(pkg, channelId);
        if (filter == null)
            return null;

        if (!filter.regex.matcher(title).find() &&
                !filter.regex.matcher(desc).find()) {
            return null;
        }
        return filter;
    }
    static NotificationFilter getFilter(StatusBarNotification sbn) {
        Notification notif = sbn.getNotification();
        String title = Helpers.getExtraString(notif.extras, Notification.EXTRA_TITLE);
        String desc = Helpers.getExtraString(notif.extras, Notification.EXTRA_TEXT);

        return getFilter(sbn.getPackageName(),
                title, desc, notif.getChannelId());
    }
}
