package net.lonelytransistor.notificationinsystem.hooks.reflected;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.UserHandle;

import androidx.annotation.NonNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import de.robv.android.xposed.XposedHelpers;

public class StatusBarIcon {
    static Constructor<?> StatusBarIcon_class_constructor_OSOIIS;
    static UserHandle UserHandle_SYSTEM;

    final Object self;
    final String pkgName;
    final Icon icon;
    private StatusBarIcon(String pkgName, Icon icon) {
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
    public static StatusBarIcon Construct(String pkgName, StatusBarIcon siconOld, @NonNull Icon iconNew, int width, int height) {
        if (siconOld != null) {
            Icon iconOld = siconOld.icon;
            if (iconOld.getType() == Icon.TYPE_BITMAP && iconNew.getType() == Icon.TYPE_BITMAP) {
                Context ctx = StatusBarIconControllerImpl.getContext();
                Bitmap.Config cfg = Bitmap.Config.ARGB_8888;
                Drawable drawableOld = iconOld.loadDrawable(ctx);
                Drawable drawableNew = iconNew.loadDrawable(ctx);

                Bitmap bitmapNew = Bitmap.createBitmap(width, height, cfg);
                Canvas canvasNew = new Canvas(bitmapNew);
                drawableNew.setBounds(0, 0, canvasNew.getWidth(), canvasNew.getHeight());
                drawableNew.draw(canvasNew);

                Bitmap bitmapOld = Bitmap.createBitmap(width, height, cfg);
                Canvas canvasOld = new Canvas(bitmapOld);
                drawableOld.setBounds(0, 0, canvasOld.getWidth(), canvasOld.getHeight());
                drawableOld.draw(canvasOld);

                Bitmap bitmapCmp = Bitmap.createBitmap(width, height, cfg);
                Canvas canvasCmp = new Canvas(bitmapCmp);
                Paint paint = new Paint();
                paint.setAlpha(255);
                paint.setAntiAlias(false);
                canvasCmp.drawBitmap(bitmapOld, 0, 0, paint);
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.XOR));
                canvasCmp.drawBitmap(bitmapNew, 0, 0, paint);
                bitmapCmp = Bitmap.createScaledBitmap(bitmapCmp, 8, 8, true);
                for (int x = 0; x < 8; x++) {
                    for (int y = 0; y < 8; y++) {
                        if (bitmapCmp.getPixel(x, y) != 0) {
                            return new StatusBarIcon(pkgName, Icon.createWithBitmap(bitmapNew));
                        }
                    }
                }
                return new StatusBarIcon(pkgName, iconOld);
            } else if ((boolean) XposedHelpers.callMethod(iconNew, "sameAs", iconOld)) {
                return new StatusBarIcon(pkgName, iconOld);
            }
        }
        Context ctx = StatusBarIconControllerImpl.getContext();
        Bitmap.Config cfg = Bitmap.Config.ARGB_8888;
        Drawable drawableNew = iconNew.loadDrawable(ctx);

        Bitmap bitmapNew = Bitmap.createBitmap(width, height, cfg);
        Canvas canvasNew = new Canvas(bitmapNew);
        drawableNew.setBounds(0, 0, canvasNew.getWidth(), canvasNew.getHeight());
        drawableNew.draw(canvasNew);
        return new StatusBarIcon(pkgName, Icon.createWithBitmap(bitmapNew));
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