package tech.chron0.burpcord.ui;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class LogPanel extends JPanel {

    private final JTextArea logArea;

    public LogPanel() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Extension Logs",
                TitledBorder.LEFT, TitledBorder.TOP));

        logArea = new JTextArea(12, 60);
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
            BurpcordSettingsTab.log("Logs copied to clipboard.");
        });
        toolbar.add(copyBtn);

        add(toolbar, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void appendLog(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append("[" + java.time.LocalTime.now().toString().substring(0, 8) + "] " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    public void clear() {
        SwingUtilities.invokeLater(() -> logArea.setText(""));
    }
}
