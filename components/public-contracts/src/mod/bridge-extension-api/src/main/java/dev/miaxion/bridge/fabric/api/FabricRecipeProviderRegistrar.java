package dev.miaxion.bridge.fabric.api;

import dev.miaxion.bridge.recipe.api.RecipeProvider;
import java.util.function.BooleanSupplier;

/** Public, minimal typed recipe registration port supplied only by the base Bridge. */
public interface FabricRecipeProviderRegistrar {
    /** Current client-world readiness; this is not a permission or a world handle. */
    BooleanSupplier gameReady();

    /**
     * Registers one stable provider identity. The provider may subsequently
     * transition between pending and ready as its target mod reloads.
     */
    RegistrationResult register(String addonId, String addonVersion, String targetModId, RecipeProvider provider);

    enum RegistrationResult { ACCEPTED, FROZEN, INVALID, DUPLICATE }
}
