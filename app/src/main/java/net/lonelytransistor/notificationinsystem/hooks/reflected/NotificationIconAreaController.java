package net.lonelytransistor.notificationinsystem.hooks.reflected;

import de.robv.android.xposed.XposedHelpers;

public class NotificationIconAreaController {
    private static final String TAG = "StatusBarIconControllerImpl";
    static Object self = null;

    static void init(Object self_) {
        self = self_;
    }
    public static void updateNotificationIcons() {
        if (self == null)
            return;

        XposedHelpers.callMethod(self, "updateNotificationIcons");
    }
}
