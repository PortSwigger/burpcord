package tech.chron0.burpcord.listeners.providers;

import tech.chron0.burpcord.config.BurpcordConfig;
import tech.chron0.burpcord.discord.ActivityProvider;
import tech.chron0.burpcord.listeners.BurpComponent;
import tech.chron0.burpcord.ui.BurpcordSettingsTab;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.collaborator.CollaboratorClient;
import burp.api.montoya.collaborator.Interaction;

import com.jagrosh.discordipc.entities.RichPresence;

import java.util.List;

/**
 * <h1>Collaborator Provider</h1>
 * <p>
 * Tracks real Burp Collaborator interactions via the Montoya API.
 * Creates a CollaboratorClient to poll for DNS, HTTP, and SMTP interactions.
 * </p>
 * <p>
 * <strong>Note:</strong> Collaborator is a Burp Suite Professional feature.
 * On Community Edition, this provider gracefully degrades and stays inactive.
 * </p>
 *
 * @author Jon Marien
 */
public class BurpcordCollaboratorProvider implements ActivityProvider, BurpComponent {

    private final BurpcordConfig config;
    private CollaboratorClient client;
    private boolean collaboratorAvailable = false;

    // Caching
    private int cachedInteractionCount = 0;
    private String cachedBreakdown = "";
    private long lastPollTime = 0;
    private static final long POLL_TTL_MS = 60000; // 60 seconds

    public BurpcordCollaboratorProvider(MontoyaApi api, BurpcordConfig config) {
        this.config = config;
    }

    @Override
    public void register(MontoyaApi api) {
        try {
            this.client = api.collaborator().createClient();
            this.collaboratorAvailable = true;
            BurpcordSettingsTab.log("Collaborator client created successfully (Pro edition detected).");
        } catch (Exception e) {
            this.collaboratorAvailable = false;
            BurpcordSettingsTab.log("Collaborator not available (Community Edition or disabled).");
        }
    }

    @Override
    public boolean isActive() {
        if (!config.isShowCollaborator() || !collaboratorAvailable) {
            return false;
        }

        // Poll with cache TTL
        long now = System.currentTimeMillis();
        if (now - lastPollTime > POLL_TTL_MS) {
            try {
                List<Interaction> interactions = client.getAllInteractions();
                cachedInteractionCount += interactions.size();

                // Build type breakdown from this poll's results
                if (!interactions.isEmpty()) {
                    long dns = interactions.stream()
                            .filter(i -> i.type().name().equals("DNS"))
                            .count();
                    long http = interactions.stream()
                            .filter(i -> i.type().name().equals("HTTP"))
                            .count();
                    long smtp = interactions.stream()
                            .filter(i -> i.type().name().equals("SMTP"))
                            .count();

                    StringBuilder sb = new StringBuilder();
                    if (dns > 0)
                        sb.append("DNS: ").append(dns);
                    if (http > 0) {
                        if (sb.length() > 0)
                            sb.append(", ");
                        sb.append("HTTP: ").append(http);
                    }
                    if (smtp > 0) {
                        if (sb.length() > 0)
                            sb.append(", ");
                        sb.append("SMTP: ").append(smtp);
                    }
                    cachedBreakdown = sb.toString();
                }

                lastPollTime = now;
            } catch (Exception e) {
                // Collaborator may have been disabled mid-session
                lastPollTime = now;
            }
        }

        // Show as active if Collaborator is available (even with 0 interactions)
        return true;
    }

    @Override
    public void updatePresence(RichPresence.Builder builder) {
        builder.setDetails("Burp Collaborator");

        if (cachedInteractionCount > 0) {
            String state = "Interactions: " + cachedInteractionCount;
            if (!cachedBreakdown.isEmpty()) {
                state += " (" + cachedBreakdown + ")";
            }
            builder.setState(state);
        } else {
            builder.setState("Awaiting Interactions");
        }

        builder.setSmallImageWithTooltip("collaborator", "Collaborator");
    }

    @Override
    public int getPriority() {
        return 80;
    }
}
