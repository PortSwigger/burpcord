package com.burpcord;

public class BurpcordConfig {
    private final burp.api.montoya.persistence.Preferences preferences;

    private static final String KEY_APP_ID = "burpcord_app_id";
    private static final String KEY_UPDATE_INTERVAL = "burpcord_update_interval";
    private static final String KEY_SHOW_INTERCEPT = "burpcord_show_intercept";
    private static final String KEY_SHOW_SCAN = "burpcord_show_scan";
    private static final String KEY_SHOW_REPEATER = "burpcord_show_repeater";
    private static final String KEY_SHOW_PROXY = "burpcord_show_proxy";

    private static final String DEFAULT_APP_ID = "1457789708753965206";
    private static final int DEFAULT_UPDATE_INTERVAL = 5;

    public BurpcordConfig(burp.api.montoya.persistence.Preferences preferences) {
        this.preferences = preferences;
    }

    public String getAppId() {
        String appId = preferences.getString(KEY_APP_ID);
        return (appId == null || appId.isBlank()) ? DEFAULT_APP_ID : appId;
    }

    public void setAppId(String appId) {
        preferences.setString(KEY_APP_ID, appId);
    }

    public int getUpdateInterval() {
        Integer val = preferences.getInteger(KEY_UPDATE_INTERVAL);
        return val == null ? DEFAULT_UPDATE_INTERVAL : val;
    }

    public void setUpdateInterval(int seconds) {
        preferences.setInteger(KEY_UPDATE_INTERVAL, seconds);
    }

    public boolean isFeatureEnabled(String featureKey) {
        // Default everything to true
        Boolean val = preferences.getBoolean(featureKey);
        return val == null ? true : val;
    }

    public void setFeatureEnabled(String featureKey, boolean enabled) {
        preferences.setBoolean(featureKey, enabled);
    }

    public boolean isShowIntercept() {
        return isFeatureEnabled(KEY_SHOW_INTERCEPT);
    }

    public void setShowIntercept(boolean enabled) {
        setFeatureEnabled(KEY_SHOW_INTERCEPT, enabled);
    }

    public boolean isShowScan() {
        return isFeatureEnabled(KEY_SHOW_SCAN);
    }

    public void setShowScan(boolean enabled) {
        setFeatureEnabled(KEY_SHOW_SCAN, enabled);
    }

    public boolean isShowRepeater() {
        return isFeatureEnabled(KEY_SHOW_REPEATER);
    }

    public void setShowRepeater(boolean enabled) {
        setFeatureEnabled(KEY_SHOW_REPEATER, enabled);
    }

    public boolean isShowProxy() {
        return isFeatureEnabled(KEY_SHOW_PROXY);
    }

    public void setShowProxy(boolean enabled) {
        setFeatureEnabled(KEY_SHOW_PROXY, enabled);
    }
}
