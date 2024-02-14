package net.lonelytransistor.notificationinsystem.hooks;


import android.app.Notification;
import android.content.res.Resources;
import android.graphics.drawable.Icon;
import android.service.notification.StatusBarNotification;

import net.lonelytransistor.notificationinsystem.Helpers;
import net.lonelytransistor.notificationinsystem.hooks.reflected.StatusBarIcon;
import net.lonelytransistor.notificationinsystem.hooks.reflected.StatusBarIconHolder;

import java.io.InvalidObjectException;
import java.util.Objects;

public class StatusBarNotificationHolder {
    final int uid;
    final int slot;
    final int width;
    final int height;
    final boolean hide;
    final String key;
    final String pkgName;
    final String category;
    final String title;
    final String desc;
    StatusBarIcon icon;
    StatusBarIconHolder iconHolder;

    boolean setIcon(Notification notif) {
        Icon smallIcon = notif.getSmallIcon();
        if (smallIcon == null)
            smallIcon = notif.getLargeIcon();
        if (smallIcon == null)
            throw new Resources.NotFoundException("Icon not found!");
        if (icon != null && icon.sameAs(smallIcon) && false)
            return false;
        icon = new StatusBarIcon(pkgName, smallIcon, width, height);
        iconHolder = new StatusBarIconHolder(icon, uid);
        return true;
    }
    StatusBarNotificationHolder(StatusBarNotification sbn, int uid, int slot, int width, int height, boolean hide) {
        this.uid = uid;
        this.slot = slot;
        this.width = width;
        this.height = height;
        this.hide = hide;

        Notification notif = sbn.getNotification();
        key = sbn.getKey();
        pkgName = sbn.getPackageName();
        category = notif.category;
        title = Helpers.getExtraString(notif.extras, Notification.EXTRA_TITLE);
        desc = Helpers.getExtraString(notif.extras, Notification.EXTRA_TEXT);
        setIcon(notif);
    }
    Boolean update(StatusBarNotification sbn) throws InvalidObjectException {
        if (!Objects.equals(key, sbn.getKey()) ||
                !Objects.equals(pkgName, sbn.getPackageName()) ||
                !Objects.equals(category, sbn.getNotification().category)) {
            throw new InvalidObjectException("Notification mismatch");
        }
        return setIcon(sbn.getNotification());
    }
}