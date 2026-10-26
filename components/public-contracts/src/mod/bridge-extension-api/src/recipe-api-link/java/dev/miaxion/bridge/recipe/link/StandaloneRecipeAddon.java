package dev.miaxion.bridge.recipe.link;

import dev.miaxion.bridge.fabric.api.FabricRecipeProviderEntrypoint;
import dev.miaxion.bridge.fabric.api.FabricRecipeProviderRegistrar;
import dev.miaxion.bridge.recipe.api.RecipeAlternativeCandidate;
import dev.miaxion.bridge.recipe.api.RecipeCandidate;
import dev.miaxion.bridge.recipe.api.RecipeIdentityCandidate;
import dev.miaxion.bridge.recipe.api.RecipeIdentityFactory;
import dev.miaxion.bridge.recipe.api.RecipeProvider;
import dev.miaxion.bridge.recipe.api.RecipeProviderDiagnostics;
import dev.miaxion.bridge.recipe.api.RecipeProviderResult;
import dev.miaxion.bridge.recipe.api.RecipeProviderStatus;
import dev.miaxion.bridge.recipe.api.RecipeQuery;
import dev.miaxion.bridge.recipe.api.RecipeSlotCandidate;
import java.util.List;
import java.util.Map;

/** Compiled only against recipeApiJar; this is not part of the deployable API. */
public final class StandaloneRecipeAddon implements FabricRecipeProviderEntrypoint, RecipeProvider {
    @Override public void register(FabricRecipeProviderRegistrar registrar) {
        registrar.register("recipe_link", "1.0.0", "examplemod", this);
    }
    @Override public boolean supportsTypedResources() { return true; }
    @Override public RecipeProviderStatus status() { return RecipeProviderStatus.READY; }
    @Override public RecipeProviderResult query(RecipeQuery query) { return result(); }
    @Override public RecipeProviderResult uses(RecipeQuery query) { return result(); }
    static RecipeProviderDiagnostics projectionDiagnostics() {
        return new RecipeProviderDiagnostics(0, 0, 0, 0, 0, 1, Map.of(), Map.of("example:loot", 1));
    }
    private static RecipeProviderResult result() {
        var slot = new RecipeSlotCandidate(List.of(new RecipeAlternativeCandidate("minecraft:stone", 1),
            new RecipeAlternativeCandidate(new dev.miaxion.bridge.recipe.api.RecipeResourceValue(
                "fluid", "example:fluid", "example.fluid", "Fluid", "270", "1", "mB"))));
        var derived = RecipeIdentityFactory.derived(new com.fasterxml.jackson.databind.ObjectMapper().createObjectNode().put("recipe", "link"));
        var recipe = new RecipeCandidate(derived.recipeId(), "minecraft:crafting", "examplemod", List.of(slot), List.of(), List.of(slot), derived.identity());
        return new RecipeProviderResult(List.of(recipe), false);
    }
}
