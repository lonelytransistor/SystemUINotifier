package net.lonelytransistor.notificationinsystem.hooks;

import android.app.AndroidAppHelper;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.FileObserver;
import android.util.Log;

import androidx.annotation.Nullable;

import net.lonelytransistor.commonlib.Preferences;
import net.lonelytransistor.notificationinsystem.Constants;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import de.robv.android.xposed.IXposedHookZygoteInit;

public class XPosedZygote implements IXposedHookZygoteInit {
    private static final String TAG = "XPosedZygote";

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) {
    }
}