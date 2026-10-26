package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Objects;
import java.util.regex.Pattern;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RecipeQueryRequest(
        @JsonProperty(required = true) String itemId,
        JsonNode components,
        String matchMode,
        RecipeSelectionRequest selection,
        Integer sourcePageSize,
        String sourceCursor,
        String resourceKind,
        String ingredientTypeUid) {
    private static final Pattern RESOURCE_LOCATION = Pattern.compile(
        "^[a-z0-9_.-]+:[a-z0-9_./-]+$");

    public RecipeQueryRequest(String itemId, JsonNode components, String matchMode, RecipeSelectionRequest selection, Integer sourcePageSize, String sourceCursor) {
        this(itemId, components, matchMode, selection, sourcePageSize, sourceCursor, "item", null);
    }
    public RecipeQueryRequest(String itemId) {
        this(itemId, null, "item", null, null, null);
    }

    public RecipeQueryRequest(
        String itemId,
        JsonNode components,
        String matchMode,
        RecipeSelectionRequest selection) {
        this(itemId, components, matchMode, selection,
            null, null);
    }

    public RecipeQueryRequest {
        resourceKind = resourceKind == null ? "item" : resourceKind;
        if (!java.util.Set.of("item", "fluid", "chemical", "energy", "custom").contains(resourceKind)
            || ingredientTypeUid != null && (ingredientTypeUid.isBlank() || ingredientTypeUid.length() > 512)) throw new IllegalArgumentException("Invalid resource type.");
        Objects.requireNonNull(itemId, "itemId");
        if (itemId.length() > 128 || !RESOURCE_LOCATION.matcher(itemId).matches()) {
            throw new IllegalArgumentException("itemId must be a valid resource location.");
        }
        matchMode = matchMode == null ? "item" : matchMode;
        if (!matchMode.equals("item")
            && !matchMode.equals("subset-components")
            && !matchMode.equals("exact-components")) {
            throw new IllegalArgumentException("Unknown recipe component match mode.");
        }
        if (components != null && !components.isObject()) {
            throw new IllegalArgumentException("components must be an object.");
        }
        components = components == null ? null : components.deepCopy();
        if (sourcePageSize != null && (sourcePageSize < 1
            || sourcePageSize > ProtocolConstants.RECIPE_SOURCE_PAGE_MAX)) {
            throw new IllegalArgumentException("sourcePageSize is outside its bounds.");
        }
        validateCursor(sourceCursor);
    }

    private static void validateCursor(String cursor) {
        if (cursor != null && (cursor.length() < 1
            || cursor.length() > ProtocolConstants.RECIPE_SOURCE_CURSOR_MAX
            || !cursor.chars().allMatch(value ->
                value >= 'a' && value <= 'z' || value >= '0' && value <= '9'))) {
            throw new IllegalArgumentException("sourceCursor is invalid.");
        }
    }
}
