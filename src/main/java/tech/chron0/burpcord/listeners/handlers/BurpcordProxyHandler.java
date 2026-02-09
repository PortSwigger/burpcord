package tech.chron0.burpcord.listeners.handlers;

import tech.chron0.burpcord.config.BurpcordConfig;
import tech.chron0.burpcord.discord.ActivityProvider;

import burp.api.montoya.proxy.http.ProxyRequestHandler;
import burp.api.montoya.proxy.http.ProxyResponseHandler;
import burp.api.montoya.proxy.http.InterceptedRequest;
import burp.api.montoya.proxy.http.InterceptedResponse;
import burp.api.montoya.proxy.http.ProxyRequestReceivedAction;
import burp.api.montoya.proxy.http.ProxyRequestToBeSentAction;
import burp.api.montoya.proxy.http.ProxyResponseReceivedAction;
import burp.api.montoya.proxy.http.ProxyResponseToBeSentAction;

import com.jagrosh.discordipc.entities.RichPresence;
import java.util.concurrent.atomic.AtomicInteger;

import burp.api.montoya.MontoyaApi;
import tech.chron0.burpcord.listeners.BurpComponent;

/**
 * <h1>Burpcord Proxy Handler</h1>
 * <p>
 * Handles HTTP requests and responses intercepted by the Burp Proxy.
 * Updates Rich Presence with details about the intercepted item.
 * </p>
 * 
 * @author Jon Marien
 * @version 2.3.0
 */
public class BurpcordProxyHandler
        implements ProxyRequestHandler, ProxyResponseHandler, ActivityProvider, BurpComponent {

    private final BurpcordConfig config;
    private final AtomicInteger requestCount = new AtomicInteger(0);
    private long lastActivityTime = 0;

    public BurpcordProxyHandler(BurpcordConfig config) {
        this.config = config;
    }

    @Override
    public ProxyRequestReceivedAction handleRequestReceived(InterceptedRequest interceptedRequest) {
        requestCount.incrementAndGet();
        lastActivityTime = System.currentTimeMillis();
        return ProxyRequestReceivedAction.continueWith(interceptedRequest);
    }

    @Override
    public ProxyRequestToBeSentAction handleRequestToBeSent(InterceptedRequest interceptedRequest) {
        return ProxyRequestToBeSentAction.continueWith(interceptedRequest);
    }

    @Override
    public ProxyResponseReceivedAction handleResponseReceived(InterceptedResponse interceptedResponse) {
        return ProxyResponseReceivedAction.continueWith(interceptedResponse);
    }

    @Override
    public ProxyResponseToBeSentAction handleResponseToBeSent(InterceptedResponse interceptedResponse) {
        return ProxyResponseToBeSentAction.continueWith(interceptedResponse);
    }

    @Override
    public boolean isActive() {
        if (!config.isShowProxy())
            return false;
        // Consider active if traffic seen in last 30 seconds
        // Efficient calculation based on local timestamp.
        return (System.currentTimeMillis() - lastActivityTime) < 30000;
    }

    @Override
    public void updatePresence(RichPresence.Builder builder) {
        builder.setDetails("Proxying Traffic");
        builder.setState("Requests: " + requestCount.get());
        builder.setSmallImageWithTooltip("proxy", "Proxy");
    }

    @Override
    public void register(MontoyaApi api) {
        api.proxy().registerRequestHandler(this);
        api.proxy().registerResponseHandler(this);
    }

    @Override
    public int getPriority() {
        return 10;
    }
}
