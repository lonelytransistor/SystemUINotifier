package net.lonelytransistor.notificationinsystem.hooks;

import android.util.Log;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

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
    public static void findAndHookMethod(Class<?> klass, Pattern subklass_rex, String names, XC_MethodHook hook) throws NoSuchMethodError {
        for (Class<?> inner : klass.getDeclaredClasses()) {
            if (!subklass_rex.matcher(inner.getName()).find())
                continue;
            findAndHookMethod(inner, names, hook);
        }
    }


    public static void findAndHookMethod(Class<?> klass, Pattern name_rex, XC_MethodHook hook) throws NoSuchMethodError {
        boolean hooked = false;
        for (Method mth : klass.getDeclaredMethods()) {
            Log.i(TAG, mth.getName() + " ? " + name_rex);
            if (name_rex.matcher(mth.getName()).find()) {
                XposedBridge.hookMethod(mth, hook);
                hooked = true;
            }
        }
        if (!hooked) {
            throw new NoSuchMethodError(klass.getName() + "#r'" + name_rex.toString() + "'");
        }
    }
    public static void findAndHookMethod(Class<?> klass, Pattern subklass_rex, Pattern names_rex, XC_MethodHook hook) throws NoSuchMethodError {
        for (Class<?> inner : klass.getDeclaredClasses()) {
            if (!subklass_rex.matcher(inner.getName()).find())
                continue;
            findAndHookMethod(inner, names_rex, hook);
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
