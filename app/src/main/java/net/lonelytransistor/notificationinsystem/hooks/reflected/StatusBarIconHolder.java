package net.lonelytransistor.notificationinsystem.hooks.reflected;

import android.os.Build;

import net.lonelytransistor.notificationinsystem.Constants;

import java.lang.reflect.Constructor;

import de.robv.android.xposed.XposedHelpers;

public class StatusBarIconHolder {
    static Class<?> StatusBarIconHolder_class;

    final Object self;
    final int tag;
    final int uid;
    public StatusBarIconHolder(StatusBarIcon icon, int tag_) {
        uid = tag_;
        tag = tag_ + Constants.TAG_PREFIX;

        if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU) {
            // WTF??
            try {
                Constructor<?> constructor = XposedHelpers.findConstructorBestMatch(StatusBarIconHolder_class);
                self = constructor.newInstance();
                XposedHelpers.setObjectField(self, "mIcon", icon.self);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            self = XposedHelpers.callStaticMethod(StatusBarIconHolder_class,
                    "fromIcon", icon.self);
        }
        XposedHelpers.setIntField(self, "mTag", tag);
    }
}