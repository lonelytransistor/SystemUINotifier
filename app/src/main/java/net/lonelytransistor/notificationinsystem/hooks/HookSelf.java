package net.lonelytransistor.notificationinsystem.hooks;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookSelf {
    private static final String TAG = "XPosedSelfHook";

    static void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
    }
}
