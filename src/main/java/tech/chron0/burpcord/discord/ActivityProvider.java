package tech.chron0.burpcord.discord;

import com.jagrosh.discordipc.entities.RichPresence;

/**
 * <h1>Activity Provider Interface</h1>
 * <p>
 * Defines a contract for components that can provide status updates to the
 * Discord Rich Presence.
 * Components implementing this interface act as sources of truth for specific
 * Burp Suite activities
 * (e.g., scanning, proxying, spidering).
 * </p>
 * 
 * @author Jon Marien
 */
public interface ActivityProvider {

    /**
     * Checks if this provider has an active status to report.
     * 
     * @return {@code true} if the provider is currently active, {@code false}
     *         otherwise.
     */
    boolean isActive();

    /**
     * Updates the Rich Presence builder with details specific to this provider.
     * 
     * @param builder The {@link RichPresence.Builder} to modify.
     */
    void updatePresence(RichPresence.Builder builder);

    /**
     * Gets the priority of this provider. Lower values indicate higher priority.
     * Default is 100.
     * 
     * @return The priority value.
     */
    default int getPriority() {
        return 100;
    }
}
