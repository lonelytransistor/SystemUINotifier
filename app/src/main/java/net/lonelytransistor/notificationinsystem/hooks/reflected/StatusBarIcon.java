package net.lonelytransistor.notificationinsystem.hooks.reflected;

import android.graphics.Bitmap;
import android.graphics.BlendMode;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.UserHandle;

import androidx.annotation.NonNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import de.robv.android.xposed.XposedHelpers;

public class StatusBarIcon {
    final Object self;
    final String pkgName;
    final Icon icon;

    static Class<?> StatusBarIcon_class;
    static Constructor<?> StatusBarIcon_class_constructor_OSOIIS;
    static UserHandle UserHandle_SYSTEM;

    public StatusBarIcon(String pkgName, Icon icon) {
        try {
            self = StatusBarIcon_class_constructor_OSOIIS.newInstance(
                    UserHandle_SYSTEM, pkgName, icon,
                    0, 0, "dummy");
            this.pkgName = pkgName;
            this.icon = icon;
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
    public StatusBarIcon(String pkgName, Icon icon, int width, int height) {
        this(pkgName, resizeIcon(icon, width, height));
    }
    public boolean sameAs(@NonNull Icon otherIcon) {
        return (boolean) XposedHelpers.callMethod(otherIcon,
                "sameAs", otherIcon); // The fuck you hide this for, Google?
    }

    private static Icon resizeIcon(Icon icon, int width, int height) {
        Drawable drawable = icon.loadDrawable(StatusBarIconControllerImpl.getContext());
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        Icon icon_new = Icon.createWithBitmap(bitmap);
        //icon_new.setTintBlendMode(BlendMode.SOFT_LIGHT);

        return icon_new;
    }
}