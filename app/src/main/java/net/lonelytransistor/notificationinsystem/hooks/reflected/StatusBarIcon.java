package net.lonelytransistor.notificationinsystem.hooks.reflected;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.UserHandle;

import androidx.annotation.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public class StatusBarIcon {
    final Object self;
    final String pkgName;
    final Icon icon;
    final int resId;

    static Class<?> StatusBarIcon_class;
    static Constructor<?> StatusBarIcon_class_constructor_OSOIIS;
    static Field StatusBarIcon_icon;
    static Field StatusBarIcon_pkg;
    static UserHandle UserHandle_SYSTEM;

    StatusBarIcon(Object obj) {
        try {
            self = obj;
            this.pkgName = (String) StatusBarIcon_pkg.get(self);
            this.icon = (Icon) StatusBarIcon_icon.get(self);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        resId = icon.getType() == Icon.TYPE_RESOURCE ? icon.getResId() : -1;
    }
    public StatusBarIcon(String pkgName, Icon icon) {
        try {
            self = StatusBarIcon_class_constructor_OSOIIS.newInstance(
                    UserHandle_SYSTEM, pkgName, icon,
                    0, 0, "dummy");
            this.pkgName = (String) StatusBarIcon_pkg.get(self);
            this.icon = (Icon) StatusBarIcon_icon.get(self);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        resId = icon.getType() == Icon.TYPE_RESOURCE ? icon.getResId() : -1;
    }
    public StatusBarIcon(String pkgName, Icon icon, int width, int height) {
        this(pkgName, resizeIcon(icon, width, height));
    }
    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof Icon) {
            Icon icon = (Icon) obj;
            if (icon.getType() == Icon.TYPE_RESOURCE) {
                return icon.getResId() == resId && pkgName.equals(icon.getResPackage());
            }
        }
        return super.equals(obj);
    }

    private static Icon resizeIcon(Icon icon, int width, int height) {
        Drawable drawable = icon.loadDrawable(StatusBarIconControllerImpl.getContext());
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return Icon.createWithBitmap(bitmap);
    }
}