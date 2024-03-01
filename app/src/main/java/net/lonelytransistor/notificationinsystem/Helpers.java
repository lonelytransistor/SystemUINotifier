package net.lonelytransistor.notificationinsystem;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;

import net.lonelytransistor.commonlib.Preferences;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.io.StringWriter;

import de.robv.android.xposed.XposedHelpers;

public class Helpers {
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

    public static void registerReceiver(Context ctx, String action, String permission, BroadcastReceiver receiver) {
        IntentFilter fp = new IntentFilter(action);
        ctx.registerReceiver(receiver, fp, permission, null);
    }

    public static void registerReceiver(Context ctx, String action, BroadcastReceiver receiver) {
        try {
            ctx.unregisterReceiver(receiver);
        } catch (Exception ignored) {}
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
}
