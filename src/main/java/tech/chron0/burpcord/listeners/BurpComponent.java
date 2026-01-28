package tech.chron0.burpcord.listeners;

import burp.api.montoya.MontoyaApi;

public interface BurpComponent {
    void register(MontoyaApi api);
}
