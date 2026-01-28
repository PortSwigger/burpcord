package tech.chron0.burpcord.listeners;

import tech.chron0.burpcord.discord.DiscordRPCManager;

import burp.api.montoya.websocket.BinaryMessage;
import burp.api.montoya.websocket.BinaryMessageAction;
import burp.api.montoya.websocket.MessageHandler;
import burp.api.montoya.websocket.TextMessage;
import burp.api.montoya.websocket.TextMessageAction;
import burp.api.montoya.websocket.WebSocketCreated;
import burp.api.montoya.websocket.WebSocketCreatedHandler;

/**
 * Handles WebSocket events for Discord Rich Presence updates.
 * 
 * <p>
 * This handler registers a message handler on each new WebSocket connection
 * to track both text and binary messages. It notifies the
 * {@link DiscordRPCManager}
 * of WebSocket activity to display message counts in Discord.
 * </p>
 * 
 * <p>
 * WebSocket activity is tracked with a 60-second timeout, so the status
 * will automatically clear after inactivity on all connections.
 * </p>
 * 
 * @author Jon Marien
 * @version 1.3
 * @see DiscordRPCManager
 */
public class BurpcordWebSocketListener implements WebSocketCreatedHandler {

    /** Reference to the RPC manager for status updates. */
    private final DiscordRPCManager manager;

    /**
     * Creates a new WebSocket listener.
     * 
     * @param manager The Discord RPC manager to notify of WebSocket activity
     */
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
