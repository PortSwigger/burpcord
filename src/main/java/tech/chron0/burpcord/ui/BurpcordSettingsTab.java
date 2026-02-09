package tech.chron0.burpcord.ui;

import tech.chron0.burpcord.config.BurpcordConfig;
import tech.chron0.burpcord.discord.DiscordRPCManager;

import burp.api.montoya.MontoyaApi;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

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
 * @version 2.2.1
 * @see BurpcordConfig
 * @see DiscordRPCManager
 */
public class BurpcordSettingsTab extends JPanel {

    /** Singleton instance for static log access. */
    private static BurpcordSettingsTab instance;

    /** Buffer for logs that arrive before the UI is ready. */
    private static final List<String> logBuffer = new ArrayList<>();

    private final DiscordRPCManager rpcManager;
    private final LogPanel logPanel;
    private final JLabel statusLabel;

    /**
     * Creates a new settings tab with all UI components.
     * 
     * @param api        The Montoya API instance
     * @param config     The configuration provider
     * @param rpcManager The Discord RPC manager
     */
    public BurpcordSettingsTab(MontoyaApi api, BurpcordConfig config, DiscordRPCManager rpcManager) {
        this.rpcManager = rpcManager;

        setLayout(new BorderLayout());

        // Create main tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();

        // Initialize Panels
        this.logPanel = new LogPanel();
        SettingsPanel settingsPanel = new SettingsPanel(api, config, rpcManager, logPanel);
        AboutPanel aboutPanel = new AboutPanel();
        HelpPanel helpPanel = new HelpPanel();

        // Status Label Init
        statusLabel = new JLabel();

        // Create tabs
        tabbedPane.addTab("Settings", settingsPanel);
        tabbedPane.addTab("About", aboutPanel);
        tabbedPane.addTab("Help", helpPanel);

        // Status bar at top
        JPanel statusBar = createStatusBar();

        add(statusBar, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);

        // Apply Burp's theme to all components
        api.userInterface().applyThemeToComponent(this);

        instance = this;
        updateConnectionStatus(false);

        // Flush buffered logs
        flushLogBuffer();
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
     * Logs a message to the built-in log viewer and standard output.
     * Buffers messages if the UI is not yet initialized.
     */
    public static void log(String message) {
        // 1. Always print to standard output (captured by Burp Extension "Output" tab)
        System.out.println(message);

        // 2. If UI is ready, append to log panel
        if (instance != null && instance.logPanel != null) {
            instance.logPanel.appendLog(message);
        } else {
            // 3. Otherwise, buffer it
            synchronized (logBuffer) {
                logBuffer.add(message);
            }
        }
    }

    private void flushLogBuffer() {
        synchronized (logBuffer) {
            for (String msg : logBuffer) {
                logPanel.appendLog(msg);
            }
            logBuffer.clear();
        }
    }
}
