package net.lonelytransistor.notificationinsystem.hooks.reflected;

import static net.lonelytransistor.notificationinsystem.Constants.DEBUG;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.robv.android.xposed.XposedHelpers;

public class StatusBarIconControllerImpl {
    private static final String TAG = "StatusBarIconControllerImpl";

    static WeakReference<Object> self = new WeakReference<>(null);
    static Map<Integer, Set<StatusBarIconHolder>> ownIcons = new HashMap<>();
    private static final List<String> slotsSnapshot = new ArrayList<>();
    static void init(Object self_) {
        self = new WeakReference<>(self_);
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
    public static int getMaxSlots() {
        updateSlotsDb();
        return slotsSnapshot.size();
    }
    public static int getSlotIndex(String name) {
        updateSlotsDb();
        return slotsSnapshot.indexOf(name);
    }
    public static String getSlotName(int index) {
        updateSlotsDb();
        return slotsSnapshot.get(index);
    }

    // Reflected
    public static Context getContext() {
        Object s = self.get();
        if (s == null)
            return null;

        DEBUG("getContext called");
        return (Context) XposedHelpers.getObjectField(s, "mContext");
    }
    public static void setIcon(int index, StatusBarIconHolder holder) {
        Object s = self.get();
        if (s == null)
            return;

        if (!ownIcons.containsKey(index))
            ownIcons.put(index, new HashSet<>());
        Set<StatusBarIconHolder> icons = ownIcons.get(index);
        icons.add(holder);

        DEBUG("setIcon called");
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            XposedHelpers.callMethod(s, "setIcon",
                    clampSlot(index), holder.self);
        } else {
            XposedHelpers.callMethod(s, "setIcon",
                    getSlotName(clampSlot(index)), holder.self);
        }
    }
    public static void removeIcon(int index, int tag) {
        Object s = self.get();
        if (s == null)
            return;

        StatusBarIconHolder holder = getHolder(index, -1, tag);
        if (holder == null)
            return;
        ownIcons.get(index).remove(holder);

        DEBUG("removeIcon called");
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            XposedHelpers.callMethod(s, "removeIcon",
                    clampSlot(index), tag);
        } else if (android.os.Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) {
            XposedHelpers.callMethod(s, "removeIcon",
                    getSlotName(clampSlot(index)), tag);
        } else if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // WTF IS THIS
            Object mStatusBarIconList = XposedHelpers.getObjectField(s, "mStatusBarIconList");
            List<Object> mSlots = (ArrayList<Object>) XposedHelpers.getObjectField(mStatusBarIconList, "mSlots");
            List<Object> mIconGroups = (ArrayList<Object>) XposedHelpers.getObjectField(s, "mIconGroups");
            int pos = 0;
            for (int ix=0; ix<mSlots.size(); ix++) {
                Object slot = mSlots.get(ix);
                Object mSubSlots = XposedHelpers.getObjectField(slot, "mSubSlots");
                Object mHolder = XposedHelpers.getObjectField(slot, "mHolder");
                if (ix == index) {
                    if (tag == 0) {
                        XposedHelpers.setObjectField(slot, "mHolder", null);
                    } else {
                        int six = (int) XposedHelpers.callMethod(slot, "getIndexForTag", tag);
                        if (six != -1 && mSubSlots != null) {
                            ((List<Object>) mSubSlots).remove(six);
                            pos += six;
                        }
                    }
                    int poss = pos;
                    mIconGroups.forEach(l -> { if (l != null) XposedHelpers.callMethod(l, "onRemoveIcon", poss); });
                    break;
                }
                if (mHolder != null) {
                    pos += 1;
                }
                if (mSubSlots != null) {
                    pos += 1 + ((List<Object>) mSubSlots).size();
                }
            }
        }
    }
    private static void updateSlotsDb() {
        Object s = self.get();
        if (s == null)
            return;

        List<Object> slots;
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            slots = (ArrayList<Object>) XposedHelpers.getObjectField(s, "mSlots");
        } else {
            Object mStatusBarIconList = XposedHelpers.getObjectField(s, "mStatusBarIconList");
            slots = (ArrayList<Object>) XposedHelpers.getObjectField(mStatusBarIconList, "mSlots");
        }

        slotsSnapshot.clear();
        for (Object slot : slots) {
            slotsSnapshot.add((String) XposedHelpers.getObjectField(slot,"mName"));
        }
    }
}