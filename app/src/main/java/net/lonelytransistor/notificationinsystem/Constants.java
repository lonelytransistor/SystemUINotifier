package net.lonelytransistor.notificationinsystem;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.SpannableString;
import android.util.Log;

import net.lonelytransistor.commonlib.Preferences;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import de.robv.android.xposed.XposedHelpers;

public class Constants {
    private static final String TAG = "Constants";

    public static final int TAG_PREFIX = 0x20;

    public static final String APK_PREF_KEY = "APP_PREF_KEY";
    public static final String APK_CHANNELS_SUFFIX = ":APK_CHANNELS_SUFFIX";
    public static final String APK_REGEX_SUFFIX = ":APK_REGEX_SUFFIX";
    public static final String APK_HIDE_PANEL_SUFFIX = ":APK_HIDE_PANEL_SUFFIX";
    public static final String APK_WIDTH_SUFFIX = ":APK_WIDTH_SUFFIX";
    public static final String APK_HEIGHT_SUFFIX = ":APK_HEIGHT_SUFFIX";
    public static final String APK_SLOT_SUFFIX = ":APK_SLOT_SUFFIX";
    public static final String APK_PKGS_SUFFIX = ":APK_PKGS_SUFFIX";
    public static final String VERSION_SUFFIX = ":VERSION_SUFFIX";
    public static final String SHARED_PREFS_PATH = Environment.getDataDirectory() +
            "/data/" +
            BuildConfig.APPLICATION_ID +
            "/shared_prefs/" +
            APK_PREF_KEY + ".xml";
    public static final String BROADCAST_SETTINGS_CHANGED = "net.lonelytransistor.notificationinsystem.BROADCAST_SETTINGS_CHANGED";
    public static final String BROADCAST_SETTINGS_REQUEST = "net.lonelytransistor.notificationinsystem.BROADCAST_SETTINGS_REQUEST";
    public static final String PERMISSION_SYSTEMUI = "com.android.systemui.permission.SELF";

    public static void registerReceiver(Context ctx, String action, String permission, BroadcastReceiver receiver) {
        IntentFilter fp = new IntentFilter(action);
        ctx.registerReceiver(receiver, fp, permission, null);
    }
    public static void registerReceiver(Context ctx, String action, BroadcastReceiver receiver) {
        IntentFilter fp = new IntentFilter(action);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ctx.registerReceiver(receiver, fp, Context.RECEIVER_EXPORTED);
        } else {
            ctx.registerReceiver(receiver, fp);
        }
    }
    public static String getExtraString(Bundle extras, String name) {
        Object obj = extras.get(name);
        String str;
        if (obj instanceof SpannableString) {
            str = String.valueOf(obj);
        } else if (obj instanceof String) {
            str = (String) obj;
        } else {
            str = "";
        }
        return str;
    }
    public static void sendConfig(Context ctx) {
        Preferences prefs = new Preferences(ctx, Constants.APK_PREF_KEY);
        sendConfig(ctx, prefs.getXml());
    }
    public static void sendConfig(Context ctx, byte[] data) {
        Intent intent = new Intent(Constants.BROADCAST_SETTINGS_CHANGED);
        intent.setPackage("com.android.systemui");
        intent.putExtra(Constants.APK_PREF_KEY, data);
        ctx.sendBroadcast(intent);
    }

    public static String dump(Object obj) {
        if (obj == null)
            return "";

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        XposedHelpers.callMethod(obj, "dump",
                new Class[]{FileDescriptor.class, PrintWriter.class, String[].class},
                null, pw, new String[]{});
        return sw.toString();
    }
}
