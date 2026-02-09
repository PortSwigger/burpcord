package tech.chron0.burpcord.listeners;

import tech.chron0.burpcord.config.BurpcordConfig;
import tech.chron0.burpcord.discord.ActivityProvider;

import burp.api.montoya.core.ToolType;

import burp.api.montoya.http.handler.HttpHandler;
import burp.api.montoya.http.handler.HttpRequestToBeSent;
import burp.api.montoya.http.handler.HttpResponseReceived;
import burp.api.montoya.http.handler.RequestToBeSentAction;
import burp.api.montoya.http.handler.ResponseReceivedAction;

import com.jagrosh.discordipc.entities.RichPresence;
import java.util.concurrent.atomic.AtomicInteger;

import burp.api.montoya.MontoyaApi;

/**
 * <h1>Burpcord Repeater Listener</h1>
 * <p>
 * Listens for HTTP requests made via Burp Repeater.
 * Updates Rich Presence with request details.
 * </p>
 * 
 * @author Jon Marien
 * @version 2.3.0
 */
public class BurpcordRepeaterListener implements HttpHandler, ActivityProvider, BurpComponent {

    private final BurpcordConfig config;
    private final AtomicInteger requestCount = new AtomicInteger(0);
    private long lastActivityTime = 0;

    public BurpcordRepeaterListener(BurpcordConfig config) {
        this.config = config;
    }

    @Override
    public RequestToBeSentAction handleHttpRequestToBeSent(HttpRequestToBeSent requestToBeSent) {
        if (requestToBeSent.toolSource().isFromTool(ToolType.REPEATER)) {
            requestCount.incrementAndGet();
            lastActivityTime = System.currentTimeMillis();
        }
        return RequestToBeSentAction.continueWith(requestToBeSent);
    }

    @Override
    public ResponseReceivedAction handleHttpResponseReceived(HttpResponseReceived responseReceived) {
        return ResponseReceivedAction.continueWith(responseReceived);
    }

    @Override
    public boolean isActive() {
        if (!config.isShowRepeater())
            return false;
        return (System.currentTimeMillis() - lastActivityTime) < 60000;
    }

    @Override
    public void updatePresence(RichPresence.Builder builder) {
        builder.setDetails("Manual Testing (Repeater)");
        builder.setState("Requests sent: " + requestCount.get());
        builder.setSmallImageWithTooltip("repeater", "Repeater");
    }

    @Override
    public void register(MontoyaApi api) {
        api.http().registerHttpHandler(this);
    }

    @Override
    public int getPriority() {
        return 40;
    }
}
