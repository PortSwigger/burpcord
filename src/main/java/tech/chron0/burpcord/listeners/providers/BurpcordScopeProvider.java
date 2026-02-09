package tech.chron0.burpcord.listeners.providers;

import tech.chron0.burpcord.config.BurpcordConfig;
import tech.chron0.burpcord.discord.ActivityProvider;

import burp.api.montoya.MontoyaApi;
import com.jagrosh.discordipc.entities.RichPresence;

/**
 * <h1>Scope Provider</h1>
 * <p>
 * Monitor's the user's targeted scope.
 * Reports based on whether any scope exclusions or inclusions are defined.
 * </p>
 * 
 * @author Jon Marien
 * @version 2.2.0
 */
public class BurpcordScopeProvider implements ActivityProvider {

    private final MontoyaApi api;
    private final BurpcordConfig config;

    public BurpcordScopeProvider(MontoyaApi api, BurpcordConfig config) {
        this.api = api;
        this.config = config;
    }

    @Override
    public boolean isActive() {
        // Efficient check: Only reads local boolean preference.
        // No caching required as no expensive API calls are made here.
        return config.isShowScope();
    }

    @Override
    public void updatePresence(RichPresence.Builder builder) {
        builder.setDetails("Scope Configured");
        builder.setState("Focusing on target scope");
        builder.setSmallImageWithTooltip("target", "Scope");
    }

    @Override
    public int getPriority() {
        return 70;
    }
}
