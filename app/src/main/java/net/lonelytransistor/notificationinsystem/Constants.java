package net.lonelytransistor.notificationinsystem;

import android.os.Environment;

public class Constants {
    private static final String TAG = "Constants";

    public static final int TAG_PREFIX = 0x20;

    public static final String APK_PREF_KEY = "APP_PREF_KEY";
    public static final String APK_FILTERS_KEY = "APP_PREF_KEY__APP_FILTERS";
    public static final String APK_PKGS_KEY = "APP_PREF_KEY__APP_PKGS";

    public static final String APK_CHANNELS_SUFFIX = ":APK_CHANNELS_SUFFIX";
    public static final String APK_REGEX_SUFFIX = ":APK_REGEX_SUFFIX";
    public static final String APK_HIDE_PANEL_SUFFIX = ":APK_HIDE_PANEL_SUFFIX";
    public static final String APK_WIDTH_SUFFIX = ":APK_WIDTH_SUFFIX";
    public static final String APK_HEIGHT_SUFFIX = ":APK_HEIGHT_SUFFIX";
    public static final String APK_SLOT_SUFFIX = ":APK_SLOT_SUFFIX";
    public static final String VERSION_SUFFIX = ":VERSION_SUFFIX";
    public static final String SHARED_PREFS_PATH = Environment.getDataDirectory() +
            "/data/" +
            BuildConfig.APPLICATION_ID +
            "/shared_prefs/" +
            APK_PREF_KEY + ".xml";
    public static final String BROADCAST_SETTINGS_CHANGED = "net.lonelytransistor.notificationinsystem.BROADCAST_SETTINGS_CHANGED";
    public static final String BROADCAST_SETTINGS_REQUEST = "net.lonelytransistor.notificationinsystem.BROADCAST_SETTINGS_REQUEST";
    public static final String PERMISSION_SYSTEMUI = "com.android.systemui.permission.SELF";
}
