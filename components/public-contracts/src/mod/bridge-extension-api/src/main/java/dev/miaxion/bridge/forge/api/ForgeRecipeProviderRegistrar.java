package dev.miaxion.bridge.forge.api;
import java.util.function.BooleanSupplier;
import dev.miaxion.bridge.recipe.api.RecipeProvider;
/** Compile-only API; the Forge base owns its sole runtime definition. */
public interface ForgeRecipeProviderRegistrar {
    BooleanSupplier gameReady();
    RegistrationResult register(String addonId, String addonVersion, String targetModId, RecipeProvider provider);
    enum RegistrationResult { ACCEPTED, FROZEN, INVALID, DUPLICATE }
}
