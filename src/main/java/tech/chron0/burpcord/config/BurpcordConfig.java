package tech.chron0.burpcord.config;

/**
 * <h1>Configuration Management</h1>
 * <p>
 * Manages configuration for the Burpcord extension, utilizing Burp Suite's
 * built-in {@code Preferences} API for persistence logic.
 * </p>
 * 
 * <h2>Stored Preferences</h2>
 * <ul>
 * <li><b>App ID:</b> Custom Discord Application ID.</li>
 * <li><b>Update Interval:</b> Frequency of Rich Presence updates.</li>
 * <li><b>Custom State:</b> User-defined status message.</li>
 * <li><b>Feature Toggles:</b> Individual toggles for displaying specific Burp
 * tools.</li>
 * </ul>
 * 
 * @author Jon Marien
 * @version 2.0.1
 */
public class BurpcordConfig {

    private final burp.api.montoya.persistence.Preferences preferences;

    // Preference keys enum
    private enum ConfigKey {
        APP_ID("burpcord_app_id"),
        UPDATE_INTERVAL("burpcord_update_interval"),
        SHOW_INTERCEPT("burpcord_show_intercept"),
        SHOW_SCAN("burpcord_show_scan"),
        SHOW_PROXY("burpcord_show_proxy"),
        SHOW_REPEATER("burpcord_show_repeater"),
        SHOW_INTRUDER("burpcord_show_intruder"),
        SHOW_WEBSOCKETS("burpcord_show_websockets"),
        SHOW_SITEMAP("burpcord_show_sitemap"),
        SHOW_SCOPE("burpcord_show_scope"),
        SHOW_COLLABORATOR("burpcord_show_collaborator"),
        DEBUG_MODE("burpcord_debug_mode"),
        CUSTOM_STATE("burpcord_custom_state"),
        RPC_ENABLED("burpcord_rpc_enabled");

        private final String key;

        ConfigKey(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }

    // Defaults
    private static final String DEFAULT_APP_ID = "1328087961230639207";
    private static final int DEFAULT_UPDATE_INTERVAL = 30;

    public BurpcordConfig(burp.api.montoya.persistence.Preferences preferences) {
        this.preferences = preferences;
    }

    public String getAppId() {
        String id = preferences.getString(ConfigKey.APP_ID.getKey());
        return (id == null || id.isEmpty()) ? DEFAULT_APP_ID : id;
    }

    public void setAppId(String appId) {
        preferences.setString(ConfigKey.APP_ID.getKey(), appId);
    }

    public int getUpdateInterval() {
        Integer val = preferences.getInteger(ConfigKey.UPDATE_INTERVAL.getKey());
        return (val == null) ? DEFAULT_UPDATE_INTERVAL : val;
    }

    public void setUpdateInterval(int seconds) {
        preferences.setInteger(ConfigKey.UPDATE_INTERVAL.getKey(), seconds);
    }

    public boolean isShowIntercept() {
        return getBoolean(ConfigKey.SHOW_INTERCEPT, true);
    }

    public void setShowIntercept(boolean value) {
        setBoolean(ConfigKey.SHOW_INTERCEPT, value);
    }

    public boolean isShowScan() {
        return getBoolean(ConfigKey.SHOW_SCAN, true);
    }

    public void setShowScan(boolean value) {
        setBoolean(ConfigKey.SHOW_SCAN, value);
    }

    public boolean isShowProxy() {
        return getBoolean(ConfigKey.SHOW_PROXY, true);
    }

    public void setShowProxy(boolean value) {
        setBoolean(ConfigKey.SHOW_PROXY, value);
    }

    public boolean isShowRepeater() {
        return getBoolean(ConfigKey.SHOW_REPEATER, true);
    }

    public void setShowRepeater(boolean value) {
        setBoolean(ConfigKey.SHOW_REPEATER, value);
    }

    public boolean isShowIntruder() {
        return getBoolean(ConfigKey.SHOW_INTRUDER, true);
    }

    public void setShowIntruder(boolean value) {
        setBoolean(ConfigKey.SHOW_INTRUDER, value);
    }

    public boolean isShowWebSockets() {
        return getBoolean(ConfigKey.SHOW_WEBSOCKETS, true);
    }

    public void setShowWebSockets(boolean value) {
        setBoolean(ConfigKey.SHOW_WEBSOCKETS, value);
    }

    public boolean isShowSiteMap() {
        return getBoolean(ConfigKey.SHOW_SITEMAP, true);
    }

    public void setShowSiteMap(boolean value) {
        setBoolean(ConfigKey.SHOW_SITEMAP, value);
    }

    public boolean isShowScope() {
        return getBoolean(ConfigKey.SHOW_SCOPE, true);
    }

    public void setShowScope(boolean value) {
        setBoolean(ConfigKey.SHOW_SCOPE, value);
    }

    public boolean isShowCollaborator() {
        return getBoolean(ConfigKey.SHOW_COLLABORATOR, true);
    }

    public void setShowCollaborator(boolean value) {
        setBoolean(ConfigKey.SHOW_COLLABORATOR, value);
    }

    public boolean isDebugMode() {
        return getBoolean(ConfigKey.DEBUG_MODE, false);
    }

    public void setDebugMode(boolean value) {
        setBoolean(ConfigKey.DEBUG_MODE, value);
    }

    public String getCustomState() {
        return preferences.getString(ConfigKey.CUSTOM_STATE.getKey());
    }

    public void setCustomState(String value) {
        preferences.setString(ConfigKey.CUSTOM_STATE.getKey(), value);
    }

    public boolean isRpcEnabled() {
        return getBoolean(ConfigKey.RPC_ENABLED, true);
    }

    public void setRpcEnabled(boolean value) {
        setBoolean(ConfigKey.RPC_ENABLED, value);
    }

    private boolean getBoolean(ConfigKey key, boolean def) {
        Boolean val = preferences.getBoolean(key.getKey());
        return val == null ? def : val;
    }

    private void setBoolean(ConfigKey key, boolean value) {
        preferences.setBoolean(key.getKey(), value);
    }
}
