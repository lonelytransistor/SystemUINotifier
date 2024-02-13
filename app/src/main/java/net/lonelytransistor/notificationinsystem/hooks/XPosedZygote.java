package net.lonelytransistor.notificationinsystem.hooks;

import de.robv.android.xposed.IXposedHookZygoteInit;

public class XPosedZygote implements IXposedHookZygoteInit {
    private static final String TAG = "XPosedZygote";

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) {
    }
}