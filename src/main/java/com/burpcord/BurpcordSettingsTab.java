package com.burpcord;

import javax.swing.*;
import java.awt.*;

public class BurpcordSettingsTab extends JPanel {

    private final BurpcordConfig config;
    private final DiscordRPCManager rpcManager;

    private final JTextField appIdField;
    private final JSpinner intervalSpinner;
    private final JCheckBox showInterceptCheck;
    private final JCheckBox showScanCheck;
    private final JCheckBox showProxyCheck;
    private final JCheckBox showRepeaterCheck;

    public BurpcordSettingsTab(BurpcordConfig config, DiscordRPCManager rpcManager) {
        this.config = config;
        this.rpcManager = rpcManager;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setAlignmentX(Component.LEFT_ALIGNMENT);

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
            JOptionPane.showMessageDialog(this, "App ID updated and RPC reconnected.");
        }
    }

    private void updateInterval() {
        int val = (Integer) intervalSpinner.getValue();
        config.setUpdateInterval(val);
        rpcManager.shutdown();
        rpcManager.initialize();
    }

    private void saveAll() {
        updateAppId();
        // Toggles are auto-saved.
    }
}
