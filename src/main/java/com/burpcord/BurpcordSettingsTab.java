package com.burpcord;

import burp.api.montoya.MontoyaApi;

import javax.swing.*;
import java.awt.*;

public class BurpcordSettingsTab extends JPanel {

    private final MontoyaApi api;
    private final BurpcordConfig config;
    private final DiscordRPCManager rpcManager;

    private final JTextField appIdField;
    private final JSpinner intervalSpinner;
    private final JButton reloadRpcBtn;
    private final JCheckBox showInterceptCheck;
    private final JCheckBox showScanCheck;
    private final JCheckBox showProxyCheck;
    private final JCheckBox showRepeaterCheck;
    private final JCheckBox showIntruderCheck;
    private final JCheckBox showSiteMapCheck;
    private final JCheckBox showScopeCheck;
    private final JCheckBox showCollaboratorCheck;
    private final JCheckBox showWebSocketsCheck;
    private final JTextField customStateField;
    private final JTextArea logArea;
    private static BurpcordSettingsTab instance;

    public BurpcordSettingsTab(MontoyaApi api, BurpcordConfig config, DiscordRPCManager rpcManager) {
        this.api = api;
        this.config = config;
        this.rpcManager = rpcManager;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setAlignmentX(Component.LEFT_ALIGNMENT);

        // Reload RPC Button
        reloadRpcBtn = new JButton("Reload Discord RPC");
        reloadRpcBtn.addActionListener(e -> {
            rpcManager.shutdown();
            rpcManager.initialize();
        });
        add(reloadRpcBtn);
        add(Box.createVerticalStrut(10));

        // App ID
        JPanel appIdPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        appIdPanel.add(new JLabel("Discord App ID (Client ID): "));
        appIdField = new JTextField(config.getAppId(), 20);
        appIdPanel.add(appIdField);
        JButton updateAppIdBtn = new JButton("Update App ID (Reconnects RPC)");
        updateAppIdBtn.addActionListener(e -> updateAppId());
        appIdPanel.add(updateAppIdBtn);
        add(appIdPanel);

        // Update Interval
        JPanel intervalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        intervalPanel.add(new JLabel("Update Interval (seconds): "));
        intervalSpinner = new JSpinner(new SpinnerNumberModel(config.getUpdateInterval(), 1, 60, 1));
        intervalSpinner.addChangeListener(e -> updateInterval());
        intervalPanel.add(intervalSpinner);
        add(intervalPanel);

        // Custom State Text
        JPanel statePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statePanel.add(new JLabel("Custom State Text: "));
        customStateField = new JTextField(config.getCustomState(), 25);
        statePanel.add(customStateField);
        JButton updateStateBtn = new JButton("Update");
        updateStateBtn.addActionListener(e -> {
            config.setCustomState(customStateField.getText().trim());
            // Force immediate presence update with user's custom state
            rpcManager.updatePresence("Using Burp Suite");
        });
        statePanel.add(updateStateBtn);
        add(statePanel);

        // Features Toggles
        add(Box.createVerticalStrut(10));
        add(new JLabel("Tool Activity Features:"));

        showInterceptCheck = new JCheckBox("Show Intercept Status", config.isShowIntercept());
        showInterceptCheck.addActionListener(e -> config.setShowIntercept(showInterceptCheck.isSelected()));
        add(showInterceptCheck);

        showScanCheck = new JCheckBox("Show Scanner Status", config.isShowScan());
        showScanCheck.addActionListener(e -> config.setShowScan(showScanCheck.isSelected()));
        add(showScanCheck);

        showProxyCheck = new JCheckBox("Show Proxy Status", config.isShowProxy());
        showProxyCheck.addActionListener(e -> config.setShowProxy(showProxyCheck.isSelected()));
        add(showProxyCheck);

        showRepeaterCheck = new JCheckBox("Show Repeater Status", config.isShowRepeater());
        showRepeaterCheck.addActionListener(e -> config.setShowRepeater(showRepeaterCheck.isSelected()));
        add(showRepeaterCheck);

        showIntruderCheck = new JCheckBox("Show Intruder Status", config.isShowIntruder());
        showIntruderCheck.addActionListener(e -> config.setShowIntruder(showIntruderCheck.isSelected()));
        add(showIntruderCheck);

        // v1.3 - Montoya API Features
        add(Box.createVerticalStrut(10));
        add(new JLabel("Advanced Features (v1.3):"));

        showSiteMapCheck = new JCheckBox("Show Site Map Stats", config.isShowSiteMap());
        showSiteMapCheck.addActionListener(e -> config.setShowSiteMap(showSiteMapCheck.isSelected()));
        add(showSiteMapCheck);

        showScopeCheck = new JCheckBox("Show Scope Target Count", config.isShowScope());
        showScopeCheck.addActionListener(e -> config.setShowScope(showScopeCheck.isSelected()));
        add(showScopeCheck);

        showCollaboratorCheck = new JCheckBox("Show Collaborator Hits (Pro Only)", config.isShowCollaborator());
        showCollaboratorCheck.addActionListener(e -> config.setShowCollaborator(showCollaboratorCheck.isSelected()));
        add(showCollaboratorCheck);

        showWebSocketsCheck = new JCheckBox("Show WebSocket Activity", config.isShowWebSockets());
        showWebSocketsCheck.addActionListener(e -> config.setShowWebSockets(showWebSocketsCheck.isSelected()));
        add(showWebSocketsCheck);

        // Save Button
        add(Box.createVerticalStrut(20));
        JButton saveBtn = new JButton("Save All Settings");
        saveBtn.addActionListener(e -> saveAll());
        add(saveBtn);

        // Log Viewer Section
        add(Box.createVerticalStrut(20));
        add(new JLabel("Extension Logs:"));

        logArea = new JTextArea(10, 50);
        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        logArea.setBackground(new Color(30, 30, 30));
        logArea.setForeground(new Color(200, 200, 200));
        JScrollPane logScrollPane = new JScrollPane(logArea);
        logScrollPane.setPreferredSize(new Dimension(600, 200));
        add(logScrollPane);

        JButton clearLogBtn = new JButton("Clear Logs");
        clearLogBtn.addActionListener(e -> logArea.setText(""));
        add(clearLogBtn);

        instance = this;
        log("Burpcord initialized.");
    }

    public static void log(String message) {
        if (instance != null && instance.logArea != null) {
            SwingUtilities.invokeLater(() -> {
                instance.logArea
                        .append("[" + java.time.LocalTime.now().toString().substring(0, 8) + "] " + message + "\n");
                instance.logArea.setCaretPosition(instance.logArea.getDocument().getLength());
            });
        }
    }

    private void updateAppId() {
        String newId = appIdField.getText().trim();
        if (!newId.isEmpty()) {
            config.setAppId(newId);
            // Re-initialize manager to pick up new ID (Requires shutdown/init)
            rpcManager.shutdown();
            rpcManager.initialize();
            JOptionPane.showMessageDialog(
                    api.userInterface().swingUtils().suiteFrame(),
                    "App ID updated and RPC reconnected.");
        }
    }

    private void updateInterval() {
        int val = (Integer) intervalSpinner.getValue();
        config.setUpdateInterval(val);
        rpcManager.restartScheduler();
    }

    private void saveAll() {
        updateAppId();
        config.setCustomState(customStateField.getText().trim());
        // Toggles are auto-saved.
    }
}
