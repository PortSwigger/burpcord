package tech.chron0.burpcord.listeners;

import tech.chron0.burpcord.discord.DiscordRPCManager;

import burp.api.montoya.core.ToolType;
import burp.api.montoya.http.handler.HttpHandler;
import burp.api.montoya.http.handler.HttpRequestToBeSent;
import burp.api.montoya.http.handler.HttpResponseReceived;
import burp.api.montoya.http.handler.RequestToBeSentAction;
import burp.api.montoya.http.handler.ResponseReceivedAction;

/**
 * Handles Burp Suite Intruder events for Discord Rich Presence updates.
 * 
 * <p>
 * This handler detects HTTP requests originating from the Intruder tool
 * during automated attacks and notifies the {@link DiscordRPCManager} of
 * attack activity with request counts.
 * </p>
 * 
 * <p>
 * Intruder activity is tracked with a 60-second timeout, so the status
 * will automatically clear after an attack completes or is stopped.
 * </p>
 * 
 * @author Jon Marien
 * @version 1.3
 * @see DiscordRPCManager
 */
public class BurpcordIntruderListener implements HttpHandler {

    /** Reference to the RPC manager for status updates. */
    private final DiscordRPCManager manager;

    /**
     * Creates a new Intruder listener.
     * 
     * @param manager The Discord RPC manager to notify of Intruder activity
     */
    public BurpcordIntruderListener(DiscordRPCManager manager) {
        this.manager = manager;
    }

    @Override
    public RequestToBeSentAction handleHttpRequestToBeSent(HttpRequestToBeSent requestToBeSent) {
        if (requestToBeSent.toolSource().isFromTool(ToolType.INTRUDER)) {
            manager.markIntruderActivity();
        }
        return RequestToBeSentAction.continueWith(requestToBeSent);
    }

    @Override
    public ResponseReceivedAction handleHttpResponseReceived(HttpResponseReceived responseReceived) {
        return ResponseReceivedAction.continueWith(responseReceived);
    }
}
