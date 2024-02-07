package net.lonelytransistor.notificationinsystem.hooks;

import java.lang.reflect.Method;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class XPosedHook implements IXposedHookLoadPackage {
    private static final String TAG = "XPosedHook";

    public static void findAndHookMethod(Class<?> klass, String name, XC_MethodHook hook) throws NoSuchMethodError {
        for (Method mth : klass.getDeclaredMethods()) {
            if (mth.getName().equals(name)) {
                XposedBridge.hookMethod(mth, hook);
                return;
            }
        }
        throw new NoSuchMethodError(klass.getName() + "#" + name);
    }
    public static void findAndHookMethod(Class<?>[] klasses, String name, XC_MethodHook hook) throws NoSuchMethodError {
        for (Class<?> klass : klasses) {
            findAndHookMethod(klass, name, hook);
        }
    }
    public static void findAndHookMethod(Class<?> klass, String[] names, XC_MethodHook hook) throws NoSuchMethodError {
        for (String name : names) {
            findAndHookMethod(klass, name, hook);
        }
    }
    public static void findAndHookMethod(Class<?>[] klasses, String[] names, XC_MethodHook hook) throws NoSuchMethodError {
        for (Class<?> klass : klasses) {
            for (String name : names) {
                findAndHookMethod(klass, name, hook);
            }
        }
    }
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (lpparam.packageName.equals("net.lonelytransistor.notificationinsystem"))
            HookSelf.handleLoadPackage(lpparam);
        if (lpparam.packageName.equals("com.android.systemui"))
            HookSystemUI.handleLoadPackage(lpparam);
    }
}
