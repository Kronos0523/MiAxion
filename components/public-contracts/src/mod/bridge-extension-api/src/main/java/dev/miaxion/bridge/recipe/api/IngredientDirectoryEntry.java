package dev.miaxion.bridge.recipe.api;

import com.fasterxml.jackson.databind.JsonNode;
/** Native identity + optional variant identity, never a stock or recipe quantity. */
public record IngredientDirectoryEntry(String kind, String resourceId, String displayName,
    String ingredientTypeUid, JsonNode components) {
    public IngredientDirectoryEntry {
        if (!java.util.List.of("item", "fluid", "custom").contains(kind)
            || resourceId == null || !resourceId.matches("[a-z0-9_.-]+:[a-z0-9/._-]+")
            || displayName == null || displayName.isBlank() || displayName.codePointCount(0, displayName.length()) > 256
            || ingredientTypeUid == null || ingredientTypeUid.isBlank() || ingredientTypeUid.length() > 256
            || ingredientTypeUid.codePoints().anyMatch(c -> Character.isISOControl(c) || c == 0x061c || c == 0x200e || c == 0x200f || c >= 0x202a && c <= 0x202e || c >= 0x2066 && c <= 0x2069))
            throw new IllegalArgumentException("Invalid native ingredient identity");
        components = components == null ? null : components.deepCopy();
    }
}
