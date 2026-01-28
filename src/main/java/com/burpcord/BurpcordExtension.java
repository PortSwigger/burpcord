package com.burpcord;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.extension.ExtensionUnloadingHandler;

/**
 * Main entry point for the Burpcord Burp Suite extension.
 * 
 * <p>
 * This extension integrates Discord Rich Presence with Burp Suite, allowing
 * security researchers to display their current activity on their Discord
 * profile.
 * It tracks various Burp Suite tools including Proxy, Scanner, Repeater,
 * Intruder,
 * and WebSockets.
 * </p>
 * 
 * <p>
 * The extension registers multiple event handlers to track tool usage and
 * periodically updates the Discord Rich Presence with relevant status
 * information.
 * </p>
 * 
 * @author Jon Marien
 * @version 1.3
 * @see DiscordRPCManager
 * @see BurpcordConfig
 */
public class BurpcordExtension implements BurpExtension, ExtensionUnloadingHandler {

    /** The Discord RPC manager responsible for all Discord communication. */
    private DiscordRPCManager manager;

    /**
     * Initializes the Burpcord extension when loaded by Burp Suite.
     * 
     * <p>
     * This method performs the following setup:
     * </p>
     * <ul>
     * <li>Sets the extension name to "Burpcord"</li>
     * <li>Registers the unloading handler for cleanup</li>
     * <li>Creates the configuration and RPC manager instances</li>
     * <li>Registers handlers for Proxy, Scanner, Repeater, Intruder, and
     * WebSockets</li>
     * <li>Adds the Burpcord settings tab to the UI</li>
     * <li>Initializes the Discord RPC connection</li>
     * </ul>
     * 
     * @param api The Montoya API instance provided by Burp Suite
     */
    @Override
    public void initialize(MontoyaApi api) {
        api.extension().setName("Burpcord");
        api.extension().registerUnloadingHandler(this);

        api.logging().logToOutput("Loading Burpcord...");

        BurpcordConfig config = new BurpcordConfig(api.persistence().preferences());

        // Log startup to built-in log viewer
        BurpcordSettingsTab.log("Loading Burpcord...");
        manager = new DiscordRPCManager(api, config);

        // Register Proxy handler for request/response tracking
        BurpcordProxyHandler proxyHandler = new BurpcordProxyHandler(manager);
        api.proxy().registerRequestHandler(proxyHandler);
        api.proxy().registerResponseHandler(proxyHandler);

        // Register Scanner listener for scan activity and vulnerability counts
        BurpcordScannerListener scannerListener = new BurpcordScannerListener(manager);
        api.scanner().registerAuditIssueHandler(scannerListener);
        api.scanner().registerActiveScanCheck(scannerListener,
                burp.api.montoya.scanner.scancheck.ScanCheckType.PER_INSERTION_POINT);
        api.scanner().registerPassiveScanCheck(scannerListener,
                burp.api.montoya.scanner.scancheck.ScanCheckType.PER_REQUEST);

        // Register Repeater listener for manual testing activity
        BurpcordRepeaterListener repeaterListener = new BurpcordRepeaterListener(manager);
        api.http().registerHttpHandler(repeaterListener);

        // Register Intruder listener for attack tracking
        BurpcordIntruderListener intruderListener = new BurpcordIntruderListener(manager);
        api.http().registerHttpHandler(intruderListener);

        // Register WebSocket listener for message tracking
        BurpcordWebSocketListener webSocketListener = new BurpcordWebSocketListener(manager);
        api.websockets().registerWebSocketCreatedHandler(webSocketListener);

        // Register Settings Tab in Burp Suite UI
        api.userInterface().registerSuiteTab("Burpcord", new BurpcordSettingsTab(api, config, manager));

        // Log enabled features to built-in log viewer
        logEnabledFeatures(config);

        // Log setup hints
        BurpcordSettingsTab.log("──────────────────────────────────");
        BurpcordSettingsTab.log("ℹ️ Tip: Use toggles below to customize displayed stats");
        BurpcordSettingsTab.log("ℹ️ Tip: Set custom state text to personalize your status");
        BurpcordSettingsTab.log("──────────────────────────────────");
        BurpcordSettingsTab.log("Connecting to Discord IPC...");

        manager.initialize();
    }

    /**
     * Called when the extension is being unloaded from Burp Suite.
     * 
     * <p>
     * Ensures proper cleanup by shutting down the Discord RPC manager,
     * which stops the scheduler and closes the Discord IPC connection.
     * </p>
     */
    @Override
    public void extensionUnloaded() {
        if (manager != null) {
            manager.shutdown();
        }
    }

    /**
     * Logs enabled features to the built-in log viewer with fancy formatting.
     * 
     * @param config The configuration to check for enabled features
     */
    private void logEnabledFeatures(BurpcordConfig config) {
        BurpcordSettingsTab.log("┌─────────────────────────────────────┐");
        BurpcordSettingsTab.log("│      📊 ENABLED STATUS TRACKERS     │");
        BurpcordSettingsTab.log("├─────────────────────────────────────┤");

        // First row
        StringBuilder row1 = new StringBuilder("│ ");
        row1.append(config.isShowIntercept() ? "✓ Intercept  " : "✗ Intercept  ");
        row1.append(config.isShowScan() ? "✓ Scanner  " : "✗ Scanner  ");
        row1.append(config.isShowProxy() ? "✓ Proxy" : "✗ Proxy");
        BurpcordSettingsTab.log(row1.toString());

        // Second row
        StringBuilder row2 = new StringBuilder("│ ");
        row2.append(config.isShowRepeater() ? "✓ Repeater   " : "✗ Repeater   ");
        row2.append(config.isShowIntruder() ? "✓ Intruder " : "✗ Intruder ");
        row2.append(config.isShowSiteMap() ? "✓ SiteMap" : "✗ SiteMap");
        BurpcordSettingsTab.log(row2.toString());

        // Third row
        StringBuilder row3 = new StringBuilder("│ ");
        row3.append(config.isShowScope() ? "✓ Scope      " : "✗ Scope      ");
        row3.append(config.isShowCollaborator() ? "✓ Collab   " : "✗ Collab   ");
        row3.append(config.isShowWebSockets() ? "✓ WebSocket" : "✗ WebSocket");
        BurpcordSettingsTab.log(row3.toString());

        BurpcordSettingsTab.log("└─────────────────────────────────────┘");
    }
}
