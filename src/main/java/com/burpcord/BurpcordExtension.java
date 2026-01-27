package com.burpcord;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.extension.ExtensionUnloadingHandler;

public class BurpcordExtension implements BurpExtension, ExtensionUnloadingHandler {

    private DiscordRPCManager manager;

    @Override
    public void initialize(MontoyaApi api) {
        api.extension().setName("Burpcord");
        api.extension().registerUnloadingHandler(this);

        api.logging().logToOutput("Loading Burpcord...");

        BurpcordConfig config = new BurpcordConfig(api.persistence().preferences());
        manager = new DiscordRPCManager(api, config);

        BurpcordProxyHandler proxyHandler = new BurpcordProxyHandler(manager);
        api.proxy().registerRequestHandler(proxyHandler);
        api.proxy().registerResponseHandler(proxyHandler);

        BurpcordScannerListener scannerListener = new BurpcordScannerListener(manager);
        api.scanner().registerAuditIssueHandler(scannerListener);
        api.scanner().registerActiveScanCheck(scannerListener,
                burp.api.montoya.scanner.scancheck.ScanCheckType.PER_INSERTION_POINT);
        api.scanner().registerPassiveScanCheck(scannerListener,
                burp.api.montoya.scanner.scancheck.ScanCheckType.PER_REQUEST);

        BurpcordRepeaterListener repeaterListener = new BurpcordRepeaterListener(manager);
        api.http().registerHttpHandler(repeaterListener);

        BurpcordIntruderListener intruderListener = new BurpcordIntruderListener(manager);
        api.http().registerHttpHandler(intruderListener);

        // Register WebSocket listener for activity tracking
        BurpcordWebSocketListener webSocketListener = new BurpcordWebSocketListener(manager);
        api.websockets().registerWebSocketCreatedHandler(webSocketListener);

        // Register Settings Tab
        api.userInterface().registerSuiteTab("Burpcord", new BurpcordSettingsTab(api, config, manager));

        manager.initialize();

    }

    @Override
    public void extensionUnloaded() {
        if (manager != null) {
            manager.shutdown();
        }
    }
}
