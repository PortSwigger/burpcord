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
    private final AtomicBoolean isIntercepting = new AtomicBoolean(false);

    // Scanner stats
    private final AtomicInteger vulnHigh = new AtomicInteger(0);
    private final AtomicInteger vulnMedium = new AtomicInteger(0);
    private final AtomicInteger vulnLow = new AtomicInteger(0);
    private final AtomicInteger vulnInfo = new AtomicInteger(0);

    // Repeater stats
    private final AtomicBoolean isRepeaterActive = new AtomicBoolean(false);
    private final AtomicInteger repeaterRequests = new AtomicInteger(0);
    private long lastRepeaterActivity = 0;

    private ScheduledExecutorService scheduler;

    public DiscordRPCManager(MontoyaApi api) {
        this.api = api;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setIntercepting(boolean intercepting) {
        isIntercepting.set(intercepting);
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
            scheduler.scheduleAtFixedRate(this::updateStatusFromStats, 0, 15, TimeUnit.SECONDS);
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

        // Priority 1: Intercepting (Most immediate user action)
        if (isIntercepting.get()) {
            String status = String.format("Intercepting Traffic - %d reqs", requestCount.get());
            updatePresence(status);
            return;
        }

        // Priority 2: Scanner (If significant vulns found)
        int high = vulnHigh.get();
        int med = vulnMedium.get();
        if (high > 0 || med > 0) {
            String status = String.format("Scanning: %d High | %d Med Issues", high, med);
            updatePresence(status);
            return;
        }

        // Priority 3: Repeater (If recently active)
        if (isRepeaterActive.get() && (currentTime - lastRepeaterActivity < 60000)) { // Active in last 60s
            String status = String.format("Testing in Repeater - %d requests", repeaterRequests.get());
            updatePresence(status);
            return;
        }

        // Priority 4: General Stats (Default fall-through)
        // If we have some low/info vulns but no high/med, maybe show that or just
        // default?
        // Let's stick to the proxy counts if we have processed a lot of traffic
        int reqs = requestCount.get();
        if (reqs > 0) {
            String status = String.format("Proxy: %d Reqs | %d Resps", reqs, responseCount.get());
            updatePresence(status);
            return;
        }

        updatePresence(BurpcordConfig.DEFAULT_PRESENCE);
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
