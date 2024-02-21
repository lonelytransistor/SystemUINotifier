package net.lonelytransistor.notificationinsystem.hooks.reflected;

import android.content.Context;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.robv.android.xposed.XposedHelpers;

public class StatusBarIconControllerImpl {
    private static final String TAG = "StatusBarIconControllerImpl";

    static WeakReference<Object> self = new WeakReference<>(null);
    static Map<Integer, Set<StatusBarIconHolder>> ownIcons = new HashMap<>();
    static void init(Object self_) {
        self = new WeakReference<>(self_);
    }
    public static Context getContext() {
        if (self.get() == null)
            return null;

        return (Context) XposedHelpers.getObjectField(self.get(), "mContext");
    }
    private static int clampSlot(int slot) {
        slot = Math.max(slot, 0);
        slot = Math.min(slot, getMaxSlots()-1);
        return slot;
    }

    public static void refreshIcons(String slotName) {
        int index = getSlotIndex(slotName);
        refreshIcons(index);
    }
    public static void refreshIcons(int index) {
        for (StatusBarIconHolder holder : ownIcons.getOrDefault(index, new HashSet<>())) {
            setIcon(index, holder);
        }
    }

    public static void removeIcon(int index, StatusBarIconHolder holder) {
        removeIcon(index, holder.tag);
    }
    public static void removeAllOwnIcons() {
        for (int slot : ownIcons.keySet()) {
            if (!ownIcons.get(slot).isEmpty()) {
                removeAllOwnIconsInSlot(slot);
            }
        }
    }
    public static void removeAllOwnIconsInSlot(int index) {
        if (!ownIcons.containsKey(index))
            ownIcons.put(index, new HashSet<>());
        Set<StatusBarIconHolder> icons = new HashSet<>(ownIcons.get(index));
        for (StatusBarIconHolder holder : icons) {
            removeIcon(clampSlot(index), holder);
        }
    }

    public static int getUid(int index, int tag) {
        StatusBarIconHolder holder = getHolder(index, -1, tag);
        return holder == null ? -1 : holder.uid;
    }
    public static int getTag(int index, int uid) {
        StatusBarIconHolder holder = getHolder(index, uid, -1);
        return holder == null ? -1 : holder.tag;
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

    public static void setIcon(int index, StatusBarIconHolder holder) {
        if (self.get() == null)
            return;

        if (!ownIcons.containsKey(index))
            ownIcons.put(index, new HashSet<>());
        Set<StatusBarIconHolder> icons = ownIcons.get(index);
        icons.add(holder);

        XposedHelpers.callMethod(self.get(), "setIcon",
                clampSlot(index), holder.self);
    }
    public static void removeIcon(int index, int tag) {
        if (self.get() == null)
            return;

        StatusBarIconHolder holder = getHolder(index, -1, tag);
        if (holder == null)
            return;
        ownIcons.get(index).remove(holder);

        XposedHelpers.callMethod(self.get(), "removeIcon",
                clampSlot(index), holder.tag);
    }
    public static int getMaxSlots() {
        if (self.get() == null)
            return 0;

        ArrayList<Object> slots = (ArrayList<Object>) XposedHelpers.callMethod(self.get(), "getSlots");
        return slots.size();
    }
    public static int getSlotIndex(String name) {
        if (self.get() == null)
            return -1;

        return (int) XposedHelpers.callMethod(self.get(), "getSlotIndex",
                name);
    }
    public static String getSlotName(int index) {
        if (self.get() == null)
            return null;

        return (String) XposedHelpers.callMethod(self.get(), "getSlotName",
                clampSlot(index));
    }
}