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

        manager = new DiscordRPCManager(api);
        BurpcordProxyHandler proxyHandler = new BurpcordProxyHandler(manager);

        api.proxy().registerRequestHandler(proxyHandler);
        api.proxy().registerResponseHandler(proxyHandler);

        if (BurpcordConfig.ENABLE_RPC) {
            manager.initialize();
        }
    }

    @Override
    public void extensionUnloaded() {
        if (manager != null) {
            manager.shutdown();
        }
    }
}
