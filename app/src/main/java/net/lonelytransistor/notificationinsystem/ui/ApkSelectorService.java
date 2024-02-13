package net.lonelytransistor.notificationinsystem.ui;

import static net.lonelytransistor.notificationinsystem.Helpers.sendConfig;

import android.os.Bundle;
import android.util.Log;

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
    private final Map<String,String> fieldNames = new HashMap<>();
    private Map<String, Store.Data> filters;

    @Override
    protected Store.Data load(Store.ApkInfo info) {
        if (filters.containsKey(info.pkgName)) {
            Store.Data data = filters.get(info.pkgName);
            if (data != null) {
                return data;
            }
        }
        Store.Data data = new Store.Data();
        data.extra.put(Constants.APK_REGEX_SUFFIX, "");
        data.catExtra.put(Constants.APK_SLOT_SUFFIX, 0);
        data.catExtra.put(Constants.APK_WIDTH_SUFFIX, 32);
        data.catExtra.put(Constants.APK_HEIGHT_SUFFIX, 32);
        data.catExtra.put(Constants.APK_HIDE_PANEL_SUFFIX, false);
        data.extra.put(FIELD_NAMES_EXTRA, (Serializable) fieldNames);
        return data;
    }
    @Override
    public void save(Store.ApkInfo info, Store.Data data) {
        filters.put(info.pkgName, data);
    }

    @Override
    protected void onBeforeLoad() {
        if (fieldNames.isEmpty()) {
            fieldNames.put(Constants.APK_REGEX_SUFFIX, "Regular expression filter");
            fieldNames.put(Constants.APK_SLOT_SUFFIX, "Slot (some are always invisible)");
            fieldNames.put(Constants.APK_WIDTH_SUFFIX, "Width");
            fieldNames.put(Constants.APK_HEIGHT_SUFFIX, "Height");
            fieldNames.put(Constants.APK_HIDE_PANEL_SUFFIX, "Hide from notif. panel");
        }
        filters = (Map<String, Store.Data>) preferences.getMap(Constants.APK_FILTERS_KEY, new HashMap<>());
        Log.i(TAG, "Filters: " + filters);
    }
    @Override
    protected void onBeforeSave() {
        filters.clear();
    }
    @Override
    public void onAfterSave(Set<String> monitoredPackages) {
        preferences.setMap(Constants.APK_FILTERS_KEY, filters);
        preferences.setStringSet(Constants.APK_PKGS_KEY, monitoredPackages);
        sendConfig(this, preferences.getXml());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        preferences = new Preferences(this, Constants.APK_PREF_KEY);
    }
}
