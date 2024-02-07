package net.lonelytransistor.notificationinsystem.ui;

import static net.lonelytransistor.notificationinsystem.Constants.sendConfig;

import android.os.Bundle;
import android.util.Log;

import net.lonelytransistor.commonlib.apkselect.SelectorAdapter;
import net.lonelytransistor.notificationinsystem.Constants;
import net.lonelytransistor.commonlib.Preferences;
import net.lonelytransistor.commonlib.apkselect.Store;
import net.lonelytransistor.commonlib.apkselect.StoreService;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ApkSelectorService extends StoreService {
    private static final String TAG = "ApkSelectorService";

    private Preferences preferences = null;
    private Map<String,String> fieldNames = new HashMap<>();
    private int ver = 0;
    @Override
    public String loadRegex(Store.ApkInfo info) {
        return preferences.getString(info.pkgName + Constants.APK_REGEX_SUFFIX);
    }
    @Override
    public Set<String> loadCategories(Store.ApkInfo info) {
        return preferences.getStringSet(info.pkgName + Constants.APK_CHANNELS_SUFFIX);
    }
    @Override
    public Bundle loadExtraSettings(Store.ApkInfo info) {
        Bundle extra = new Bundle();
        extra.putInt(Constants.APK_SLOT_SUFFIX, preferences.getInt(info.pkgName + Constants.APK_SLOT_SUFFIX, 0));
        extra.putInt(Constants.APK_WIDTH_SUFFIX, preferences.getInt(info.pkgName + Constants.APK_WIDTH_SUFFIX, 32));
        extra.putInt(Constants.APK_HEIGHT_SUFFIX, preferences.getInt(info.pkgName + Constants.APK_HEIGHT_SUFFIX, 32));
        extra.putBoolean(Constants.APK_HIDE_PANEL_SUFFIX, preferences.getBoolean(info.pkgName + Constants.APK_HIDE_PANEL_SUFFIX, false));
        if (fieldNames.isEmpty()) {
            fieldNames.put(Constants.APK_SLOT_SUFFIX, "Slot (some are always invisible)");
            fieldNames.put(Constants.APK_WIDTH_SUFFIX, "Width");
            fieldNames.put(Constants.APK_HEIGHT_SUFFIX, "Height");
            fieldNames.put(Constants.APK_HIDE_PANEL_SUFFIX, "Hide from notif. panel");
        }
        extra.putSerializable(FIELD_NAMES_EXTRA, (Serializable) fieldNames);
        return extra;
    }
    @Override
    public void save() {
        super.save();
        ver = preferences.getInt(Constants.VERSION_SUFFIX);
        preferences.clear();
    }
    @Override
    public void save(Store.ApkInfo info, String notificationRegex, Set<String> monitoredGroups, Bundle extras) {
        preferences.setStringSet(info.pkgName + Constants.APK_CHANNELS_SUFFIX, monitoredGroups);
        preferences.setString(info.pkgName + Constants.APK_REGEX_SUFFIX, info.notificationRegex);
        preferences.setInt(info.pkgName + Constants.APK_SLOT_SUFFIX, extras.getInt(Constants.APK_SLOT_SUFFIX));
        preferences.setInt(info.pkgName + Constants.APK_WIDTH_SUFFIX, extras.getInt(Constants.APK_WIDTH_SUFFIX));
        preferences.setInt(info.pkgName + Constants.APK_HEIGHT_SUFFIX, extras.getInt(Constants.APK_HEIGHT_SUFFIX));
        preferences.setBoolean(info.pkgName + Constants.APK_HIDE_PANEL_SUFFIX, extras.getBoolean(Constants.APK_HIDE_PANEL_SUFFIX));
    }
    @Override
    public void save(Set<String> monitoredPackages) {
        preferences.setStringSet(Constants.APK_PKGS_SUFFIX, monitoredPackages);
        preferences.setInt(Constants.VERSION_SUFFIX, ver+1);
        sendConfig(this, preferences.getXml());
    }
    @Override
    public void onCreate() {
        super.onCreate();
        preferences = new Preferences(this, Constants.APK_PREF_KEY);
    }
}
