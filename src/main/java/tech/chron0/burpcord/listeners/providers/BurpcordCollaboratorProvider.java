package tech.chron0.burpcord.listeners.providers;

import tech.chron0.burpcord.config.BurpcordConfig;
import tech.chron0.burpcord.discord.ActivityProvider;

import burp.api.montoya.MontoyaApi;
import com.jagrosh.discordipc.entities.RichPresence;

/**
 * <h1>Collaborator Provider</h1>
 * <p>
 * Indicates activity related to Burp Collaborator interactions.
 * </p>
 * 
 * @author Jon Marien
 * @version 2.2.0
 */
public class BurpcordCollaboratorProvider implements ActivityProvider {

    private final MontoyaApi api;
    private final BurpcordConfig config;

    public BurpcordCollaboratorProvider(MontoyaApi api, BurpcordConfig config) {
        this.api = api;
        this.config = config;
    }

    @Override
    public boolean isActive() {
        return config.isShowCollaborator();
    }

    @Override
    public void updatePresence(RichPresence.Builder builder) {
        builder.setDetails("Burp Collaborator");
        builder.setState("Tracking Interactions");
        builder.setSmallImageWithTooltip("collaborator", "Collaborator");
    }

    @Override
    public int getPriority() {
        return 80;
    }
}
