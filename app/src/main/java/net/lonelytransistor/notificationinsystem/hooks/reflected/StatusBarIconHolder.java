package net.lonelytransistor.notificationinsystem.hooks.reflected;

import net.lonelytransistor.notificationinsystem.Constants;

import java.lang.reflect.Field;

import de.robv.android.xposed.XposedHelpers;

public class StatusBarIconHolder {
    static Class<?> StatusBarIconHolder_class;

    final Object self;
    final int tag;
    final int uid;
    public StatusBarIconHolder(StatusBarIcon icon, int tag_) {
        uid = tag_;
        tag = tag_ + Constants.TAG_PREFIX;
        self = XposedHelpers.callStaticMethod(StatusBarIconHolder_class,
                "fromIcon", icon.self);
        XposedHelpers.setIntField(self, "mTag", tag);
    }
}