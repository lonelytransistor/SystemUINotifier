package net.lonelytransistor.notificationinsystem.hooks.reflected;

import android.annotation.SuppressLint;
import android.content.Context;

import de.robv.android.xposed.XposedHelpers;

public class NotificationIconAreaController {
    private static final String TAG = "StatusBarIconControllerImpl";
    static Object self = null;
    @SuppressLint("StaticFieldLeak")
    static Context context = null;

    static void init(Object self_) {
        self = self_;
        context = (Context) XposedHelpers.getObjectField(self, "mContext");
    }
    public static void updateNotificationIcons() {
        if (self == null)
            return;

        XposedHelpers.callMethod(self, "updateNotificationIcons");
    }
}
