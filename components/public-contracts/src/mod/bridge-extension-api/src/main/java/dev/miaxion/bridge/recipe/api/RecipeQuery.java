package dev.miaxion.bridge.recipe.api;

import com.fasterxml.jackson.databind.JsonNode;

/** Validated input filter. Jackson is supplied by the base Bridge at runtime. */
public record RecipeQuery(String itemId, JsonNode components, String matchMode, String preferredRecipeId, int sourcePageSize, String afterSourceKey, String resourceKind, String ingredientTypeUid) {
    public RecipeQuery(String itemId, JsonNode components, String matchMode, String preferredRecipeId, int sourcePageSize, String afterSourceKey) {
        this(itemId, components, matchMode, preferredRecipeId, sourcePageSize, afterSourceKey, "item", null);
    }
    public RecipeQuery(String itemId) { this(itemId, null, "item", null); }
    public RecipeQuery(String itemId, JsonNode components, String matchMode, String preferredRecipeId) { this(itemId, components, matchMode, preferredRecipeId, RecipeProviderResult.MAX_SOURCE_RECIPES, null); }
    public RecipeQuery {
        resourceKind = resourceKind == null ? "item" : resourceKind;
        if (!java.util.Set.of("item", "fluid", "chemical", "energy", "custom").contains(resourceKind)
            || ingredientTypeUid != null && (ingredientTypeUid.isBlank() || ingredientTypeUid.length() > 512)) throw new IllegalArgumentException("Invalid target resource type.");
        if (!isResourceLocation(itemId) || (preferredRecipeId != null && !isResourceLocation(preferredRecipeId))) throw new IllegalArgumentException("Invalid resource location.");
        matchMode = matchMode == null ? "item" : matchMode;
        if (!(matchMode.equals("item") || matchMode.equals("subset-components") || matchMode.equals("exact-components")) || (components != null && !components.isObject())) throw new IllegalArgumentException("Invalid recipe match.");
        if (sourcePageSize < 1 || sourcePageSize > RecipeProviderResult.MAX_SOURCE_RECIPES) throw new IllegalArgumentException("Invalid source page size.");
        RecipeSourcePageCollector.validateKey(afterSourceKey); components = components == null ? null : components.deepCopy();
    }
    @Override public JsonNode components() { return components == null ? null : components.deepCopy(); }
    public boolean shouldProjectRegisteredRecipe(String id) { return preferredRecipeId == null || id == null || preferredRecipeId.equals(id); }
    public boolean matches(RecipeCandidate candidate, boolean uses) {
        if (preferredRecipeId != null && !preferredRecipeId.equals(candidate.recipeId())) return false;
        var slots = uses ? java.util.stream.Stream.concat(candidate.inputs().stream(), candidate.catalysts().stream()).toList() : candidate.outputs();
        for (var slot : slots) for (var alternative : slot.alternatives()) {
            if (!itemId.equals(alternative.resourceId())) continue;
            boolean legacyTypedAlias = resourceKind.equals("custom") && ingredientTypeUid != null && alternative.resource() != null
                && ingredientTypeUid.equals(alternative.resource().ingredientTypeUid()) && !alternative.resourceKind().equals("item");
            if (!resourceKind.equals(alternative.resourceKind()) && !legacyTypedAlias) continue;
            if (ingredientTypeUid != null && (alternative.resource() == null || !ingredientTypeUid.equals(alternative.resource().ingredientTypeUid()))) continue;
            if (components == null || matchMode.equals("item")) return true;
            var actual = alternative.components();
            if (actual == null) continue;
            if (matchMode.equals("exact-components") && components.equals(actual)) return true;
            if (matchMode.equals("subset-components")) {
                boolean match = true; var names = components.fieldNames();
                while (names.hasNext()) { var name = names.next(); if (!components.get(name).equals(actual.get(name))) { match = false; break; } }
                if (match) return true;
            }
        }
        return false;
    }
    static boolean isResourceLocation(String value) { return value != null && value.length() <= 128 && value.matches("[a-z0-9_.-]+:[a-z0-9_./-]+"); }
}
