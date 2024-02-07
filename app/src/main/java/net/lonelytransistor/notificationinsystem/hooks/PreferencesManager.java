package net.lonelytransistor.notificationinsystem.hooks;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import net.lonelytransistor.commonlib.Preferences;
import net.lonelytransistor.notificationinsystem.Constants;
import net.lonelytransistor.notificationinsystem.hooks.reflected.NotificationIconAreaController;
import net.lonelytransistor.notificationinsystem.hooks.reflected.StatusBarIconControllerImpl;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

public class PreferencesManager {
    private static final String TAG = "XPosedSystemUIHook";
    private static Map<String, NotificationFilter> mNotificationFilter = new HashMap<>();
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
            Set<String> pkgs = prefs.getStringSet(Constants.APK_PKGS_SUFFIX);
            Map<String, NotificationFilter> filter = new HashMap<>();
            for (String pkg : pkgs) {
                filter.put(pkg, new NotificationFilter(
                        prefs.getStringSet(pkg + Constants.APK_CHANNELS_SUFFIX),
                        prefs.getString(pkg + Constants.APK_REGEX_SUFFIX),
                        prefs.getInt(pkg + Constants.APK_SLOT_SUFFIX),
                        prefs.getInt(pkg + Constants.APK_WIDTH_SUFFIX),
                        prefs.getInt(pkg + Constants.APK_HEIGHT_SUFFIX),
                        prefs.getBoolean(pkg + Constants.APK_HIDE_PANEL_SUFFIX)
                ));
            }
            setNotificationFilters(filter);
        }
    }

    public static class NotificationFilter {
        private final Set<String> channels;
        private final Pattern regex;
        final int width;
        final int height;
        final int slot;
        final boolean hide;
        NotificationFilter(Set<String> channels, String regex, int slot, int width, int height, boolean hide) {
            this.channels = channels;
            this.regex = Pattern.compile(regex);
            this.slot = slot;
            this.width = width;
            this.height = height;
            this.hide = hide;
        }
        @NonNull @Override
        public String toString() {
            return "NotificationFilter{" +
                    "channels=" + channels +
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
    static NotificationFilter getNotificationFilter(String pkg) {
        mutex.lock();
        NotificationFilter ret = mNotificationFilter.get(pkg);
        mutex.unlock();
        return ret;
    }
    static void setNotificationFilters(Map<String, NotificationFilter> filters) {
        mutex.lock();
        mNotificationFilter = filters;
        Log.i(TAG, "Set filters: " + filters);
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
        NotificationFilter filter = getNotificationFilter(pkg);
        if (filter == null)
            return null;

        if (!filter.regex.matcher(title).find() &&
                !filter.regex.matcher(desc).find()) {
            return null;
        }
        if (filter.channels.contains(channelId))
            return filter;

        return null;
    }
    static NotificationFilter getFilter(StatusBarNotification sbn) {
        Notification notif = sbn.getNotification();
        String title = Constants.getExtraString(notif.extras, Notification.EXTRA_TITLE);
        String desc = Constants.getExtraString(notif.extras, Notification.EXTRA_TEXT);

        return getFilter(sbn.getPackageName(),
                title, desc, notif.getChannelId());
    }
}
