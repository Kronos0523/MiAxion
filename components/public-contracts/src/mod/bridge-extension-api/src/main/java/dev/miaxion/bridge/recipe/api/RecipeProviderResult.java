package dev.miaxion.bridge.recipe.api;

import java.util.List;
import java.util.Objects;

/** A bounded source page; truncation always survives into the base dispatcher. */
public record RecipeProviderResult(List<RecipeCandidate> recipes, boolean truncated, String nextSourceKey,
    RecipeProviderDiagnostics diagnostics, boolean projectionIncomplete) {
    /** Older constructors cannot distinguish overflow from projection loss; stay conservative. */
    public RecipeProviderResult(List<RecipeCandidate> recipes, boolean truncated, String nextSourceKey,
        RecipeProviderDiagnostics diagnostics) {
        this(recipes, truncated, nextSourceKey, diagnostics, truncated);
    }
    public static final int MAX_SOURCE_RECIPES = 4096;
    public RecipeProviderResult(List<RecipeCandidate> recipes, boolean truncated) {
        this(recipes, truncated, null, null);
    }
    /** Retains the 0.1.1 binary constructor for existing third-party addons. */
    public RecipeProviderResult(List<RecipeCandidate> recipes, boolean truncated, String nextSourceKey) {
        this(recipes, truncated, nextSourceKey, null);
    }
    public RecipeProviderResult {
        truncated |= projectionIncomplete;
        Objects.requireNonNull(recipes); if (recipes.size() > MAX_SOURCE_RECIPES || recipes.stream().anyMatch(Objects::isNull)) throw new IllegalArgumentException("Invalid recipe page.");
        RecipeSourcePageCollector.validateKey(nextSourceKey);
        if (nextSourceKey != null && (!truncated || recipes.isEmpty() || !nextSourceKey.equals(recipes.get(recipes.size()-1).typeId() + '|' + recipes.get(recipes.size()-1).recipeId()))) throw new IllegalArgumentException("Invalid recipe continuation.");
        recipes = List.copyOf(recipes);
    }
    public static RecipeProviderResult empty() { return new RecipeProviderResult(List.of(), false); }
}
