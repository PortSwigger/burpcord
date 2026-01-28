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
 * @version 2.0.0
 */
public class BurpcordConfig {

    private final burp.api.montoya.persistence.Preferences preferences;

    // Preference keys
    private static final String KEY_APP_ID = "burpcord_app_id";
    private static final String KEY_UPDATE_INTERVAL = "burpcord_update_interval";
    private static final String KEY_SHOW_INTERCEPT = "burpcord_show_intercept";
    private static final String KEY_SHOW_SCAN = "burpcord_show_scan";
    private static final String KEY_SHOW_PROXY = "burpcord_show_proxy";
    private static final String KEY_SHOW_REPEATER = "burpcord_show_repeater";
    private static final String KEY_SHOW_INTRUDER = "burpcord_show_intruder";
    private static final String KEY_SHOW_WEBSOCKETS = "burpcord_show_websockets";
    private static final String KEY_SHOW_SITEMAP = "burpcord_show_sitemap";
    private static final String KEY_SHOW_SCOPE = "burpcord_show_scope";
    private static final String KEY_SHOW_COLLABORATOR = "burpcord_show_collaborator";
    private static final String KEY_DEBUG_MODE = "burpcord_debug_mode";
    private static final String KEY_CUSTOM_STATE = "burpcord_custom_state";
    private static final String KEY_RPC_ENABLED = "burpcord_rpc_enabled";

    // Defaults
    private static final String DEFAULT_APP_ID = "1328087961230639207";
    private static final int DEFAULT_UPDATE_INTERVAL = 30;

    public BurpcordConfig(burp.api.montoya.persistence.Preferences preferences) {
        this.preferences = preferences;
    }

    public String getAppId() {
        String id = preferences.getString(KEY_APP_ID);
        return (id == null || id.isEmpty()) ? DEFAULT_APP_ID : id;
    }

    public void setAppId(String appId) {
        preferences.setString(KEY_APP_ID, appId);
    }

    public int getUpdateInterval() {
        Integer val = preferences.getInteger(KEY_UPDATE_INTERVAL);
        return (val == null) ? DEFAULT_UPDATE_INTERVAL : val;
    }

    public void setUpdateInterval(int seconds) {
        preferences.setInteger(KEY_UPDATE_INTERVAL, seconds);
    }

    public boolean isShowIntercept() {
        return getBoolean(KEY_SHOW_INTERCEPT, true);
    }

    public void setShowIntercept(boolean value) {
        setBoolean(KEY_SHOW_INTERCEPT, value);
    }

    public boolean isShowScan() {
        return getBoolean(KEY_SHOW_SCAN, true);
    }

    public void setShowScan(boolean value) {
        setBoolean(KEY_SHOW_SCAN, value);
    }

    public boolean isShowProxy() {
        return getBoolean(KEY_SHOW_PROXY, true);
    }

    public void setShowProxy(boolean value) {
        setBoolean(KEY_SHOW_PROXY, value);
    }

    public boolean isShowRepeater() {
        return getBoolean(KEY_SHOW_REPEATER, true);
    }

    public void setShowRepeater(boolean value) {
        setBoolean(KEY_SHOW_REPEATER, value);
    }

    public boolean isShowIntruder() {
        return getBoolean(KEY_SHOW_INTRUDER, true);
    }

    public void setShowIntruder(boolean value) {
        setBoolean(KEY_SHOW_INTRUDER, value);
    }

    public boolean isShowWebSockets() {
        return getBoolean(KEY_SHOW_WEBSOCKETS, true);
    }

    public void setShowWebSockets(boolean value) {
        setBoolean(KEY_SHOW_WEBSOCKETS, value);
    }

    public boolean isShowSiteMap() {
        return getBoolean(KEY_SHOW_SITEMAP, true);
    }

    public void setShowSiteMap(boolean value) {
        setBoolean(KEY_SHOW_SITEMAP, value);
    }

    public boolean isShowScope() {
        return getBoolean(KEY_SHOW_SCOPE, true);
    }

    public void setShowScope(boolean value) {
        setBoolean(KEY_SHOW_SCOPE, value);
    }

    public boolean isShowCollaborator() {
        return getBoolean(KEY_SHOW_COLLABORATOR, true);
    }

    public void setShowCollaborator(boolean value) {
        setBoolean(KEY_SHOW_COLLABORATOR, value);
    }

    public boolean isDebugMode() {
        return getBoolean(KEY_DEBUG_MODE, false);
    }

    public void setDebugMode(boolean value) {
        setBoolean(KEY_DEBUG_MODE, value);
    }

    public String getCustomState() {
        return preferences.getString(KEY_CUSTOM_STATE);
    }

    public void setCustomState(String value) {
        preferences.setString(KEY_CUSTOM_STATE, value);
    }

    public boolean isRpcEnabled() {
        return getBoolean(KEY_RPC_ENABLED, true);
    }

    public void setRpcEnabled(boolean value) {
        setBoolean(KEY_RPC_ENABLED, value);
    }

    private boolean getBoolean(String key, boolean def) {
        Boolean val = preferences.getBoolean(key);
        return val == null ? def : val;
    }

    private void setBoolean(String key, boolean value) {
        preferences.setBoolean(key, value);
    }
}
