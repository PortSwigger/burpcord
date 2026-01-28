package tech.chron0.burpcord.ui;

import tech.chron0.burpcord.config.BurpcordConfig;
import tech.chron0.burpcord.discord.DiscordRPCManager;
import tech.chron0.burpcord.core.BurpcordExtension;

import burp.api.montoya.MontoyaApi;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class SettingsPanel extends JPanel {

    private final MontoyaApi api;
    private final BurpcordConfig config;
    private final DiscordRPCManager rpcManager;
    private final LogPanel logPanel;

    // UI Components
    private final JTextField appIdField;
    private final JSpinner intervalSpinner;
    private final JTextField customStateField;
    private final JCheckBox showInterceptCheck;
    private final JCheckBox showScanCheck;
    private final JCheckBox showProxyCheck;
    private final JCheckBox showRepeaterCheck;
    private final JCheckBox showIntruderCheck;
    private final JCheckBox showSiteMapCheck;
    private final JCheckBox showScopeCheck;
    private final JCheckBox showCollaboratorCheck;
    private final JCheckBox showWebSocketsCheck;

    public SettingsPanel(MontoyaApi api, BurpcordConfig config, DiscordRPCManager rpcManager, LogPanel logPanel) {
        this.api = api;
        this.config = config;
        this.rpcManager = rpcManager;
        this.logPanel = logPanel;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Initialize components
        appIdField = new JTextField(config.getAppId(), 20);
        intervalSpinner = new JSpinner(new SpinnerNumberModel(config.getUpdateInterval(), 1, 60, 1));
        customStateField = new JTextField(config.getCustomState(), 25);

        // Feature checkboxes (using helper for cleaner code)
        showInterceptCheck = createCheckboxWithTooltip("Show Intercept on Discord", config.isShowIntercept(),
                "Show intercept on/off status in Discord presence");
        showScanCheck = createCheckboxWithTooltip("Show Scanner on Discord", config.isShowScan(),
                "Show active scan count in Discord presence");
        showProxyCheck = createCheckboxWithTooltip("Show Proxy on Discord", config.isShowProxy(),
                "Show proxy request count in Discord presence");
        showRepeaterCheck = createCheckboxWithTooltip("Show Repeater on Discord", config.isShowRepeater(),
                "Show repeater tab count in Discord presence");
        showIntruderCheck = createCheckboxWithTooltip("Show Intruder on Discord", config.isShowIntruder(),
                "Show intruder attack count in Discord presence");
        showSiteMapCheck = createCheckboxWithTooltip("Show Site Map on Discord", config.isShowSiteMap(),
                "Show site map host count in Discord presence");
        showScopeCheck = createCheckboxWithTooltip("Show Scope on Discord", config.isShowScope(),
                "Show target scope count in Discord presence");
        showCollaboratorCheck = createCheckboxWithTooltip("Show Collaborator on Discord", config.isShowCollaborator(),
                "Show Collaborator interactions (Burp Pro only)");
        showWebSocketsCheck = createCheckboxWithTooltip("Show WebSocket on Discord", config.isShowWebSockets(),
                "Show WebSocket message count in Discord presence");

        // Top section: Connection Settings + Status
        JPanel topSection = new JPanel(new GridLayout(1, 2, 15, 0));
        topSection.add(createConnectionPanel());
        topSection.add(createQuickActionsPanel());

        // Middle section: Feature Toggles (two columns)
        JPanel middleSection = new JPanel(new GridLayout(1, 2, 15, 0));
        middleSection.add(createToolFeaturesPanel());
        middleSection.add(createAdvancedFeaturesPanel());

        // Feature apply button with hint
        JPanel applyPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JButton applyFeaturesBtn = new JButton("⟳ Apply Feature Changes");
        applyFeaturesBtn.setToolTipText("Click to apply feature toggle changes to Discord");
        applyFeaturesBtn.addActionListener(e -> {
            logPanel.clear();
            BurpcordExtension.logBanner();
            BurpcordSettingsTab.log("Applying feature changes...");
            logEnabledFeatures();
            if (rpcManager != null) {
                rpcManager.reloadRPC();
                BurpcordSettingsTab.log("Discord RPC reloaded with new feature settings.");
            } else {
                BurpcordSettingsTab.log("ERROR: RPC Manager not available.");
            }
        });
        JLabel hintLabel = new JLabel("(Click after toggling features)");
        hintLabel.setFont(hintLabel.getFont().deriveFont(Font.ITALIC, 11f));
        applyPanel.add(applyFeaturesBtn);
        applyPanel.add(hintLabel);

        // Combine sections
        JPanel settingsContent = new JPanel();
        settingsContent.setLayout(new BoxLayout(settingsContent, BoxLayout.Y_AXIS));
        settingsContent.add(topSection);
        settingsContent.add(Box.createVerticalStrut(15));
        settingsContent.add(middleSection);
        settingsContent.add(Box.createVerticalStrut(10));
        settingsContent.add(applyPanel);

        add(settingsContent, BorderLayout.NORTH);
        add(logPanel, BorderLayout.CENTER);
    }

    private JPanel createConnectionPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Connection Settings",
                TitledBorder.LEFT, TitledBorder.TOP));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Discord App ID
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Discord App ID:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panel.add(appIdField, gbc);

        // Update Interval
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(new JLabel("Update Interval (sec):"), gbc);
        gbc.gridx = 1;
        intervalSpinner.setPreferredSize(new Dimension(60, 25));
        panel.add(intervalSpinner, gbc);

        // Custom State
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Custom State Text:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panel.add(customStateField, gbc);

        // Save button
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton saveBtn = new JButton("Save Connection Settings");
        saveBtn.setToolTipText("Save Discord App ID, update interval, and custom state text");
        saveBtn.addActionListener(e -> saveConnectionSettings());
        panel.add(saveBtn, gbc);

        return panel;
    }

    private JPanel createQuickActionsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Quick Actions",
                TitledBorder.LEFT, TitledBorder.TOP));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JButton updateStateBtn = new JButton("Update Discord Status Now");
        updateStateBtn.setToolTipText("Force an immediate update to your Discord Rich Presence");
        updateStateBtn.addActionListener(e -> {
            config.setCustomState(customStateField.getText().trim());
            rpcManager.updatePresence("Using Burp Suite");
            BurpcordSettingsTab.log("Discord status updated manually.");
        });
        gbc.gridy = 0;
        panel.add(updateStateBtn, gbc);

        JButton resetBtn = new JButton("Reset All Settings");
        resetBtn.setToolTipText("Reset all Burpcord settings to default values");
        resetBtn.addActionListener(e -> resetSettings());
        gbc.gridy = 1;
        panel.add(resetBtn, gbc);

        JButton disconnectBtn = new JButton("Disconnect from Discord");
        disconnectBtn.setToolTipText("Stop Discord Rich Presence and disconnect from Discord IPC");
        disconnectBtn.addActionListener(e -> {
            rpcManager.shutdown();
            BurpcordSettingsTab.updateConnectionStatusStatic(false);
            BurpcordSettingsTab.log("Disconnected from Discord.");
        });
        gbc.gridy = 2;
        panel.add(disconnectBtn, gbc);

        return panel;
    }

    private JPanel createToolFeaturesPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Tool Activity Features",
                TitledBorder.LEFT, TitledBorder.TOP));

        showInterceptCheck.addActionListener(e -> {
            config.setShowIntercept(showInterceptCheck.isSelected());
            logToggle("Intercept", showInterceptCheck.isSelected());
        });
        showScanCheck.addActionListener(e -> {
            config.setShowScan(showScanCheck.isSelected());
            logToggle("Scanner", showScanCheck.isSelected());
        });
        showProxyCheck.addActionListener(e -> {
            config.setShowProxy(showProxyCheck.isSelected());
            logToggle("Proxy", showProxyCheck.isSelected());
        });
        showRepeaterCheck.addActionListener(e -> {
            config.setShowRepeater(showRepeaterCheck.isSelected());
            logToggle("Repeater", showRepeaterCheck.isSelected());
        });
        showIntruderCheck.addActionListener(e -> {
            config.setShowIntruder(showIntruderCheck.isSelected());
            logToggle("Intruder", showIntruderCheck.isSelected());
        });

        panel.add(showInterceptCheck);
        panel.add(showScanCheck);
        panel.add(showProxyCheck);
        panel.add(showRepeaterCheck);
        panel.add(showIntruderCheck);

        return panel;
    }

    private JPanel createAdvancedFeaturesPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Advanced Features (v1.4+)",
                TitledBorder.LEFT, TitledBorder.TOP));

        showSiteMapCheck.addActionListener(e -> {
            config.setShowSiteMap(showSiteMapCheck.isSelected());
            logToggle("Site Map", showSiteMapCheck.isSelected());
        });
        showScopeCheck.addActionListener(e -> {
            config.setShowScope(showScopeCheck.isSelected());
            logToggle("Scope", showScopeCheck.isSelected());
        });
        showCollaboratorCheck.addActionListener(e -> {
            config.setShowCollaborator(showCollaboratorCheck.isSelected());
            logToggle("Collaborator", showCollaboratorCheck.isSelected());
        });
        showWebSocketsCheck.addActionListener(e -> {
            config.setShowWebSockets(showWebSocketsCheck.isSelected());
            logToggle("WebSocket", showWebSocketsCheck.isSelected());
        });

        panel.add(showSiteMapCheck);
        panel.add(showScopeCheck);
        panel.add(showCollaboratorCheck);
        panel.add(showWebSocketsCheck);

        // Add spacer
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private void saveConnectionSettings() {
        String newId = appIdField.getText().trim();
        if (!newId.isEmpty()) {
            config.setAppId(newId);
        }

        int interval = (Integer) intervalSpinner.getValue();
        config.setUpdateInterval(interval);

        config.setCustomState(customStateField.getText().trim());

        // Restart RPC with new settings
        rpcManager.shutdown();
        rpcManager.restartScheduler();
        rpcManager.initialize();

        BurpcordSettingsTab.log("Connection settings saved and RPC restarted.");
        JOptionPane.showMessageDialog(
                api.userInterface().swingUtils().suiteFrame(),
                "Settings saved and Discord RPC reconnected.",
                "Settings Saved",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void resetSettings() {
        int result = JOptionPane.showConfirmDialog(
                api.userInterface().swingUtils().suiteFrame(),
                "Reset all settings to defaults?",
                "Reset Settings",
                JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            appIdField.setText("1457789738753965206");
            intervalSpinner.setValue(5);
            customStateField.setText("");
            showInterceptCheck.setSelected(true);
            showScanCheck.setSelected(true);
            showProxyCheck.setSelected(true);
            showRepeaterCheck.setSelected(true);
            showIntruderCheck.setSelected(true);
            showSiteMapCheck.setSelected(true);
            showScopeCheck.setSelected(true);
            showCollaboratorCheck.setSelected(true);
            showWebSocketsCheck.setSelected(true);

            saveConnectionSettings();
            BurpcordSettingsTab.log("All settings reset to defaults.");
        }
    }

    private void logToggle(String feature, boolean enabled) {
        BurpcordSettingsTab.log(feature + " display on Discord: " + (enabled ? "ENABLED" : "DISABLED"));
    }

    private void logEnabledFeatures() {
        BurpcordExtension.logEnabledFeatures(config);
    }

    /**
     * Helper method to create a checkbox with a tooltip in one line.
     * Reduces boilerplate for feature checkboxes.
     *
     * @param label    The checkbox label
     * @param selected Initial selection state
     * @param tooltip  The tooltip text
     * @return A configured JCheckBox
     */
    private JCheckBox createCheckboxWithTooltip(String label, boolean selected, String tooltip) {
        JCheckBox checkbox = new JCheckBox(label, selected);
        checkbox.setToolTipText(tooltip);
        return checkbox;
    }
}
