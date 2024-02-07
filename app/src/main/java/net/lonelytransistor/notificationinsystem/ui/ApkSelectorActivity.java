package net.lonelytransistor.notificationinsystem.ui;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Bundle;

import net.lonelytransistor.commonlib.apkselect.SelectorServicedActivity;
import net.lonelytransistor.notificationinsystem.R;

public class ApkSelectorActivity extends SelectorServicedActivity {
    @Override
    protected String getHeader() {
        return "Monitored Apps";
    }
    @Override
    protected Class<?> getStoreService() {
        return ApkSelectorService.class;
    }
}
