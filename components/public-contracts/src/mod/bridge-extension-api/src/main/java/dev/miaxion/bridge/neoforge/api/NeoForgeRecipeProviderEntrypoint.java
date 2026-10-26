package dev.miaxion.bridge.neoforge.api;

/**
 * Implemented by a NeoForge recipe addon and supplied to the base through
 * {@code miaxion:recipe_provider}; an addon registers exactly once at load.
 */
@FunctionalInterface
public interface NeoForgeRecipeProviderEntrypoint {
    void register(NeoForgeRecipeProviderRegistrar registrar);
}
