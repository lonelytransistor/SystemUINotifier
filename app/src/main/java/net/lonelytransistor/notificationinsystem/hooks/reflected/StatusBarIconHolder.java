package net.lonelytransistor.notificationinsystem.hooks.reflected;

import net.lonelytransistor.notificationinsystem.Constants;
import java.lang.reflect.Constructor;
import de.robv.android.xposed.XposedHelpers;

public class StatusBarIconHolder {
    static Constructor<?> StatusBarIconHolder_constructor;

    final Object self;
    final int tag;
    final int uid;
    public StatusBarIconHolder(StatusBarIcon icon, int tag_) {
        try {
            self = StatusBarIconHolder_constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        uid = tag_;
        tag = tag_ + Constants.TAG_PREFIX;
        XposedHelpers.setObjectField(self, "mIcon", icon.self);
        XposedHelpers.setIntField(self, "mTag", tag);
    }
}