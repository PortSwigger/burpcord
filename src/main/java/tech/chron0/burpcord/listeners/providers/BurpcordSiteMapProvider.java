package tech.chron0.burpcord.listeners.providers;

import tech.chron0.burpcord.config.BurpcordConfig;
import tech.chron0.burpcord.discord.ActivityProvider;
import tech.chron0.burpcord.listeners.BurpComponent;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.Registration;
import burp.api.montoya.proxy.http.InterceptedRequest;
import burp.api.montoya.proxy.http.ProxyRequestHandler;
import burp.api.montoya.proxy.http.ProxyRequestReceivedAction;
import burp.api.montoya.proxy.http.ProxyRequestToBeSentAction;

import com.jagrosh.discordipc.entities.RichPresence;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Site map style presence without loading the full site map on the Discord update path.
 * <p>
 * <strong>Primary (BApp / large projects):</strong> counts unique request URLs observed via
 * the Proxy using a bounded {@link Set} so memory stays capped.
 * </p>
 * <p>
 * <strong>Fallback:</strong> periodically (long interval) refreshes the true
 * {@code siteMap().requestResponses().size()} on a dedicated background thread, because Montoya
 * exposes no cheap size-only API (see PortSwigger feature request in project docs).
 * </p>
 */
public class BurpcordSiteMapProvider implements ActivityProvider, ProxyRequestHandler, BurpComponent {

    /** Max distinct URLs tracked from proxy traffic (memory bound). */
    private static final int UNIQUE_URL_CAP = 8192;

    /** First full site map scan after load (lets Burp finish startup). */
    private static final long INITIAL_TRUE_COUNT_DELAY_SEC = 45L;

    /** How often to run the expensive full site map count. */
    private static final long TRUE_COUNT_PERIOD_MINUTES = 30L;

    /** Prefer displaying true count if younger than this (slightly &gt; period). */
    private static final long TRUE_COUNT_DISPLAY_STALE_MS = TimeUnit.MINUTES.toMillis(34);

    private final MontoyaApi api;
    private final BurpcordConfig config;

    private final Set<String> seenUrls = ConcurrentHashMap.newKeySet();
    private volatile boolean estimateCapped = false;

    private volatile int trueSiteMapCount = 0;
    private volatile long lastTrueCountMs = 0L;

    private Registration proxyRegistration;
    private ScheduledExecutorService trueCountExecutor;

    public BurpcordSiteMapProvider(MontoyaApi api, BurpcordConfig config) {
        this.api = api;
        this.config = config;
    }

    @Override
    public void register(MontoyaApi api) {
        this.proxyRegistration = api.proxy().registerRequestHandler(this);
        this.trueCountExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "Burpcord-SiteMap-TrueCount");
            t.setDaemon(true);
            return t;
        });
        trueCountExecutor.scheduleWithFixedDelay(
                this::refreshTrueSiteMapCount,
                INITIAL_TRUE_COUNT_DELAY_SEC,
                TimeUnit.MINUTES.toSeconds(TRUE_COUNT_PERIOD_MINUTES),
                TimeUnit.SECONDS);
    }

    /**
     * Stops background site map counting and unregisters the proxy handler. Call from extension unload.
     */
    public void shutdown() {
        if (proxyRegistration != null) {
            proxyRegistration.deregister();
            proxyRegistration = null;
        }
        if (trueCountExecutor != null) {
            trueCountExecutor.shutdownNow();
            trueCountExecutor = null;
        }
    }

    private void refreshTrueSiteMapCount() {
        if (!config.isShowSiteMap()) {
            return;
        }
        try {
            int n = api.siteMap().requestResponses().size();
            trueSiteMapCount = n;
            lastTrueCountMs = System.currentTimeMillis();
        } catch (Exception e) {
            api.logging().logToError("Burpcord: full site map count failed: " + e.getMessage());
        }
    }

    private void recordUrlFromProxy(String url) {
        if (!config.isShowSiteMap()) {
            return;
        }
        if (estimateCapped) {
            return;
        }
        if (seenUrls.size() >= UNIQUE_URL_CAP) {
            estimateCapped = true;
            return;
        }
        seenUrls.add(url);
    }

    @Override
    public ProxyRequestReceivedAction handleRequestReceived(InterceptedRequest interceptedRequest) {
        recordUrlFromProxy(interceptedRequest.url());
        return ProxyRequestReceivedAction.continueWith(interceptedRequest);
    }

    @Override
    public ProxyRequestToBeSentAction handleRequestToBeSent(InterceptedRequest interceptedRequest) {
        return ProxyRequestToBeSentAction.continueWith(interceptedRequest);
    }

    @Override
    public boolean isActive() {
        if (!config.isShowSiteMap()) {
            return false;
        }
        if (estimateCapped || !seenUrls.isEmpty()) {
            return true;
        }
        return trueSiteMapCount > 0 && lastTrueCountMs > 0L;
    }

    @Override
    public void updatePresence(RichPresence.Builder builder) {
        builder.setDetails("Site Map");
        builder.setState(buildStateLine());
        builder.setSmallImageWithTooltip("target", "Site Map");
    }

    private String buildStateLine() {
        long now = System.currentTimeMillis();
        boolean trueFresh = lastTrueCountMs > 0L && (now - lastTrueCountMs) < TRUE_COUNT_DISPLAY_STALE_MS;

        if (trueFresh) {
            return trueSiteMapCount + " site map items";
        }

        int est = seenUrls.size();
        if (estimateCapped) {
            return "\u2265" + UNIQUE_URL_CAP + " unique URLs (proxy)";
        }
        if (est > 0) {
            return "~" + est + " unique URLs (proxy)";
        }
        if (trueSiteMapCount > 0) {
            return trueSiteMapCount + " site map items (last full scan)";
        }
        return "0 items";
    }

    @Override
    public int getPriority() {
        return 60;
    }
}
