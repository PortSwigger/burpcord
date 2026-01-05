package com.burpcord;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;

import com.google.gson.JsonObject;
import com.jagrosh.discordipc.IPCClient;
import com.jagrosh.discordipc.IPCListener;
import com.jagrosh.discordipc.entities.Packet;
import com.jagrosh.discordipc.entities.RichPresence;
import com.jagrosh.discordipc.entities.User;
import com.jagrosh.discordipc.entities.pipe.PipeStatus;

public class BurpcordExtension implements BurpExtension {

    private IPCClient client;
    private static final long CLIENT_ID = 1457789708753965206L;

    @Override
    public void initialize(MontoyaApi api) {
        api.extension().setName("Burpcord");

        api.logging().logToOutput("Loading Burpcord...");

        try {
            client = new IPCClient(CLIENT_ID);
            client.setListener(new IPCListener() {
                @Override
                public void onReady(IPCClient client) {
                    api.logging().logToOutput("Discord IPC Ready!");
                    updatePresence("Scanning for vulnerabilities");
                }

                @Override
                public void onPacketSent(IPCClient client, Packet packet) {
                    // TODO Auto-generated method stub
                    throw new UnsupportedOperationException("Unimplemented method 'onPacketSent'");
                }

                @Override
                public void onPacketReceived(IPCClient client, Packet packet) {
                    // TODO Auto-generated method stub
                    throw new UnsupportedOperationException("Unimplemented method 'onPacketReceived'");
                }

                @Override
                public void onActivityJoin(IPCClient client, String secret) {
                    // TODO Auto-generated method stub
                    throw new UnsupportedOperationException("Unimplemented method 'onActivityJoin'");
                }

                @Override
                public void onActivitySpectate(IPCClient client, String secret) {
                    // TODO Auto-generated method stub
                    throw new UnsupportedOperationException("Unimplemented method 'onActivitySpectate'");
                }

                @Override
                public void onActivityJoinRequest(IPCClient client, String secret, User user) {
                    // TODO Auto-generated method stub
                    throw new UnsupportedOperationException("Unimplemented method 'onActivityJoinRequest'");
                }

                @Override
                public void onClose(IPCClient client, JsonObject json) {
                    // TODO Auto-generated method stub
                    throw new UnsupportedOperationException("Unimplemented method 'onClose'");
                }

                @Override
                public void onDisconnect(IPCClient client, Throwable t) {
                    // TODO Auto-generated method stub
                    throw new UnsupportedOperationException("Unimplemented method 'onDisconnect'");
                }
            });

            client.connect();

        } catch (Exception e) {
            api.logging().logToError("Failed to connect to Discord IPC: " + e.getMessage());
        }
    }

    private void updatePresence(String details) {
        if (client != null && client.getStatus() == PipeStatus.CONNECTED) {
            RichPresence.Builder builder = new RichPresence.Builder();
            builder.setDetails(details)
                    .setStartTimestamp(System.currentTimeMillis());
            client.sendRichPresence(builder.build());
        }
    }
}
