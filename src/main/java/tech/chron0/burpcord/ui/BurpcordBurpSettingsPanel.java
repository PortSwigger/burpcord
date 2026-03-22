package tech.chron0.burpcord.ui;

import javax.swing.JComponent;
import java.util.Set;

/**
 * Adapts the Burpcord Swing UI for {@link burp.api.montoya.ui.settings.SettingsPanel}
 * (Montoya type referenced fully qualified to avoid clashing with {@link BurpcordConfigurationForm}).
 */
public final class BurpcordBurpSettingsPanel implements burp.api.montoya.ui.settings.SettingsPanel {

    private final JComponent root;

    public BurpcordBurpSettingsPanel(JComponent root) {
        this.root = root;
    }

    @Override
    public JComponent uiComponent() {
        return root;
    }

    @Override
    public Set<String> keywords() {
        return Set.of(
                "Burpcord",
                "Discord",
                "Rich Presence",
                "RPC",
                "Discord IPC");
    }
}
