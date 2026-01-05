package com.burpcord;

import burp.api.montoya.scanner.audit.AuditIssueHandler;
import burp.api.montoya.scanner.audit.issues.AuditIssue;
import burp.api.montoya.scanner.audit.issues.AuditIssueSeverity;

public class BurpcordScannerListener implements AuditIssueHandler {

    private final DiscordRPCManager manager;

    public BurpcordScannerListener(DiscordRPCManager manager) {
        this.manager = manager;
    }

    @Override
    public void handleNewAuditIssue(AuditIssue auditIssue) {
        AuditIssueSeverity severity = auditIssue.severity();

        switch (severity) {
            case HIGH -> manager.incrementVulnHigh();
            case MEDIUM -> manager.incrementVulnMedium();
            case LOW -> manager.incrementVulnLow();
            case INFORMATION -> manager.incrementVulnInfo();
            default -> manager.incrementVulnInfo(); // Fallback for undefined
        }
    }
}
