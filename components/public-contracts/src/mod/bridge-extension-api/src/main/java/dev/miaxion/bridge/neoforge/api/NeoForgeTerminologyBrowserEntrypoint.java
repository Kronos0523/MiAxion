package dev.miaxion.bridge.neoforge.api;

/** Addon-owned local browser observation registration, delivered through NeoForge IMC. */
@FunctionalInterface
public interface NeoForgeTerminologyBrowserEntrypoint {
    void register(NeoForgeTerminologyBrowserRegistrar registrar);
}
