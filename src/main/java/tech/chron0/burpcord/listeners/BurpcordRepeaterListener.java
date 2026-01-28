package tech.chron0.burpcord.listeners;

import tech.chron0.burpcord.discord.DiscordRPCManager;

import burp.api.montoya.core.ToolSource;
import burp.api.montoya.core.ToolType;
import burp.api.montoya.http.handler.HttpHandler;
import burp.api.montoya.http.handler.HttpRequestToBeSent;
import burp.api.montoya.http.handler.RequestToBeSentAction;
import burp.api.montoya.http.handler.HttpResponseReceived;
import burp.api.montoya.http.handler.ResponseReceivedAction;

/**
 * Handles Burp Suite Repeater events for Discord Rich Presence updates.
 * 
 * <p>
 * This handler detects HTTP requests originating from the Repeater tool
 * and notifies the {@link DiscordRPCManager} of manual testing activity.
 * </p>
 * 
 * <p>
 * Repeater activity is tracked with a 60-second timeout, so the status
 * will automatically clear after inactivity.
 * </p>
 * 
 * @author Jon Marien
 * @version 1.3
 * @see DiscordRPCManager
 */
public class BurpcordRepeaterListener implements HttpHandler {

    /** Reference to the RPC manager for status updates. */
    private final DiscordRPCManager manager;

    /**
     * Creates a new Repeater listener.
     * 
     * @param manager The Discord RPC manager to notify of Repeater activity
     */
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
