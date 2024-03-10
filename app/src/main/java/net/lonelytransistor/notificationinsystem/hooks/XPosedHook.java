package net.lonelytransistor.notificationinsystem.hooks;

import static net.lonelytransistor.notificationinsystem.Constants.DEBUG;

import android.util.Log;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class XPosedHook implements IXposedHookLoadPackage {
    private static final String TAG = "XPosedHook";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        DEBUG("handleLoadPackage('" + lpparam.packageName + "')");
        switch (lpparam.packageName) {
            case "net.lonelytransistor.notificationinsystem" -> HookSelf.handleLoadPackage(lpparam);
            case "com.android.systemui" -> HookSystemUI.handleLoadPackage(lpparam);
            case "com.android.settings" -> HookSettings.handleLoadPackage(lpparam);
        }
    }
}
