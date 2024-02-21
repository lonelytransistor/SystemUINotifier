package net.lonelytransistor.notificationinsystem.hooks.reflected;

import java.lang.ref.WeakReference;

import de.robv.android.xposed.XposedHelpers;

public class NotificationIconAreaController {
    private static final String TAG = "StatusBarIconControllerImpl";
    static WeakReference<Object> self = new WeakReference<>(null);

    static void init(Object self_) {
        self = new WeakReference<>(self_);
    }
    public static void updateNotificationIcons() {
        if (self.get() == null)
            return;

        XposedHelpers.callMethod(self.get(), "updateNotificationIcons");
    }
}
