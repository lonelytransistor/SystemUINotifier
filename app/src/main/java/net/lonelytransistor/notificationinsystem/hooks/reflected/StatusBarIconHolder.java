package net.lonelytransistor.notificationinsystem.hooks.reflected;

import net.lonelytransistor.notificationinsystem.Constants;

import java.lang.reflect.Field;

import de.robv.android.xposed.XposedHelpers;

public class StatusBarIconHolder {
    final Object self;
    final int tag;
    final int uid;

    static Class<?> StatusBarIconHolder_class;
    static Field StatusBarIconHolder_field_tag;
    public StatusBarIconHolder(StatusBarIcon icon, int tag_) {
        try {
            uid = tag_;
            tag = tag_ + Constants.TAG_PREFIX;
            self = XposedHelpers.callStaticMethod(StatusBarIconHolder_class, "fromIcon",
                    icon.self);
            StatusBarIconHolder_field_tag.set(self, tag);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    StatusBarIcon getIcon() {
        return new StatusBarIcon(XposedHelpers.callMethod(self, "getIcon"));
    }
}