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
import java.util.Arrays;
import java.util.Objects;

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
    private static Bitmap getBitmap(Context ctx, Icon icon, int width, int height, Bitmap.Config cfg) {
        Drawable drawable = icon.loadDrawable(ctx);
        Bitmap bitmap = Bitmap.createBitmap(width, height, cfg);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static StatusBarIcon Construct(String pkgName, StatusBarIcon siconOld, @NonNull Icon iconNew, int width, int height) {
        Context ctx = StatusBarIconControllerImpl.getContext();
        Bitmap.Config cfg = Bitmap.Config.ARGB_8888;

        if (siconOld == null) {
            return new StatusBarIcon(pkgName, Icon.createWithBitmap(
                    getBitmap(ctx, iconNew, width, height, cfg)));
        }

        Icon iconOld = siconOld.icon;
        if (iconNew == iconOld) {
            return siconOld;
        }

        int typeOld = iconOld.getType();
        int typeNew = iconNew.getType();
        if (typeOld != typeNew) {
            return new StatusBarIcon(pkgName, Icon.createWithBitmap(
                    getBitmap(ctx, iconNew, width, height, cfg)));
        }

        switch (typeNew) {
            case Icon.TYPE_BITMAP:
            case Icon.TYPE_ADAPTIVE_BITMAP:
                Bitmap bitmapNew = getBitmap(ctx, iconNew, width, height, cfg);
                Bitmap bitmapOld = getBitmap(ctx, iconOld, width, height, cfg);

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
                return siconOld;
            case Icon.TYPE_RESOURCE:
                if (iconOld.getResId() == iconNew.getResId() &&
                        Objects.equals(iconOld.getResPackage(), iconNew.getResPackage())) {
                    return siconOld;
                }
                break;
            case Icon.TYPE_URI:
            case Icon.TYPE_URI_ADAPTIVE_BITMAP:
                if (iconOld.getUri().compareTo(iconNew.getUri()) == 0) {
                    return siconOld;
                }
                break;
            case Icon.TYPE_DATA:
                int lenOld = (int) XposedHelpers.callMethod(iconOld, "getDataLength");
                int lenNew = (int) XposedHelpers.callMethod(iconNew, "getDataLength");
                if (lenOld != lenNew)
                    break;
                int offOld = (int) XposedHelpers.callMethod(iconOld, "getDataOffset");
                int offNew = (int) XposedHelpers.callMethod(iconNew, "getDataOffset");
                if (offOld != offNew)
                    break;
                byte[] dataOld = (byte[]) XposedHelpers.callMethod(iconOld, "getDataBytes");
                byte[] dataNew = (byte[]) XposedHelpers.callMethod(iconNew, "getDataBytes");
                if (!Arrays.equals(dataOld, dataNew))
                    break;
                return siconOld;
            default:
                break;
        }

        return new StatusBarIcon(pkgName, Icon.createWithBitmap(
                getBitmap(ctx, iconNew, width, height, cfg)));
    }
}