package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RecipeAlternativeDto(
        String itemId,
        @JsonInclude(JsonInclude.Include.NON_DEFAULT) int count,
        JsonNode components, RecipeResourceDto resource) {
    private static final int RESOURCE_LOCATION_MAX_LENGTH = 128;
    private static final Pattern RESOURCE_LOCATION_PATTERN = Pattern.compile(
        "^[a-z0-9_.-]+:[a-z0-9_./-]+$");

    public RecipeAlternativeDto(String itemId, int count, JsonNode components) { this(itemId, count, components, null); }

    public RecipeAlternativeDto(String itemId, int count) {
        this(itemId, count, null, null);
    }

    public RecipeAlternativeDto {
        if (resource == null) {
        Objects.requireNonNull(itemId, "itemId");
        if (itemId.length() > RESOURCE_LOCATION_MAX_LENGTH
            || !RESOURCE_LOCATION_PATTERN.matcher(itemId).matches()) {
            throw new IllegalArgumentException(
                "itemId must be a valid resource location.");
        }
        if (count <= 0) {
            throw new IllegalArgumentException("count must be positive.");
        }
        } else if (itemId != null || count != 0) throw new IllegalArgumentException("Typed resource is not an item.");
        components = copyComponents(components);
    }

    private static JsonNode copyComponents(JsonNode value) {
        if (value == null) {
            return null;
        }
        if (!value.isObject()) {
            throw new IllegalArgumentException("components must be an object.");
        }
        Iterator<Map.Entry<String, JsonNode>> fields = value.fields();
        while (fields.hasNext()) {
            String key = fields.next().getKey();
            if (key.length() > RESOURCE_LOCATION_MAX_LENGTH
                || !RESOURCE_LOCATION_PATTERN.matcher(key).matches()) {
                throw new IllegalArgumentException(
                    "component keys must be resource locations.");
            }
        }
        return value.deepCopy();
    }
}
