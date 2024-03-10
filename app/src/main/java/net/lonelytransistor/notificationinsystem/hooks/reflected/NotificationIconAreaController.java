package net.lonelytransistor.notificationinsystem.hooks.reflected;

import static net.lonelytransistor.notificationinsystem.Constants.DEBUG;

import android.os.Build;
import java.lang.ref.WeakReference;
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

        DEBUG("updateNotificationIcons called");
        if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.TIRAMISU) {
            XposedHelpers.callMethod(s,"updateNotificationIcons");
        } else if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            XposedHelpers.callMethod(s,"updateNotificationIcons",
                    XposedHelpers.getObjectField(s, "mNotificationEntries"));
        }
    }
}
