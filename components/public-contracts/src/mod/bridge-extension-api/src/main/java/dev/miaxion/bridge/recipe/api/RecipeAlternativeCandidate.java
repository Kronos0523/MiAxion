package dev.miaxion.bridge.recipe.api;

import com.fasterxml.jackson.databind.JsonNode;
import dev.miaxion.bridge.recipe.api.RecipeResourceValue;

/** Item compatibility representation or an explicitly typed resource, never both. */
public record RecipeAlternativeCandidate(String itemId, int count, JsonNode components, RecipeResourceValue resource) {
    public RecipeAlternativeCandidate(String itemId, int count) { this(itemId, count, null, null); }
    public RecipeAlternativeCandidate(String itemId, int count, JsonNode components) { this(itemId, count, components, null); }
    public RecipeAlternativeCandidate(RecipeResourceValue resource) { this(null, 0, null, java.util.Objects.requireNonNull(resource)); }
    public RecipeAlternativeCandidate {
        if (resource == null) {
            if (!RecipeQuery.isResourceLocation(itemId) || count < 1) throw new IllegalArgumentException("Invalid item alternative.");
        } else if (itemId != null || count != 0) throw new IllegalArgumentException("Typed alternatives cannot masquerade as items.");
        if (components != null && !components.isObject()) throw new IllegalArgumentException("Components must be an object.");
        if (components != null) {
            var names = components.fieldNames();
            while (names.hasNext()) if (!RecipeQuery.isResourceLocation(names.next())) throw new IllegalArgumentException("Invalid component identity.");
        }
        components = components == null ? null : components.deepCopy();
    }
    public String resourceId() { return resource == null ? itemId : resource.resourceId(); }
    public String resourceKind() { return resource == null ? "item" : resource.kind(); }
    public String identityKey() { return resource == null ? "item\u001f" + itemId : resource.identityKey(); }
    @Override public JsonNode components() { return components == null ? null : components.deepCopy(); }
}
