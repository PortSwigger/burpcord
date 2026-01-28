package tech.chron0.burpcord.listeners;

import tech.chron0.burpcord.config.BurpcordConfig;
import tech.chron0.burpcord.discord.ActivityProvider;

import burp.api.montoya.websocket.MessageHandler;
import burp.api.montoya.websocket.TextMessage;
import burp.api.montoya.websocket.TextMessageAction;
import burp.api.montoya.websocket.BinaryMessage;
import burp.api.montoya.websocket.BinaryMessageAction;
import burp.api.montoya.websocket.WebSocketCreated;
import burp.api.montoya.websocket.WebSocketCreatedHandler;

import com.jagrosh.discordipc.entities.RichPresence;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <h1>WebSocket Listener</h1>
 * <p>
 * Intercepts WebSocket creation and messages.
 * Tracks message volume and activity through WebSocket connections.
 * </p>
 * 
 * @author Jon Marien
 * @version 2.0.1
 */
public class BurpcordWebSocketListener implements WebSocketCreatedHandler, MessageHandler, ActivityProvider {

    private final BurpcordConfig config;
    private final AtomicInteger messageCount = new AtomicInteger(0);
    private long lastActivityTime = 0;

    public BurpcordWebSocketListener(BurpcordConfig config) {
        this.config = config;
    }

    @Override
    public void handleWebSocketCreated(WebSocketCreated webSocketCreated) {
        // Fix: register handler on the WebSocket object, not the event object
        webSocketCreated.webSocket().registerMessageHandler(this);
    }

    @Override
    public TextMessageAction handleTextMessage(TextMessage textMessage) {
        messageCount.incrementAndGet();
        lastActivityTime = System.currentTimeMillis();
        return TextMessageAction.continueWith(textMessage);
    }

    @Override
    public BinaryMessageAction handleBinaryMessage(BinaryMessage binaryMessage) {
        messageCount.incrementAndGet();
        lastActivityTime = System.currentTimeMillis();
        return BinaryMessageAction.continueWith(binaryMessage);
    }

    @Override
    public boolean isActive() {
        if (!config.isShowWebSockets())
            return false;
        return (System.currentTimeMillis() - lastActivityTime) < 30000;
    }

    @Override
    public void updatePresence(RichPresence.Builder builder) {
        builder.setDetails("WebSocket Traffic");
        builder.setState("Messages: " + messageCount.get());
        builder.setSmallImage("websocket", "WebSocket");
    }
}
