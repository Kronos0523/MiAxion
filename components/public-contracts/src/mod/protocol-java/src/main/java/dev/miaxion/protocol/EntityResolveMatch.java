package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import java.util.regex.Pattern;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
public record EntityResolveMatch(
        @JsonProperty(required = true) String kind,
        @JsonProperty(required = true) String resourceId,
        @JsonProperty(required = true) String translationKey,
        @JsonProperty(required = true) String displayName,
        String ingredientTypeUid, com.fasterxml.jackson.databind.JsonNode components) {
    public EntityResolveMatch(String kind, String resourceId, String translationKey, String displayName) {
        this(kind, resourceId, translationKey, displayName, null, null);
    }
    private static final Pattern RESOURCE_LOCATION = Pattern.compile(
        "^[a-z0-9_.-]+:[a-z0-9_./-]+$");

    public EntityResolveMatch {
        if (ingredientTypeUid != null && (ingredientTypeUid.isBlank() || ingredientTypeUid.length() > 256)) throw new IllegalArgumentException("Invalid ingredient type");
        if ("custom".equals(kind) && ingredientTypeUid == null) throw new IllegalArgumentException("Custom ingredient needs type");
        components = components == null ? null : components.deepCopy();
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(resourceId, "resourceId");
        Objects.requireNonNull(translationKey, "translationKey");
        Objects.requireNonNull(displayName, "displayName");
        if (!kind.equals("item") && !kind.equals("block") && !kind.equals("fluid") && !kind.equals("custom")) {
            throw new IllegalArgumentException("kind must be item, block or fluid.");
        }
        if (resourceId.length() > 128 || !RESOURCE_LOCATION.matcher(resourceId).matches()) {
            throw new IllegalArgumentException("resourceId must be a resource location.");
        }
        EntityTextValidation.require(translationKey, "translationKey");
        EntityTextValidation.require(displayName, "displayName");
        if (scalarCount(translationKey) < 1 || scalarCount(translationKey) > 256 ||
            scalarCount(displayName) < 1 || scalarCount(displayName) > 256) {
            throw new IllegalArgumentException("Entity names are outside their bounds.");
        }
    }

    private static int scalarCount(String value) {
        return value.codePointCount(0, value.length());
    }
}
