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
 * <h1>Burpcord Intruder Listener</h1>
 * <p>
 * Monitors Burp Intruder activity for fuzzing and brute-force attacks.
 * Tracks the number of attack requests sent.
 * </p>
 * 
 * @author Jon Marien
 * @version 2.2.0
 */
public class BurpcordIntruderListener implements HttpHandler, ActivityProvider, BurpComponent {

    private final BurpcordConfig config;
    private final AtomicInteger attackCount = new AtomicInteger(0);
    private long lastActivityTime = 0;

    public BurpcordIntruderListener(BurpcordConfig config) {
        this.config = config;
    }

    @Override
    public RequestToBeSentAction handleHttpRequestToBeSent(HttpRequestToBeSent requestToBeSent) {
        if (requestToBeSent.toolSource().isFromTool(ToolType.INTRUDER)) {
            attackCount.incrementAndGet();
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
        if (!config.isShowIntruder())
            return false;
        return (System.currentTimeMillis() - lastActivityTime) < 60000;
    }

    @Override
    public void updatePresence(RichPresence.Builder builder) {
        builder.setDetails("Fuzzing / Brute Forcing");
        builder.setState("Requests sent: " + attackCount.get());
        builder.setSmallImageWithTooltip("intruder", "Intruder");
    }

    @Override
    public void register(MontoyaApi api) {
        api.http().registerHttpHandler(this);
    }

    @Override
    public int getPriority() {
        return 30;
    }
}
