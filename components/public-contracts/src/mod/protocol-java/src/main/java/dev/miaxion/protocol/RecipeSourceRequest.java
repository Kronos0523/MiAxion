package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

/** Source-bound recipe request; the existing 1.6 request frames remain unchanged. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record RecipeSourceRequest(
    @JsonProperty(required = true) String addonId,
    @JsonProperty(required = true) long sourceGeneration,
    @JsonProperty(required = true) String itemId,
    JsonNode components,
    String matchMode,
    RecipeSelectionRequest selection,
    Integer sourcePageSize,
    String sourceCursor,
    String resourceKind,
    String ingredientTypeUid
) {
    public RecipeSourceRequest(String addonId, long sourceGeneration, String itemId, JsonNode components,
        String matchMode, RecipeSelectionRequest selection, Integer sourcePageSize, String sourceCursor) {
        this(addonId, sourceGeneration, itemId, components, matchMode, selection, sourcePageSize, sourceCursor, "item", null);
    }
    public RecipeSourceRequest {
        if (addonId == null || !addonId.matches("[a-z][a-z0-9_]{1,63}"))
            throw new IllegalArgumentException("addonId is invalid.");
        if (sourceGeneration < 0) throw new IllegalArgumentException("sourceGeneration is invalid.");
        var query = new RecipeQueryRequest(itemId, components, matchMode, selection, sourcePageSize, sourceCursor, resourceKind, ingredientTypeUid);
        resourceKind = query.resourceKind();
        components = query.components();
        matchMode = query.matchMode();
    }
}
