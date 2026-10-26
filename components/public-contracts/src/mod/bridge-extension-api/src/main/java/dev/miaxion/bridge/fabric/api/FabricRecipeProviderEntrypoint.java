package dev.miaxion.bridge.fabric.api;

/**
 * Fabric addon entrypoint loaded by the base Bridge during client startup.
 * Implementations must register once; reload readiness belongs to the provider.
 */
@FunctionalInterface
public interface FabricRecipeProviderEntrypoint {
    void register(FabricRecipeProviderRegistrar registrar);
}
