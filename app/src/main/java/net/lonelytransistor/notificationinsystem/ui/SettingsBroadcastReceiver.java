package net.lonelytransistor.notificationinsystem.ui;

import static net.lonelytransistor.notificationinsystem.Constants.sendConfig;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import net.lonelytransistor.notificationinsystem.Constants;

public class SettingsBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "SettingsBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Constants.BROADCAST_SETTINGS_REQUEST.equals(intent.getAction())) {
            sendConfig(context);
        }
    }
}
