package tech.chron0.burpcord.discord;

import tech.chron0.burpcord.config.BurpcordConfig;
import tech.chron0.burpcord.core.BurpcordConstants;
import tech.chron0.burpcord.ui.BurpcordSettingsTab;

import com.jagrosh.discordipc.IPCClient;
import com.jagrosh.discordipc.IPCListener;
import com.jagrosh.discordipc.entities.Packet;
import com.jagrosh.discordipc.entities.RichPresence;
import com.jagrosh.discordipc.entities.User;
import com.jagrosh.discordipc.entities.pipe.PipeStatus;
import com.jagrosh.discordipc.entities.ActivityType;
import com.google.gson.JsonObject;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * <h1>Discord RPC Manager</h1>
 * <p>
 * Central controller for the Discord Rich Presence integration.
 * </p>
 * 
 * <h2>Functionality</h2>
 * <ul>
 * <li><b>Connection Handling:</b> Manages the lifecycle of the IPC connection
 * to the Discord client.</li>
 * <li><b>Activity Registry:</b> Maintains a prioritized list of
 * {@link ActivityProvider}s.</li>
 * <li><b>Scheduler:</b> Periodically refreshes the user's presence based on the
 * configured interval.</li>
 * </ul>
 * 
 * @author Jon Marien
 * @version 2.2.0
 */
public class DiscordRPCManager {

    private final BurpcordConfig config;
    private final List<ActivityProvider> providers = new ArrayList<>();

    // IPC Client
    private IPCClient client;
    private ScheduledExecutorService scheduler;
    private boolean isConnected = false;
    private final OffsetDateTime startTime;

    public DiscordRPCManager(BurpcordConfig config) {
        this.config = config;
        this.startTime = OffsetDateTime.now();
    }

    /**
     * Registers a new activity provider.
     * 
     * @param provider The provider implementation to register.
     */
    public void registerProvider(ActivityProvider provider) {
        providers.add(provider);
        providers.sort(java.util.Comparator.comparingInt(ActivityProvider::getPriority));
    }

    /**
     * Registers multiple activity providers at once.
     * 
     * @param newProviders Variable arguments of providers to register.
     */
    public void registerProviders(ActivityProvider... newProviders) {
        java.util.Collections.addAll(providers, newProviders);
        providers.sort(java.util.Comparator.comparingInt(ActivityProvider::getPriority));
    }

    public void registerProviders(List<ActivityProvider> newProviders) {
        providers.addAll(newProviders);
        providers.sort(java.util.Comparator.comparingInt(ActivityProvider::getPriority));
    }

    private static final int MAX_CONNECT_RETRIES = 3;
    private static final long INITIAL_RETRY_DELAY_MS = 2000;

    /**
     * Initializes the IPC client and starts the update scheduler.
     * <p>
     * Connection is attempted on a background thread with exponential backoff
     * retries to handle transient Discord IPC handshake failures (e.g., null
     * {@code data} in the handshake response when Discord is not fully ready).
     * </p>
     */
    public void initialize() {
        if (!config.isRpcEnabled())
            return;

        Thread connectThread = new Thread(() -> {
            try {
                connectWithRetry();
            } catch (Exception e) {
                BurpcordSettingsTab.log("Burpcord: Failed to connect to Discord IPC after "
                        + MAX_CONNECT_RETRIES + " attempts: " + e.getMessage());
                System.err.println("Burpcord: Failed to connect to Discord IPC.");
                e.printStackTrace();
            }
        }, "Burpcord-IPC-Connect");
        connectThread.setDaemon(true);
        connectThread.start();
    }

    /**
     * Attempts to connect to Discord IPC with exponential backoff retries.
     *
     * @throws Exception if all retry attempts are exhausted.
     */
    private void connectWithRetry() throws Exception {
        long appId = Long.parseLong(config.getAppId());

        Exception lastException = null;
        for (int attempt = 1; attempt <= MAX_CONNECT_RETRIES; attempt++) {
            try {
                client = new IPCClient(appId);
                client.setListener(createIPCListener());
                client.connect();
                startScheduler();
                return; // Success
            } catch (Exception e) {
                lastException = e;
                if (attempt < MAX_CONNECT_RETRIES) {
                    long delay = INITIAL_RETRY_DELAY_MS * (1L << (attempt - 1)); // 2s, 4s, 8s
                    BurpcordSettingsTab.log("Discord IPC connect attempt " + attempt + "/" + MAX_CONNECT_RETRIES
                            + " failed: " + e.getMessage() + " — retrying in " + (delay / 1000) + "s...");
                    Thread.sleep(delay);
                }
            }
        }
        throw lastException;
    }

    /**
     * Creates the {@link IPCListener} for handling IPC lifecycle events.
     */
    private IPCListener createIPCListener() {
        return new IPCListener() {
            @Override
            public void onReady(IPCClient client) {
                isConnected = true;
                BurpcordSettingsTab.log("Connected to Discord IPC.");
                BurpcordSettingsTab.updateConnectionStatusStatic(true);
                updatePresence("Ready");
            }

            @Override
            public void onDisconnect(IPCClient client, Throwable t) {
                isConnected = false;
                BurpcordSettingsTab.log("Disconnected from Discord IPC.");
                BurpcordSettingsTab.updateConnectionStatusStatic(false);
            }

            @Override
            public void onPacketSent(IPCClient client, Packet packet) {
                // No-op
            }

            @Override
            public void onPacketReceived(IPCClient client, Packet packet) {
                // No-op
            }

            @Override
            public void onActivityJoin(IPCClient client, String secret) {
                // No-op
            }

            @Override
            public void onActivitySpectate(IPCClient client, String secret) {
                // No-op
            }

            @Override
            public void onActivityJoinRequest(IPCClient client, String secret, User user) {
                // No-op
            }

            @Override
            public void onClose(IPCClient client, JsonObject json) {
                isConnected = false;
                BurpcordSettingsTab.log("Discord IPC Closed.");
                BurpcordSettingsTab.updateConnectionStatusStatic(false);
            }
        };
    }

    /**
     * Shuts down the scheduler and closes the IPC connection.
     */
    public void shutdown() {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
        if (client != null && client.getStatus() == PipeStatus.CONNECTED) {
            client.close();
        }
        isConnected = false;
    }

    /**
     * Restarts the scheduler with the current configuration.
     */
    public void restartScheduler() {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
        startScheduler();
    }

    private void startScheduler() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::updateLoop, 0, config.getUpdateInterval(), TimeUnit.SECONDS);
    }

    private void updateLoop() {
        if (client == null || client.getStatus() != PipeStatus.CONNECTED) {
            return;
        }
        updatePresence(null);
    }

    /**
     * Constructs and sends the Rich Presence update to Discord.
     * <p>
     * Iterates through registered providers to determine the current activity
     * state.
     * If no specific provider is active, a default state is used.
     * </p>
     * 
     * @param manualState Optional manual state override.
     */
    public void updatePresence(String manualState) {
        if (client == null || client.getStatus() != PipeStatus.CONNECTED) {
            return;
        }

        RichPresence.Builder builder = new RichPresence.Builder();
        // Explicitly set ActivityType to prevent NPE in library
        builder.setActivityType(ActivityType.Playing);

        builder.setLargeImageWithTooltip("burp", "Burp Suite Professional");
        builder.setStartTimestamp(startTime.toEpochSecond());

        String state = config.getCustomState();
        boolean providerFound = false;

        for (ActivityProvider provider : providers) {
            if (provider.isActive()) {
                provider.updatePresence(builder);
                providerFound = true;
                break;
            }
        }

        if (!providerFound) {
            builder.setDetails("Burp Suite - " + BurpcordConstants.VERSION);
            builder.setState(state != null && !state.isEmpty() ? state : "Security Researching");
        }

        client.sendRichPresence(builder.build());
    }

    /**
     * Reinitializes the entire RPC subsystem.
     */
    public void reloadRPC() {
        shutdown();
        initialize();
    }
}
