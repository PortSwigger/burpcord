package tech.chron0.burpcord.listeners;

import tech.chron0.burpcord.discord.DiscordRPCManager;

import burp.api.montoya.http.Http;
import burp.api.montoya.http.message.HttpRequestResponse;

import burp.api.montoya.scanner.AuditResult;

import burp.api.montoya.scanner.audit.AuditIssueHandler;
import burp.api.montoya.scanner.audit.insertionpoint.AuditInsertionPoint;
import burp.api.montoya.scanner.audit.issues.AuditIssue;

import burp.api.montoya.scanner.scancheck.PassiveScanCheck;
import burp.api.montoya.scanner.scancheck.ActiveScanCheck;

import burp.api.montoya.scanner.audit.issues.AuditIssueSeverity;
import burp.api.montoya.scanner.ConsolidationAction;

/**
 * Handles Burp Suite Scanner events for Discord Rich Presence updates.
 * 
 * <p>
 * This listener monitors both active and passive scan activity, as well as
 * discovered vulnerabilities. It categorizes findings by severity (High,
 * Medium,
 * Low, Information) and reports them to the {@link DiscordRPCManager}.
 * </p>
 * 
 * <p>
 * Implements multiple interfaces to receive comprehensive scan events:
 * </p>
 * <ul>
 * <li>{@link AuditIssueHandler} - Receives discovered vulnerability
 * notifications</li>
 * <li>{@link PassiveScanCheck} - Monitors passive scan activity</li>
 * <li>{@link ActiveScanCheck} - Monitors active scan activity</li>
 * </ul>
 * 
 * @author Jon Marien
 * @version 1.3
 * @see DiscordRPCManager
 */
public class BurpcordScannerListener implements AuditIssueHandler, PassiveScanCheck, ActiveScanCheck {

    /** Reference to the RPC manager for status updates. */
    private final DiscordRPCManager manager;

    /**
     * Creates a new scanner listener.
     * 
     * @param manager The Discord RPC manager to notify of scan activity
     */
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

    @Override
    public String checkName() {
        return "Burpcord Scanner";
    }

    @Override
    public AuditResult doCheck(HttpRequestResponse baseRequestResponse) {
        manager.markPassiveScan();
        return AuditResult.auditResult(java.util.Collections.emptyList());
    }

    @Override
    public AuditResult doCheck(HttpRequestResponse baseRequestResponse, AuditInsertionPoint insertionPoint, Http http) {
        manager.markActiveScan();
        return AuditResult.auditResult(java.util.Collections.emptyList());
    }

    @Override
    public ConsolidationAction consolidateIssues(AuditIssue newIssue, AuditIssue existingIssue) {
        return ConsolidationAction.KEEP_EXISTING;
    }
}
