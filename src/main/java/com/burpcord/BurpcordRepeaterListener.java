package com.burpcord;

import burp.api.montoya.core.ToolSource;
import burp.api.montoya.core.ToolType;
import burp.api.montoya.http.handler.HttpHandler;
import burp.api.montoya.http.handler.HttpRequestToBeSent;
import burp.api.montoya.http.handler.RequestToBeSentAction;
import burp.api.montoya.http.handler.HttpResponseReceived;
import burp.api.montoya.http.handler.ResponseReceivedAction;

public class BurpcordRepeaterListener implements HttpHandler {

    private final DiscordRPCManager manager;

    public BurpcordRepeaterListener(DiscordRPCManager manager) {
        this.manager = manager;
    }

    @Override
    public RequestToBeSentAction handleHttpRequestToBeSent(HttpRequestToBeSent requestToBeSent) {
        ToolSource toolSource = requestToBeSent.toolSource();
        if (toolSource.isFromTool(ToolType.REPEATER)) {
            manager.markRepeaterActivity();
        }
        return RequestToBeSentAction.continueWith(requestToBeSent);
    }

    @Override
    public ResponseReceivedAction handleHttpResponseReceived(HttpResponseReceived responseReceived) {
        return ResponseReceivedAction.continueWith(responseReceived);
    }
}
