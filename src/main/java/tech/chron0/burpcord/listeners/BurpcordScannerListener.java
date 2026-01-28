package tech.chron0.burpcord.listeners;

import tech.chron0.burpcord.config.BurpcordConfig;
import tech.chron0.burpcord.discord.ActivityProvider;

import burp.api.montoya.scanner.audit.issues.AuditIssue;
import burp.api.montoya.scanner.audit.AuditIssueHandler;

import com.jagrosh.discordipc.entities.RichPresence;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <h1>Scanner Listener</h1>
 * <p>
 * Listens for new audit issues identified by the Burp Scanner.
 * Provides real-time stats on vulnerabilities found.
 * </p>
 * 
 * @author Jon Marien
 * @version 2.0.1
 */
public class BurpcordScannerListener implements AuditIssueHandler, ActivityProvider {

    private final BurpcordConfig config;
    private final AtomicInteger issueCount = new AtomicInteger(0);

    public BurpcordScannerListener(BurpcordConfig config) {
        this.config = config;
    }

    @Override
    public void handleNewAuditIssue(AuditIssue auditIssue) {
        issueCount.incrementAndGet();
    }

    @Override
    public boolean isActive() {
        // Efficient check: AtomicInteger read is instantaneous.
        // Event-driven updates ensure no polling of Scan API is required.
        return config.isShowScan() && issueCount.get() > 0;
    }

    @Override
    public void updatePresence(RichPresence.Builder builder) {
        builder.setDetails("Scanning Targets");
        builder.setState("Issues Found: " + issueCount.get());
        builder.setSmallImage("scanner", "Scanner");
    }
}
