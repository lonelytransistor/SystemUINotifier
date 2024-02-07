package net.lonelytransistor.notificationinsystem.hooks.reflected;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.robv.android.xposed.XposedHelpers;

public class StatusBarIconControllerImpl {
    private static final String TAG = "StatusBarIconControllerImpl";
    static Object self = null;
    @SuppressLint("StaticFieldLeak")
    static Context context = null;
    static Map<Integer, Set<StatusBarIconHolder>> ownIcons = new HashMap<>();

    static void init(Object self_) {
        self = self_;
        context = (Context) XposedHelpers.getObjectField(self, "mContext");
    }

    public static Context getContext() {
        return context;
    }

    public static void refreshIcons(String slotName) {
        if (self == null)
            return;

        int index = getSlotIndex(slotName);
        refreshIcons(index);
    }
    public static void refreshIcons(int index) {
        for (StatusBarIconHolder holder : ownIcons.getOrDefault(index, new HashSet<>())) {
            setIcon(index, holder);
        }
    }
    public static void setIcon(int index, StatusBarIconHolder holder) {
        if (self == null)
            return;

        if (!ownIcons.containsKey(index))
            ownIcons.put(index, new HashSet<>());
        Set<StatusBarIconHolder> icons = ownIcons.get(index);
        icons.add(holder);

        XposedHelpers.callMethod(self, "setIcon",
                index, holder.self);
    }

    private static StatusBarIconHolder getHolder(int index, int uid, int tag) {
        if (!ownIcons.containsKey(index))
            ownIcons.put(index, new HashSet<>());
        Set<StatusBarIconHolder> icons = ownIcons.get(index);
        for (StatusBarIconHolder holder : icons) {
            if ((uid == holder.uid && uid >= 0) || (tag == holder.tag && tag >= 0)) {
                return holder;
            }
        }
        return null;
    }
    public static int getUid(int index, int tag) {
        StatusBarIconHolder holder = getHolder(index, -1, tag);
        return holder == null ? -1 : holder.uid;
    }
    public static int getTag(int index, int uid) {
        StatusBarIconHolder holder = getHolder(index, uid, -1);
        return holder == null ? -1 : holder.tag;
    }
    public static void removeIcon(int index, StatusBarIconHolder holder) {
        removeIcon(index, holder.tag);
    }
    public static void removeIcon(int index, int tag) {
        if (self == null)
            return;

        StatusBarIconHolder holder = getHolder(index, -1, tag);
        if (holder == null)
            return;
        ownIcons.get(index).remove(holder);

        XposedHelpers.callMethod(self, "removeIcon",
                index, holder.tag);
    }
    public static void removeAllOwnIcons() {
        for (int slot : ownIcons.keySet()) {
            if (!ownIcons.get(slot).isEmpty()) {
                removeAllOwnIconsInSlot(slot);
            }
        }
    }
    public static void removeAllOwnIconsInSlot(int index) {
        if (self == null)
            return;

        if (!ownIcons.containsKey(index))
            ownIcons.put(index, new HashSet<>());
        Set<StatusBarIconHolder> icons = new HashSet<>();
        icons.addAll(ownIcons.get(index));
        for (StatusBarIconHolder holder : icons) {
            removeIcon(index, holder);
        }
    }
    public static int getSlotIndex(String name) {
        if (self == null)
            return -1;

        return (int) XposedHelpers.callMethod(self, "getSlotIndex",
                name);
    }
    public static String getSlotName(int index) {
        if (self == null)
            return null;

        return (String) XposedHelpers.callMethod(self, "getSlotName",
                index);
    }
    public static void removeAllIconsInSlot(int index) {
        if (self == null)
            return;

        if (!ownIcons.containsKey(index))
            ownIcons.put(index, new HashSet<>());
        Set<StatusBarIconHolder> icons = ownIcons.get(index);
        icons.clear();

        XposedHelpers.callMethod(self, "removeAllIconsForSlot",
                getSlotName(index));
    }
}