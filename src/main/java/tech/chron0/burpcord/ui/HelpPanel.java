package tech.chron0.burpcord.ui;

import javax.swing.*;
import java.awt.*;

public class HelpPanel extends JPanel {

    private static final String GITHUB_URL = "https://github.com/jondmarien/Burpcord";

    public HelpPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JTextArea helpText = new JTextArea();
        helpText.setEditable(false);
        helpText.setLineWrap(true);
        helpText.setWrapStyleWord(true);
        helpText.setFont(helpText.getFont().deriveFont(12f));
        helpText.setText(getHelpText());

        JScrollPane scrollPane = new JScrollPane(helpText);
        add(scrollPane, BorderLayout.CENTER);
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
                4. Open Burp Settings (gear) and search for "Burpcord" to configure

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
                • Site Map: Unique URLs seen via Proxy (bounded); periodic full site map count
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
}
