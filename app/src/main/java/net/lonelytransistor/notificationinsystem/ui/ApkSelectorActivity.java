package net.lonelytransistor.notificationinsystem.ui;

import net.lonelytransistor.commonlib.apkselect.SelectorServicedActivity;

public class ApkSelectorActivity extends SelectorServicedActivity {
    @Override
    protected boolean isButtonVisible(Button btn) {
        return btn == Button.RELOAD || btn == Button.SORT;
    }
    @Override
    protected String getHeader() {
        return "Monitored Apps";
    }
    @Override
    protected Class<?> getStoreService() {
        return ApkSelectorService.class;
    }
}
