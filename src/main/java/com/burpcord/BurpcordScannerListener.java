package com.burpcord;

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

public class BurpcordScannerListener implements AuditIssueHandler, PassiveScanCheck, ActiveScanCheck {

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
