package dev.miaxion.bridge.neoforge.api;

/** Single local-browser port. The base owns consent, retention and publication. */
public interface NeoForgeTerminologyBrowserRegistrar {
    RegistrationResult register(String addonId, String targetModId, TerminologyBrowserProvider provider);
    enum RegistrationResult { ACCEPTED, FROZEN, INVALID, DUPLICATE }
}
