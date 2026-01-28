package tech.chron0.burpcord.listeners;

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

/**
 * <h1>Proxy Listener</h1>
 * <p>
 * Monitors the Burp Suite Proxy tool for traffic activity.
 * Tracks the number of intercepted requests and responses passing through the
 * proxy.
 * </p>
 * 
 * @author Jon Marien
 * @version 2.0.0
 */
public class BurpcordProxyHandler implements ProxyRequestHandler, ProxyResponseHandler, ActivityProvider {

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
        builder.setSmallImage("proxy", "Proxy");
    }
}
