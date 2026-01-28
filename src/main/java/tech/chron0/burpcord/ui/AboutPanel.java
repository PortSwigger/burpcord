package tech.chron0.burpcord.ui;

import tech.chron0.burpcord.core.BurpcordConstants;

import javax.swing.*;
import java.awt.*;
import java.net.URI;

public class AboutPanel extends JPanel {

    private static final String AUTHOR = "Jon Marien";
    private static final String GITHUB_URL = "https://github.com/jondmarien/Burpcord";

    public AboutPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

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
        JLabel versionLabel = new JLabel("Version " + BurpcordConstants.VERSION);
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

        add(centerPanel, BorderLayout.CENTER);
    }

    private void openUrl(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            BurpcordSettingsTab.log("Failed to open URL: " + url);
        }
    }
}
