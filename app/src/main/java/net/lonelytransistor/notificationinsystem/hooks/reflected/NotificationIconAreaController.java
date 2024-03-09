package net.lonelytransistor.notificationinsystem.hooks.reflected;

import android.os.Build;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import de.robv.android.xposed.XposedHelpers;

public class NotificationIconAreaController {
    private static final String TAG = "NotificationIconAreaController";
    static WeakReference<Object> self = new WeakReference<>(null);

    static void init(Object self_) {
        self = new WeakReference<>(self_);
    }
    public static void updateNotificationIcons() {
        Object s = self.get();
        if (s == null)
            return;

        if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU) {
            XposedHelpers.callMethod(s,"updateNotificationIcons",
                    XposedHelpers.getObjectField(s, "mNotificationEntries"));
        } else {
            XposedHelpers.callMethod(s,"updateNotificationIcons");
        }
    }
}
