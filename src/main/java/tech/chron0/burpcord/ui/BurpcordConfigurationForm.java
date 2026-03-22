package tech.chron0.burpcord.ui;

import tech.chron0.burpcord.config.BurpcordConfig;
import tech.chron0.burpcord.discord.DiscordRPCManager;
import tech.chron0.burpcord.core.BurpcordExtension;

import burp.api.montoya.MontoyaApi;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * Main configuration form (connection, toggles, embedded log) shown inside Burp Settings.
 */
public class BurpcordConfigurationForm extends JPanel {

    /**
     * Declarative definition for a feature toggle checkbox.
     * Each record binds a UI label/tooltip to its config getter and setter.
     */
    private record FeatureToggle(
            String label,
            String tooltip,
            String logName,
            BooleanSupplier getter,
            Consumer<Boolean> setter) {
    }

    private final MontoyaApi api;
    private final BurpcordConfig config;
    private final DiscordRPCManager rpcManager;

    // UI Components
    private final JTextField appIdField;
    private final JSpinner intervalSpinner;
    private final JTextField customStateField;
    private final List<FeatureToggle> toolFeatures;
    private final List<FeatureToggle> advancedFeatures;
    private final List<JCheckBox> allCheckboxes = new java.util.ArrayList<>();

    public BurpcordConfigurationForm(MontoyaApi api, BurpcordConfig config, DiscordRPCManager rpcManager,
            LogPanel logPanel) {
        this.api = api;
        this.config = config;
        this.rpcManager = rpcManager;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Initialize components
        appIdField = new JTextField(config.getAppId(), 20);
        intervalSpinner = new JSpinner(new SpinnerNumberModel(config.getUpdateInterval(), 1, 60, 1));
        customStateField = new JTextField(config.getCustomState(), 25);

        // Feature toggle definitions — declarative, data-driven
        toolFeatures = List.of(
                new FeatureToggle("Show Intercept on Discord",
                        "Show intercept on/off status in Discord presence",
                        "Intercept", config::isShowIntercept, config::setShowIntercept),
                new FeatureToggle("Show Scanner on Discord",
                        "Show active scan count in Discord presence",
                        "Scanner", config::isShowScan, config::setShowScan),
                new FeatureToggle("Show Proxy on Discord",
                        "Show proxy request count in Discord presence",
                        "Proxy", config::isShowProxy, config::setShowProxy),
                new FeatureToggle("Show Repeater on Discord",
                        "Show repeater tab count in Discord presence",
                        "Repeater", config::isShowRepeater, config::setShowRepeater),
                new FeatureToggle("Show Intruder on Discord",
                        "Show intruder attack count in Discord presence",
                        "Intruder", config::isShowIntruder, config::setShowIntruder));

        advancedFeatures = List.of(
                new FeatureToggle("Show Site Map on Discord",
                        "Primary: unique URLs seen via Proxy (bounded). Periodically reconciles with full site map count on a long interval.",
                        "Site Map", config::isShowSiteMap, config::setShowSiteMap),
                new FeatureToggle("Show Scope on Discord",
                        "Show target scope count in Discord presence",
                        "Scope", config::isShowScope, config::setShowScope),
                new FeatureToggle("Show Collaborator on Discord",
                        "Show Collaborator interactions (Burp Pro only)",
                        "Collaborator", config::isShowCollaborator, config::setShowCollaborator),
                new FeatureToggle("Show WebSocket on Discord",
                        "Show WebSocket message count in Discord presence",
                        "WebSocket", config::isShowWebSockets, config::setShowWebSockets));

        // Top section: Connection Settings + Status
        JPanel topSection = new JPanel(new GridLayout(1, 2, 15, 0));
        topSection.add(createConnectionPanel());
        topSection.add(createQuickActionsPanel());

        // Middle section: Feature Toggles (two columns)
        JPanel middleSection = new JPanel(new GridLayout(1, 2, 15, 0));
        middleSection.add(createFeaturePanel("Tool Activity Features", toolFeatures));
        middleSection.add(createFeaturePanel("Advanced Features", advancedFeatures));

        // Feature apply button with hint
        JPanel applyPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JButton applyFeaturesBtn = new JButton("\u27F3 Apply Feature Changes");
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

    /**
     * Builds a titled feature panel from a list of toggle definitions.
     * Wires each checkbox to its config setter and log output.
     */
    private JPanel createFeaturePanel(String title, List<FeatureToggle> features) {
        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), title,
                TitledBorder.LEFT, TitledBorder.TOP));

        for (FeatureToggle toggle : features) {
            JCheckBox checkbox = new JCheckBox(toggle.label(), toggle.getter().getAsBoolean());
            checkbox.setToolTipText(toggle.tooltip());
            checkbox.addActionListener(e -> {
                toggle.setter().accept(checkbox.isSelected());
                logToggle(toggle.logName(), checkbox.isSelected());
            });
            panel.add(checkbox);
            allCheckboxes.add(checkbox);
        }

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
        rpcManager.reloadRPC();

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
            appIdField.setText(BurpcordConfig.DEFAULT_APP_ID);
            intervalSpinner.setValue(BurpcordConfig.DEFAULT_UPDATE_INTERVAL);
            customStateField.setText("");
            allCheckboxes.forEach(cb -> cb.setSelected(true));

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
}
