package tech.chron0.burpcord.listeners.providers;

import tech.chron0.burpcord.config.BurpcordConfig;
import tech.chron0.burpcord.discord.ActivityProvider;

import burp.api.montoya.MontoyaApi;
import com.jagrosh.discordipc.entities.RichPresence;

/**
 * <h1>Site Map Provider</h1>
 * <p>
 * Reports the current size of the Burp Suite Site Map.
 * Includes caching to prevent performance issues with large projects.
 * </p>
 * 
 * @author Jon Marien
 */
public class BurpcordSiteMapProvider implements ActivityProvider {

    private final MontoyaApi api;
    private final BurpcordConfig config;

    // Caching
    private int cachedCount = 0;
    private long lastUpdate = 0;
    private static final long CACHE_TTL_MS = 60000; // 60 seconds

    public BurpcordSiteMapProvider(MontoyaApi api, BurpcordConfig config) {
        this.api = api;
        this.config = config;
    }

    @Override
    public boolean isActive() {
        if (!config.isShowSiteMap())
            return false;

        // Update cache if expired
        long now = System.currentTimeMillis();
        if (now - lastUpdate > CACHE_TTL_MS) {
            try {
                cachedCount = api.siteMap().requestResponses().size();
                lastUpdate = now;
            } catch (Exception e) {
                // Fail safe
                cachedCount = 0;
            }
        }

        return cachedCount > 0;
    }

    @Override
    public void updatePresence(RichPresence.Builder builder) {
        // Use cached value
        builder.setDetails("Site Map");
        builder.setState(cachedCount + " items");
        builder.setSmallImageWithTooltip("target", "Site Map");
    }

    @Override
    public int getPriority() {
        return 60;
    }
}
