package com.burpcord;

import burp.api.montoya.MontoyaApi;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.net.URI;

/**
 * Settings tab UI for the Burpcord extension in Burp Suite.
 * 
 * <p>
 * This class creates and manages the graphical user interface for configuring
 * Burpcord settings. It appears as a tab in the main Burp Suite window and
 * provides a tabbed interface for Settings, About, and Help sections.
 * </p>
 * 
 * <h2>Thread Safety</h2>
 * <p>
 * The static {@link #log(String)} method uses
 * {@link SwingUtilities#invokeLater(Runnable)}
 * to ensure thread-safe UI updates from any thread.
 * </p>
 * 
 * @author Jon Marien
 * @version 1.5
 * @see BurpcordConfig
 * @see DiscordRPCManager
 */
public class BurpcordSettingsTab extends JPanel {

    private static final String VERSION = "1.5.0";
    private static final String AUTHOR = "Jon Marien";
    private static final String GITHUB_URL = "https://github.com/jondmarien/Burpcord";

    /** Montoya API instance for accessing Burp Suite features. */
    private final MontoyaApi api;
    /** Configuration provider for reading/writing preferences. */
    private final BurpcordConfig config;
    /** RPC manager for triggering reconnections. */
    private final DiscordRPCManager rpcManager;

    // UI Components
    private final JTextField appIdField;
    private final JSpinner intervalSpinner;
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
    private final JLabel statusLabel;

    /** Singleton instance for static log access. */
    private static BurpcordSettingsTab instance;

    /**
     * Creates a new settings tab with all UI components.
     * 
     * @param api        The Montoya API instance
     * @param config     The configuration provider
     * @param rpcManager The Discord RPC manager
     */
    public BurpcordSettingsTab(MontoyaApi api, BurpcordConfig config, DiscordRPCManager rpcManager) {
        this.api = api;
        this.config = config;
        this.rpcManager = rpcManager;

        setLayout(new BorderLayout());

        // Create main tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();

        // Initialize UI components before creating tabs
        appIdField = new JTextField(config.getAppId(), 20);
        intervalSpinner = new JSpinner(new SpinnerNumberModel(config.getUpdateInterval(), 1, 60, 1));
        customStateField = new JTextField(config.getCustomState(), 25);
        logArea = new JTextArea(12, 60);
        statusLabel = new JLabel();

        // Feature checkboxes - Display status on Discord
        showInterceptCheck = new JCheckBox("Show Intercept on Discord", config.isShowIntercept());
        showScanCheck = new JCheckBox("Show Scanner on Discord", config.isShowScan());
        showProxyCheck = new JCheckBox("Show Proxy on Discord", config.isShowProxy());
        showRepeaterCheck = new JCheckBox("Show Repeater on Discord", config.isShowRepeater());
        showIntruderCheck = new JCheckBox("Show Intruder on Discord", config.isShowIntruder());
        showSiteMapCheck = new JCheckBox("Show Site Map on Discord", config.isShowSiteMap());
        showScopeCheck = new JCheckBox("Show Scope on Discord", config.isShowScope());
        showCollaboratorCheck = new JCheckBox("Show Collaborator on Discord", config.isShowCollaborator());
        showWebSocketsCheck = new JCheckBox("Show WebSocket on Discord", config.isShowWebSockets());

        // Create tabs
        tabbedPane.addTab("Settings", createSettingsPanel());
        tabbedPane.addTab("About", createAboutPanel());
        tabbedPane.addTab("Help", createHelpPanel());

        // Status bar at top
        JPanel statusBar = createStatusBar();

        add(statusBar, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);

        // Apply Burp's theme to all components
        api.userInterface().applyThemeToComponent(this);

        instance = this;
        updateConnectionStatus(false);
    }

    /**
     * Creates the status bar with connection indicator.
     */
    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));

        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD, 12f));
        statusBar.add(statusLabel, BorderLayout.WEST);

        JButton reloadBtn = new JButton("Reload Discord RPC");
        reloadBtn.addActionListener(e -> {
            log("Reloading Discord RPC...");
            rpcManager.shutdown();
            rpcManager.initialize();
        });
        statusBar.add(reloadBtn, BorderLayout.EAST);

        return statusBar;
    }

    /**
     * Creates the main Settings panel.
     */
    private JPanel createSettingsPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Top section: Connection Settings + Status
        JPanel topSection = new JPanel(new GridLayout(1, 2, 15, 0));
        topSection.add(createConnectionPanel());
        topSection.add(createQuickActionsPanel());

        // Middle section: Feature Toggles (two columns)
        JPanel middleSection = new JPanel(new GridLayout(1, 2, 15, 0));
        middleSection.add(createToolFeaturesPanel());
        middleSection.add(createAdvancedFeaturesPanel());

        // Bottom section: Log viewer
        JPanel bottomSection = createLogPanel();

        // Combine sections
        JPanel settingsContent = new JPanel();
        settingsContent.setLayout(new BoxLayout(settingsContent, BoxLayout.Y_AXIS));
        settingsContent.add(topSection);
        settingsContent.add(Box.createVerticalStrut(15));
        settingsContent.add(middleSection);

        mainPanel.add(settingsContent, BorderLayout.NORTH);
        mainPanel.add(bottomSection, BorderLayout.CENTER);

        return mainPanel;
    }

    /**
     * Creates the connection settings panel.
     */
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
        saveBtn.addActionListener(e -> saveConnectionSettings());
        panel.add(saveBtn, gbc);

        return panel;
    }

    /**
     * Creates the quick actions panel.
     */
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
        updateStateBtn.addActionListener(e -> {
            config.setCustomState(customStateField.getText().trim());
            rpcManager.updatePresence("Using Burp Suite");
            log("Discord status updated manually.");
        });
        gbc.gridy = 0;
        panel.add(updateStateBtn, gbc);

        JButton resetBtn = new JButton("Reset All Settings");
        resetBtn.addActionListener(e -> resetSettings());
        gbc.gridy = 1;
        panel.add(resetBtn, gbc);

        JButton disconnectBtn = new JButton("Disconnect from Discord");
        disconnectBtn.addActionListener(e -> {
            rpcManager.shutdown();
            updateConnectionStatus(false);
            log("Disconnected from Discord.");
        });
        gbc.gridy = 2;
        panel.add(disconnectBtn, gbc);

        return panel;
    }

    /**
     * Creates the tool features toggle panel.
     */
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

    /**
     * Creates the advanced features toggle panel.
     */
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

    /**
     * Creates the log viewer panel.
     */
    private JPanel createLogPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Extension Logs",
                TitledBorder.LEFT, TitledBorder.TOP));

        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setPreferredSize(new Dimension(800, 200));

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));

        JButton clearBtn = new JButton("Clear Logs");
        clearBtn.addActionListener(e -> logArea.setText(""));
        toolbar.add(clearBtn);

        JButton copyBtn = new JButton("Copy to Clipboard");
        copyBtn.addActionListener(e -> {
            logArea.selectAll();
            logArea.copy();
            logArea.setCaretPosition(logArea.getDocument().getLength());
            log("Logs copied to clipboard.");
        });
        toolbar.add(copyBtn);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Creates the About panel.
     */
    private JPanel createAboutPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        // Center content
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        // Title
        JLabel titleLabel = new JLabel("Burpcord");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 28f));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(titleLabel);

        JLabel subtitleLabel = new JLabel("Discord Rich Presence for Burp Suite");
        subtitleLabel.setFont(subtitleLabel.getFont().deriveFont(Font.ITALIC, 14f));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(subtitleLabel);

        centerPanel.add(Box.createVerticalStrut(20));

        // Version & Author
        JLabel versionLabel = new JLabel("Version " + VERSION);
        versionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(versionLabel);

        JLabel authorLabel = new JLabel("Developed by " + AUTHOR);
        authorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(authorLabel);

        centerPanel.add(Box.createVerticalStrut(30));

        // Links
        JPanel linksPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));

        JButton githubBtn = new JButton("View on GitHub");
        githubBtn.addActionListener(e -> openUrl(GITHUB_URL));
        linksPanel.add(githubBtn);

        JButton issueBtn = new JButton("Report an Issue");
        issueBtn.addActionListener(e -> openUrl(GITHUB_URL + "/issues"));
        linksPanel.add(issueBtn);

        linksPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(linksPanel);

        centerPanel.add(Box.createVerticalStrut(30));

        // Features list
        JPanel featuresPanel = new JPanel();
        featuresPanel.setBorder(BorderFactory.createTitledBorder("Features"));
        featuresPanel.setLayout(new BoxLayout(featuresPanel, BoxLayout.Y_AXIS));

        String[] features = {
                "• Real-time activity tracking on Discord",
                "• Intercept, Scanner, Proxy, Repeater, Intruder status",
                "• Site Map, Scope, Collaborator, WebSocket tracking",
                "• Customizable state text",
                "• Configurable update interval",
                "• Built-in log viewer"
        };

        for (String feature : features) {
            JLabel featureLabel = new JLabel(feature);
            featureLabel.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
            featuresPanel.add(featureLabel);
        }

        featuresPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(featuresPanel);

        panel.add(centerPanel, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Creates the Help panel.
     */
    private JPanel createHelpPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JTextArea helpText = new JTextArea();
        helpText.setEditable(false);
        helpText.setLineWrap(true);
        helpText.setWrapStyleWord(true);
        helpText.setFont(helpText.getFont().deriveFont(12f));
        helpText.setText(getHelpText());

        JScrollPane scrollPane = new JScrollPane(helpText);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private String getHelpText() {
        return """
                BURPCORD HELP
                =============

                GETTING STARTED
                ---------------
                1. Ensure Discord desktop is running on your computer
                2. Burpcord will automatically connect to Discord IPC
                3. Your Burp Suite activity will appear on your Discord profile

                CONNECTION SETTINGS
                -------------------
                • Discord App ID: The application ID from Discord Developer Portal
                  (default works out of the box)
                • Update Interval: How often the Discord status updates (1-60 seconds)
                • Custom State Text: Your custom message shown as the activity state

                TOOL FEATURES
                -------------
                Toggle which Burp Suite tools are tracked:
                • Intercept: Shows when proxy intercept is active
                • Scanner: Displays vulnerability scan results
                • Proxy: Shows proxied request/response counts
                • Repeater: Indicates manual testing activity
                • Intruder: Shows fuzzing attack progress

                ADVANCED FEATURES (v1.4+)
                -------------------------
                • Site Map: Displays mapped endpoint count
                • Scope: Shows in-scope target count
                • Collaborator: Displays OOB interaction hits (Pro only)
                • WebSocket: Tracks WebSocket message counts

                TROUBLESHOOTING
                ---------------
                • Status not showing: Check Discord Activity Status is enabled
                • Connection failed: Restart Discord, click "Reload Discord RPC"
                • Wrong images: Verify App ID and that assets are uploaded

                For more help, visit: """ + GITHUB_URL + """


                NOTES
                -----
                • Burpcord only displays activity type and counts
                • No target URLs or request data is shared with Discord
                • Settings are automatically saved to Burp preferences
                """;
    }

    /**
     * Updates the connection status indicator.
     */
    public void updateConnectionStatus(boolean connected) {
        SwingUtilities.invokeLater(() -> {
            if (connected) {
                statusLabel.setText("● Connected to Discord");
                statusLabel.setForeground(new Color(76, 175, 80)); // Green
            } else {
                statusLabel.setText("○ Disconnected");
                statusLabel.setForeground(new Color(244, 67, 54)); // Red
            }
        });
    }

    /**
     * Static method to update connection status from other classes.
     */
    public static void updateConnectionStatusStatic(boolean connected) {
        if (instance != null) {
            instance.updateConnectionStatus(connected);
        }
    }

    /**
     * Helper method to log when a feature is toggled.
     */
    private void logToggle(String feature, boolean enabled) {
        log(feature + " display on Discord: " + (enabled ? "ENABLED" : "DISABLED"));
    }

    /**
     * Logs a message to the built-in log viewer.
     */
    public static void log(String message) {
        if (instance != null && instance.logArea != null) {
            SwingUtilities.invokeLater(() -> {
                instance.logArea
                        .append("[" + java.time.LocalTime.now().toString().substring(0, 8) + "] " + message + "\n");
                instance.logArea.setCaretPosition(instance.logArea.getDocument().getLength());
            });
        }
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

        log("Connection settings saved and RPC restarted.");
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
            log("All settings reset to defaults.");
        }
    }

    private void openUrl(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            log("Failed to open URL: " + url);
        }
    }
}
