package net.lonelytransistor.notificationinsystem;

import static net.lonelytransistor.notificationinsystem.Constants.sendConfig;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import net.lonelytransistor.notificationinsystem.ui.SettingsBroadcastReceiver;

public class autostart extends BroadcastReceiver {
    private static final String TAG = "autostart";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            sendConfig(context);
            context.startService(new Intent(context, SettingsBroadcastReceiver.class));
        }
    }
}
