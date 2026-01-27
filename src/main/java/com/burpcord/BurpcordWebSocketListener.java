package com.burpcord;

import burp.api.montoya.websocket.BinaryMessage;
import burp.api.montoya.websocket.BinaryMessageAction;
import burp.api.montoya.websocket.MessageHandler;
import burp.api.montoya.websocket.TextMessage;
import burp.api.montoya.websocket.TextMessageAction;
import burp.api.montoya.websocket.WebSocketCreated;
import burp.api.montoya.websocket.WebSocketCreatedHandler;

/**
 * Listener for WebSocket activity to update Discord Rich Presence.
 */
public class BurpcordWebSocketListener implements WebSocketCreatedHandler {

    private final DiscordRPCManager manager;

    public BurpcordWebSocketListener(DiscordRPCManager manager) {
        this.manager = manager;
    }

    @Override
    public void handleWebSocketCreated(WebSocketCreated webSocketCreated) {
        // Register a message handler that tracks all WebSocket messages
        webSocketCreated.webSocket().registerMessageHandler(new MessageHandler() {
            @Override
            public TextMessageAction handleTextMessage(TextMessage textMessage) {
                manager.markWebSocketActivity();
                return TextMessageAction.continueWith(textMessage);
            }

            @Override
            public BinaryMessageAction handleBinaryMessage(BinaryMessage binaryMessage) {
                manager.markWebSocketActivity();
                return BinaryMessageAction.continueWith(binaryMessage);
            }
        });
    }
}
