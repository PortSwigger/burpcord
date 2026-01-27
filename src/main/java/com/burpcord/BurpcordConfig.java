package com.burpcord;

public class BurpcordConfig {
    private final burp.api.montoya.persistence.Preferences preferences;

    private static final String KEY_APP_ID = "burpcord_app_id";
    private static final String KEY_UPDATE_INTERVAL = "burpcord_update_interval";
    private static final String KEY_SHOW_INTERCEPT = "burpcord_show_intercept";
    private static final String KEY_SHOW_SCAN = "burpcord_show_scan";
    private static final String KEY_SHOW_REPEATER = "burpcord_show_repeater";
    private static final String KEY_SHOW_PROXY = "burpcord_show_proxy";
    private static final String KEY_RPC_ENABLED = "burpcord_rpc_enabled";
    private static final String KEY_SHOW_INTRUDER = "burpcord_show_intruder";
    private static final String KEY_CUSTOM_STATE = "burpcord_custom_state";
    private static final String KEY_SHOW_SITEMAP = "burpcord_show_sitemap";
    private static final String KEY_SHOW_SCOPE = "burpcord_show_scope";
    private static final String KEY_SHOW_COLLABORATOR = "burpcord_show_collaborator";
    private static final String KEY_SHOW_WEBSOCKETS = "burpcord_show_websockets";

    private static final String DEFAULT_APP_ID = "1457789708753965206";
    private static final int DEFAULT_UPDATE_INTERVAL = 5;
    private static final String DEFAULT_STATE = "Security Researching";

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

    // RPC Master Toggle
    public boolean isRpcEnabled() {
        return isFeatureEnabled(KEY_RPC_ENABLED);
    }

    public void setRpcEnabled(boolean enabled) {
        setFeatureEnabled(KEY_RPC_ENABLED, enabled);
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

    public boolean isShowIntruder() {
        return isFeatureEnabled(KEY_SHOW_INTRUDER);
    }

    public void setShowIntruder(boolean enabled) {
        setFeatureEnabled(KEY_SHOW_INTRUDER, enabled);
    }

    // Custom State Text
    public String getCustomState() {
        String state = preferences.getString(KEY_CUSTOM_STATE);
        return (state == null || state.isBlank()) ? DEFAULT_STATE : state;
    }

    public void setCustomState(String state) {
        preferences.setString(KEY_CUSTOM_STATE, state);
    }

    // v1.3 - Montoya API Features
    public boolean isShowSiteMap() {
        return isFeatureEnabled(KEY_SHOW_SITEMAP);
    }

    public void setShowSiteMap(boolean enabled) {
        setFeatureEnabled(KEY_SHOW_SITEMAP, enabled);
    }

    public boolean isShowScope() {
        return isFeatureEnabled(KEY_SHOW_SCOPE);
    }

    public void setShowScope(boolean enabled) {
        setFeatureEnabled(KEY_SHOW_SCOPE, enabled);
    }

    public boolean isShowCollaborator() {
        return isFeatureEnabled(KEY_SHOW_COLLABORATOR);
    }

    public void setShowCollaborator(boolean enabled) {
        setFeatureEnabled(KEY_SHOW_COLLABORATOR, enabled);
    }

    public boolean isShowWebSockets() {
        return isFeatureEnabled(KEY_SHOW_WEBSOCKETS);
    }

    public void setShowWebSockets(boolean enabled) {
        setFeatureEnabled(KEY_SHOW_WEBSOCKETS, enabled);
    }
}
