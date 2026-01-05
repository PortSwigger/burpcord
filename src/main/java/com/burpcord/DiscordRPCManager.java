package com.burpcord;

import burp.api.montoya.MontoyaApi;
import com.google.gson.JsonObject;
import com.jagrosh.discordipc.IPCClient;
import com.jagrosh.discordipc.IPCListener;
import com.jagrosh.discordipc.entities.Packet;
import com.jagrosh.discordipc.entities.RichPresence;
import com.jagrosh.discordipc.entities.User;
import com.jagrosh.discordipc.entities.pipe.PipeStatus;

public class DiscordRPCManager {

    private final MontoyaApi api;
    private IPCClient client;
    private boolean isConnected = false;

    public DiscordRPCManager(MontoyaApi api) {
        this.api = api;
    }

    public boolean isConnected() {
        return isConnected;
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
                }

                @Override
                public void onDisconnect(IPCClient client, Throwable t) {
                    String message = (t != null && t.getMessage() != null) ? t.getMessage() : "Unknown error";
                    api.logging().logToError("Discord IPC Disconnected: " + message);
                    isConnected = false;
                }

                @Override
                public void onClose(IPCClient client, JsonObject json) {
                    api.logging().logToOutput("Discord IPC Closed: " + json.toString());
                    isConnected = false;
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
        if (client != null) {
            try {
                client.close();
            } catch (Exception e) {
                api.logging().logToError("Error shutting down Discord IPC: " + e.getMessage());
            }
        }
    }
}
