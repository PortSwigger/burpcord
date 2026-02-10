package tech.chron0.burpcord.listeners.providers;

import tech.chron0.burpcord.config.BurpcordConfig;
import tech.chron0.burpcord.discord.ActivityProvider;
import tech.chron0.burpcord.listeners.BurpComponent;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.scope.ScopeChangeHandler;
import burp.api.montoya.scope.ScopeChange;

import com.jagrosh.discordipc.entities.RichPresence;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * <h1>Scope Provider</h1>
 * <p>
 * Tracks the user's target scope via the Montoya Scope API.
 * Registers a ScopeChangeHandler to count real scope modifications.
 * </p>
 * <p>
 * <strong>Note:</strong> The Montoya API does not provide a method to list
 * or count scope items, only to query individual URLs and listen for changes.
 * </p>
 *
 * @author Jon Marien
 */
public class BurpcordScopeProvider implements ActivityProvider, ScopeChangeHandler, BurpComponent {

    private final BurpcordConfig config;
    private final AtomicInteger scopeChangeCount = new AtomicInteger(0);

    public BurpcordScopeProvider(MontoyaApi api, BurpcordConfig config) {
        this.config = config;
    }

    @Override
    public void register(MontoyaApi api) {
        api.scope().registerScopeChangeHandler(this);
    }

    @Override
    public void scopeChanged(ScopeChange scopeChange) {
        scopeChangeCount.incrementAndGet();
    }

    @Override
    public boolean isActive() {
        return config.isShowScope() && scopeChangeCount.get() > 0;
    }

    @Override
    public void updatePresence(RichPresence.Builder builder) {
        int changes = scopeChangeCount.get();
        builder.setDetails("Target Scope Active");
        builder.setState("Scope changes: " + changes);
        builder.setSmallImageWithTooltip("target", "Scope");
    }

    @Override
    public int getPriority() {
        return 70;
    }
}
