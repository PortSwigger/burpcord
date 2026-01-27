package com.burpcord;

import burp.api.montoya.proxy.http.ProxyRequestHandler;
import burp.api.montoya.proxy.http.ProxyRequestReceivedAction;
import burp.api.montoya.proxy.http.ProxyRequestToBeSentAction;
import burp.api.montoya.proxy.http.ProxyResponseHandler;
import burp.api.montoya.proxy.http.ProxyResponseReceivedAction;
import burp.api.montoya.proxy.http.ProxyResponseToBeSentAction;
import burp.api.montoya.proxy.http.InterceptedRequest;
import burp.api.montoya.proxy.http.InterceptedResponse;

/**
 * Handles Burp Suite Proxy events for Discord Rich Presence updates.
 * 
 * <p>
 * This handler tracks HTTP requests and responses flowing through the
 * Proxy tool. It updates the {@link DiscordRPCManager} with request/response
 * counts and intercept status to display proxy activity in Discord.
 * </p>
 * 
 * @author Jon Marien
 * @version 1.3
 * @see DiscordRPCManager
 */
public class BurpcordProxyHandler implements ProxyRequestHandler, ProxyResponseHandler {

    /** Reference to the RPC manager for status updates. */
    private final DiscordRPCManager manager;

    /**
     * Creates a new proxy handler.
     * 
     * @param manager The Discord RPC manager to notify of proxy activity
     */
    public BurpcordProxyHandler(DiscordRPCManager manager) {
        this.manager = manager;
    }

    @Override
    public ProxyRequestReceivedAction handleRequestReceived(InterceptedRequest interceptedRequest) {
        manager.incrementRequestCount();
        manager.setIntercepting(true);
        return ProxyRequestReceivedAction.continueWith(interceptedRequest);
    }

    @Override
    public ProxyRequestToBeSentAction handleRequestToBeSent(InterceptedRequest interceptedRequest) {
        return ProxyRequestToBeSentAction.continueWith(interceptedRequest);
    }

    @Override
    public ProxyResponseReceivedAction handleResponseReceived(InterceptedResponse interceptedResponse) {
        manager.incrementResponseCount();
        manager.setIntercepting(true);
        return ProxyResponseReceivedAction.continueWith(interceptedResponse);
    }

    @Override
    public ProxyResponseToBeSentAction handleResponseToBeSent(InterceptedResponse interceptedResponse) {
        return ProxyResponseToBeSentAction.continueWith(interceptedResponse);
    }
}
