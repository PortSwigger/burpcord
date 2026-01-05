package com.burpcord;

import burp.api.montoya.MontoyaApi;

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

public class DiscordRPCManager {

    private final MontoyaApi api;
    private IPCClient client;
    private boolean isConnected = false;

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

    // Status rotation
    private int statusIndex = 0;

    private ScheduledExecutorService scheduler;

    public DiscordRPCManager(MontoyaApi api) {
        this.api = api;
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
            client = new IPCClient(BurpcordConfig.CLIENT_ID);
            client.setListener(new IPCListener() {
                @Override
                public void onReady(IPCClient client) {
                    api.logging().logToOutput("Discord IPC Ready!");
                    isConnected = true;
                    // Send initial presence
                    updatePresence(BurpcordConfig.DEFAULT_PRESENCE);
                    startPeriodicUpdates();
                }

                @Override
                public void onDisconnect(IPCClient client, Throwable t) {
                    String message = (t != null && t.getMessage() != null) ? t.getMessage() : "Unknown error";
                    api.logging().logToError("Discord IPC Disconnected: " + message);
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

    private void startPeriodicUpdates() {
        if (scheduler == null || scheduler.isShutdown()) {
            scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.scheduleAtFixedRate(this::updateStatusFromStats, 0, 5, TimeUnit.SECONDS);
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
            } else {
                activeStatuses.add(String.format("Intercepting Traffic - %d reqs", requestCount.get()));
            }
        }

        // 2. Check Scanner (Active or Passive activity in last 60s, or significant
        // vulns)
        int high = vulnHigh.get();
        int med = vulnMedium.get();
        boolean recentScan = (currentTime - lastActiveScanTime < 60000) || (currentTime - lastPassiveScanTime < 60000);

        if (recentScan || high > 0 || med > 0) {
            if (high > 0 || med > 0) {
                activeStatuses.add(String.format("Scanning: %d High | %d Med Issues", high, med));
            } else if (recentScan) {
                activeStatuses.add("Scanning for Vulnerabilities...");
            }
        }

        // 3. Check Proxy (If processing requests)
        // We consider proxy active if we have requests and are not just intercepting
        if (requestCount.get() > 0 && !isIntercepting.get()) {
            // Ideally check recent proxy activity, but for now existance of requests
            // implies usage.
            // Maybe verify if count changed? Let's keep it simple as per "Proxy >
            // Repeater".
            activeStatuses.add(String.format("Proxy: %d Reqs | %d Resps", requestCount.get(), responseCount.get()));
        }

        // 4. Check Repeater (Active in last 60s)
        if (isRepeaterActive.get() && (currentTime - lastRepeaterActivity < 60000)) {
            activeStatuses.add(String.format("Testing in Repeater - %d requests", repeaterRequests.get()));
        }

        // Default if nothing else
        if (activeStatuses.isEmpty()) {
            updatePresence(BurpcordConfig.DEFAULT_PRESENCE);
            return;
        }

        // Rotate statuses
        // Priority logic from requirement: Scanner > Proxy > Repeater > General
        // The requirement says "priority... to align priority to scanner > proxy >
        // repeater... respecting intended ordering"
        // AND "Implement rotation... round-robin over scanner, proxy, repeater when
        // they're simultaneously active"
        // This implies we should rotate through all valid ones.

        // We collected them in an order. Let's just rotate through the collection.
        // If we wanted strict priority (only show top), we would pick
        // activeStatuses.get(0).
        // But "rotation" implies cycling.

        String nextStatus = activeStatuses.get(statusIndex++ % activeStatuses.size());
        updatePresence(nextStatus);
    }

    public void updatePresence(String details) {
        if (!BurpcordConfig.ENABLE_RPC)
            return;

        if (client != null && client.getStatus() == PipeStatus.CONNECTED) {
            try {
                RichPresence.Builder builder = new RichPresence.Builder();
                builder.setDetails(details)
                        .setStartTimestamp(System.currentTimeMillis() / 1000L);
                client.sendRichPresence(builder.build());
            } catch (Exception e) {
                api.logging().logToError("Failed to update presence: " + e.getMessage());
            }
        }
    }

    public void shutdown() {
        stopPeriodicUpdates();
        isIntercepting.set(false);
        if (client != null) {
            try {
                client.close();
            } catch (Exception e) {
                api.logging().logToError("Error shutting down Discord IPC: " + e.getMessage());
            }
        }
    }
}
