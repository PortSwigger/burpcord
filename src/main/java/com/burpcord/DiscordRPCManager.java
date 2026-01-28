package com.burpcord;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.collaborator.CollaboratorClient;

import com.google.gson.JsonObject;

import com.jagrosh.discordipc.IPCClient;
import com.jagrosh.discordipc.IPCListener;
import com.jagrosh.discordipc.entities.Packet;
import com.jagrosh.discordipc.entities.RichPresence;
import com.jagrosh.discordipc.entities.User;
import com.jagrosh.discordipc.entities.pipe.PipeStatus;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Central manager for Discord Rich Presence integration with Burp Suite.
 * 
 * <p>
 * This class coordinates all Discord IPC communication and maintains
 * the state of various Burp Suite activities. It:
 * </p>
 * <ul>
 * <li>Manages the Discord IPC client lifecycle (connect, disconnect,
 * reconnect)</li>
 * <li>Periodically updates Discord presence with current Burp activity</li>
 * <li>Tracks activity from multiple tools (Proxy, Scanner, Repeater, Intruder,
 * WebSockets)</li>
 * <li>Integrates with Montoya API for advanced metrics (Site Map, Scope,
 * Collaborator)</li>
 * <li>Rotates between active statuses when multiple tools are in use</li>
 * </ul>
 * 
 * <h2>Thread Safety</h2>
 * <p>
 * All activity counters use {@link AtomicInteger} and {@link AtomicBoolean}
 * to ensure thread-safe updates from Burp Suite's event handlers.
 * </p>
 * 
 * <h2>Status Priority</h2>
 * <p>
 * When multiple activities are active, they are displayed in rotation.
 * Priority order: Intercept → Scanner → Proxy → Repeater → Intruder →
 * Site Map → Scope → Collaborator → WebSockets
 * </p>
 * 
 * @author Jon Marien
 * @version 1.3
 * @see BurpcordConfig
 * @see BurpcordSettingsTab
 */
public class DiscordRPCManager {

    /** Montoya API instance for accessing Burp Suite features. */
    private final MontoyaApi api;
    /** Configuration provider for user preferences. */
    private final BurpcordConfig config;
    /** Discord IPC client for Rich Presence communication. */
    private IPCClient client;
    /** Connection status flag. */
    private boolean isConnected = false;

    // Proxy activity tracking
    private final AtomicInteger requestCount = new AtomicInteger(0);
    private final AtomicInteger responseCount = new AtomicInteger(0);

    // Intercept stats
    private final AtomicBoolean isIntercepting = new AtomicBoolean(false);
    private long lastInterceptTime = 0;

    // Scanner stats
    private final AtomicInteger vulnHigh = new AtomicInteger(0);
    private final AtomicInteger vulnMedium = new AtomicInteger(0);
    private final AtomicInteger vulnLow = new AtomicInteger(0);
    private final AtomicInteger vulnInfo = new AtomicInteger(0);

    private long lastActiveScanTime = 0;
    private long lastPassiveScanTime = 0;

    // Repeater stats
    private final AtomicBoolean isRepeaterActive = new AtomicBoolean(false);
    private final AtomicInteger repeaterRequests = new AtomicInteger(0);
    private long lastRepeaterActivity = 0;

    // Intruder stats
    private final AtomicInteger intruderRequests = new AtomicInteger(0);
    private long lastIntruderActivity = 0;

    // WebSocket stats
    private final AtomicInteger webSocketMessages = new AtomicInteger(0);
    private long lastWebSocketActivity = 0;

    // Collaborator (Pro only)
    private CollaboratorClient collaboratorClient;
    private boolean collaboratorAvailable = false;

    // Status rotation
    private int statusIndex = 0;

    // Persistent start time for Discord timestamp
    private long startTime = 0;

    private ScheduledExecutorService scheduler;

    public DiscordRPCManager(MontoyaApi api, BurpcordConfig config) {
        this.api = api;
        this.config = config;
        initCollaborator();
    }

    private void initCollaborator() {
        try {
            collaboratorClient = api.collaborator().createClient();
            collaboratorAvailable = true;
            api.logging().logToOutput("Collaborator client initialized (Pro feature)");
        } catch (Exception e) {
            collaboratorAvailable = false;
            api.logging().logToOutput("Collaborator not available (Community Edition or not configured)");
        }
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setIntercepting(boolean intercepting) {
        isIntercepting.set(intercepting);
        if (intercepting) {
            lastInterceptTime = System.currentTimeMillis();
        }
    }

    public void markActiveScan() {
        lastActiveScanTime = System.currentTimeMillis();
    }

    public void markPassiveScan() {
        lastPassiveScanTime = System.currentTimeMillis();
    }

    public void initialize() {
        try {
            client = new IPCClient(Long.parseLong(config.getAppId()));
            client.setListener(new IPCListener() {
                public void onReady(IPCClient client) {
                    api.logging().logToOutput("Discord IPC Ready!");
                    BurpcordSettingsTab.log("Discord RPC connected successfully!");
                    isConnected = true;
                    // Initialize start time on connection
                    startTime = System.currentTimeMillis() / 1000L;
                    // Send initial presence with Burp version
                    updatePresence(getBurpVersion());
                    startPeriodicUpdates();
                }

                public void onDisconnect(IPCClient client, Throwable t) {
                    String message = (t != null && t.getMessage() != null) ? t.getMessage() : "Unknown error";
                    api.logging().logToError("Discord IPC Disconnected: " + message);
                    BurpcordSettingsTab.log("ERROR: Discord disconnected - " + message);
                    isConnected = false;
                    stopPeriodicUpdates();
                }

                @Override
                public void onClose(IPCClient client, JsonObject json) {
                    api.logging().logToOutput("Discord IPC Closed: " + json.toString());
                    isConnected = false;
                    stopPeriodicUpdates();
                }

                @Override
                public void onPacketSent(IPCClient client, Packet packet) {
                    // Optional: Debug logging
                }

                @Override
                public void onPacketReceived(IPCClient client, Packet packet) {
                    // Optional: Debug logging
                }

                @Override
                public void onActivityJoin(IPCClient client, String secret) {
                    // Not used
                }

                @Override
                public void onActivitySpectate(IPCClient client, String secret) {
                    // Not used
                }

                @Override
                public void onActivityJoinRequest(IPCClient client, String secret, User user) {
                    // Not used
                }
            });

            api.logging().logToOutput("Connecting to Discord IPC...");
            client.connect();

        } catch (Exception e) {
            api.logging().logToError("Failed to initialize Discord IPC: " + e.getMessage());
            isConnected = false;
        }
    }

    public void incrementRequestCount() {
        requestCount.incrementAndGet();
    }

    public void incrementResponseCount() {
        responseCount.incrementAndGet();
    }

    // Scanner methods
    public void incrementVulnHigh() {
        vulnHigh.incrementAndGet();
    }

    public void incrementVulnMedium() {
        vulnMedium.incrementAndGet();
    }

    public void incrementVulnLow() {
        vulnLow.incrementAndGet();
    }

    public void incrementVulnInfo() {
        vulnInfo.incrementAndGet();
    }

    // Repeater methods
    public void markRepeaterActivity() {
        isRepeaterActive.set(true);
        repeaterRequests.incrementAndGet();
        lastRepeaterActivity = System.currentTimeMillis();
    }

    // Intruder methods
    public void markIntruderActivity() {
        intruderRequests.incrementAndGet();
        lastIntruderActivity = System.currentTimeMillis();
    }

    // WebSocket methods
    public void markWebSocketActivity() {
        webSocketMessages.incrementAndGet();
        lastWebSocketActivity = System.currentTimeMillis();
    }

    public void restartScheduler() {
        stopPeriodicUpdates();
        startPeriodicUpdates();
    }

    // Montoya API Stats Methods
    private String getBurpVersion() {
        try {
            return api.burpSuite().version().toString();
        } catch (Exception e) {
            return "Burp Suite";
        }
    }

    private int getProxyHistorySize() {
        try {
            return api.proxy().history().size();
        } catch (Exception e) {
            return requestCount.get();
        }
    }

    private int getSiteMapSize() {
        try {
            return api.siteMap().requestResponses().size();
        } catch (Exception e) {
            return 0;
        }
    }

    private int getScopeTargetCount() {
        try {
            return (int) api.proxy().history().stream()
                    .map(r -> r.finalRequest().httpService().host())
                    .distinct()
                    .filter(host -> {
                        try {
                            return api.scope().isInScope("https://" + host);
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .count();
        } catch (Exception e) {
            return 0;
        }
    }

    private int getCollaboratorHits() {
        if (!collaboratorAvailable || collaboratorClient == null) {
            return -1;
        }
        try {
            return collaboratorClient.getAllInteractions().size();
        } catch (Exception e) {
            return -1;
        }
    }

    private void startPeriodicUpdates() {
        if (scheduler == null || scheduler.isShutdown()) {
            scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.scheduleAtFixedRate(() -> {
                try {
                    updateStatusFromStats();
                } catch (Exception e) {
                    api.logging().logToError("Error in status update: " + e.getMessage());
                }
            }, 0, config.getUpdateInterval(), TimeUnit.SECONDS);
        }
    }

    private void stopPeriodicUpdates() {
        if (scheduler != null) {
            scheduler.shutdown();
            scheduler = null;
        }
    }

    private void updateStatusFromStats() {
        long currentTime = System.currentTimeMillis();
        java.util.List<String> activeStatuses = new java.util.ArrayList<>();

        // 1. Check Intercept
        if (isIntercepting.get()) {
            // Auto-clear after 5 seconds of inactivity
            if (currentTime - lastInterceptTime > 5000) {
                isIntercepting.set(false);
            } else if (config.isShowIntercept()) {
                activeStatuses.add(String.format("Intercepting Traffic - %d reqs", requestCount.get()));
            }
        }

        // 2. Check Scanner (Active or Passive activity in last 60s, or significant
        // vulns)
        int high = vulnHigh.get();
        int med = vulnMedium.get();
        boolean recentScan = (currentTime - lastActiveScanTime < 60000) || (currentTime - lastPassiveScanTime < 60000);

        if ((recentScan || high > 0 || med > 0) && config.isShowScan()) {
            if (high > 0 || med > 0) {
                activeStatuses.add(String.format("Scanning: %d High | %d Med Issues", high, med));
            } else if (recentScan) {
                activeStatuses.add("Scanning for Vulnerabilities...");
            }
        }

        // 3. Check Proxy (use Montoya API for accurate count)
        if (config.isShowProxy()) {
            int historySize = getProxyHistorySize();
            if (historySize > 0 && !isIntercepting.get()) {
                activeStatuses.add(String.format("Proxy: %d requests", historySize));
            }
        }

        // 4. Check Repeater (Active in last 60s)
        if (isRepeaterActive.get() && (currentTime - lastRepeaterActivity < 60000) && config.isShowRepeater()) {
            activeStatuses.add(String.format("Testing in Repeater - %d requests", repeaterRequests.get()));
        }

        // 5. Check Intruder (Active in last 60s)
        if ((currentTime - lastIntruderActivity < 60000) && config.isShowIntruder()) {
            activeStatuses.add(String.format("Intruder Attack - %d requests", intruderRequests.get()));
        }

        // 6. Check Site Map
        if (config.isShowSiteMap()) {
            int endpoints = getSiteMapSize();
            if (endpoints > 0) {
                activeStatuses.add(String.format("Site Map: %d endpoints", endpoints));
            }
        }

        // 7. Check Scope
        if (config.isShowScope()) {
            int targets = getScopeTargetCount();
            if (targets > 0) {
                activeStatuses.add(String.format("Testing %d target(s) in scope", targets));
            }
        }

        // 8. Check Collaborator (Pro only)
        if (config.isShowCollaborator() && collaboratorAvailable) {
            int hits = getCollaboratorHits();
            if (hits > 0) {
                activeStatuses.add(String.format("Collaborator: %d OOB hits!", hits));
            }
        }

        // 9. Check WebSockets (Active in last 60s)
        if ((currentTime - lastWebSocketActivity < 60000) && config.isShowWebSockets()) {
            activeStatuses.add(String.format("WebSocket: %d messages", webSocketMessages.get()));
        }

        // Default if nothing else
        if (activeStatuses.isEmpty()) {
            updatePresence(getBurpVersion());
            return;
        }

        // Rotate statuses
        String nextStatus = activeStatuses.get(statusIndex++ % activeStatuses.size());
        updatePresence(nextStatus);
    }

    public void updatePresence(String details) {
        if (!config.isRpcEnabled()) {
            return;
        }

        if (client != null && client.getStatus() == PipeStatus.CONNECTED) {
            try {
                RichPresence.Builder builder = new RichPresence.Builder();
                builder.setDetails(details)
                        .setLargeImage("burp", "Burp Suite")
                        .setStartTimestamp(startTime); // Use persistent start time
                // Set state from config (user customizable)
                builder.setState(config.getCustomState());

                RichPresence rp = builder.build();

                // Fix for NPE in library: explicitly set activityType via reflection if null
                try {
                    java.lang.reflect.Field f = rp.getClass().getDeclaredField("activityType");
                    f.setAccessible(true);
                    if (f.get(rp) == null) {
                        Class<?> enumClass = f.getType();
                        Object[] constants = enumClass.getEnumConstants();
                        if (constants != null && constants.length > 0) {
                            f.set(rp, constants[0]); // Default to first available (Playing)
                        }
                    }
                } catch (Exception e) {
                    // Silent
                }

                client.sendRichPresence(rp);
            } catch (Exception e) {
                api.logging().logToError("Failed to update presence: " + e.getMessage());
            }
        }
    }

    /**
     * Clears the Discord Rich Presence by sending an empty activity.
     * Discord interprets this as a "clear activity" command.
     */
    private void clearPresence() {
        if (client != null && client.getStatus() == PipeStatus.CONNECTED) {
            try {
                // Send empty RichPresence - Discord interprets this as "clear activity"
                RichPresence.Builder emptyBuilder = new RichPresence.Builder();
                client.sendRichPresence(emptyBuilder.build());
                BurpcordSettingsTab.log("Rich Presence cleared.");
            } catch (Exception e) {
                api.logging().logToError("Failed to clear presence: " + e.getMessage());
            }
        }
    }

    /**
     * Shuts down the Discord RPC connection and cleans up resources.
     * Clears the Rich Presence before disconnecting to prevent ghost statuses.
     */
    public void shutdown() {
        stopPeriodicUpdates();
        isIntercepting.set(false);
        isConnected = false;
        startTime = 0; // Reset so next connection gets fresh time
        if (client != null) {
            try {
                // CRITICAL: Clear presence BEFORE closing connection
                clearPresence();

                // Wait for Discord to process the clear command
                Thread.sleep(500);

                client.close();
                BurpcordSettingsTab.log("Discord RPC disconnected.");
            } catch (Exception e) {
                api.logging().logToError("Error shutting down Discord IPC: " + e.getMessage());
            }
            client = null;
        }
    }
}
