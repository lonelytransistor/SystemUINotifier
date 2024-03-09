package net.lonelytransistor.notificationinsystem.hooks;

import static net.lonelytransistor.notificationinsystem.Helpers.findAndHookMethod;

import java.util.regex.Pattern;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookSettings {
    private static final String TAG = "XPosedSettings";

    static void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        Class<?> klass = XposedHelpers.findClass(
                "com.android.settings.notification.app.NotificationPreferenceController", lpparam.classLoader);
        findAndHookMethod(klass, Pattern.compile("is.*blockable", Pattern.CASE_INSENSITIVE), new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult(true);
            }
        });
    }
}
