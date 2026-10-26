package dev.miaxion.bridge.neoforge.api;

import dev.miaxion.bridge.recipe.api.RecipeProvider;
import java.util.function.BooleanSupplier;

/** Public, loader-specific registration port supplied by the NeoForge base Bridge. */
public interface NeoForgeRecipeProviderRegistrar {
    BooleanSupplier gameReady();

    RegistrationResult register(String addonId, String addonVersion, String targetModId,
        RecipeProvider provider);

    enum RegistrationResult { ACCEPTED, FROZEN, INVALID, DUPLICATE }
}
