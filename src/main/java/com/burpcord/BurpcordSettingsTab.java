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
    private final JCheckBox rpcEnabledCheck;
    private final JCheckBox showInterceptCheck;
    private final JCheckBox showScanCheck;
    private final JCheckBox showProxyCheck;
    private final JCheckBox showRepeaterCheck;
    private final JCheckBox showIntruderCheck;
    private final JTextField customStateField;

    public BurpcordSettingsTab(MontoyaApi api, BurpcordConfig config, DiscordRPCManager rpcManager) {
        this.api = api;
        this.config = config;
        this.rpcManager = rpcManager;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setAlignmentX(Component.LEFT_ALIGNMENT);

        // RPC Master Toggle (at top)
        rpcEnabledCheck = new JCheckBox("Enable Discord Rich Presence", config.isRpcEnabled());
        rpcEnabledCheck.addActionListener(e -> config.setRpcEnabled(rpcEnabledCheck.isSelected()));
        add(rpcEnabledCheck);
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
        updateStateBtn.addActionListener(e -> config.setCustomState(customStateField.getText().trim()));
        statePanel.add(updateStateBtn);
        add(statePanel);

        // Features Toggles
        add(Box.createVerticalStrut(10));
        add(new JLabel("Rich Presence Features:"));

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

        // Save Button (Auto-save is implemented on action listeners, but a button is
        // reassuring)
        add(Box.createVerticalStrut(20));
        JButton saveBtn = new JButton("Save All Settings");
        saveBtn.addActionListener(e -> saveAll());
        add(saveBtn);
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
