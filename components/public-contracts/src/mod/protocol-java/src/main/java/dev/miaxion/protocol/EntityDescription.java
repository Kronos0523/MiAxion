package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

public record EntityDescription(
        @JsonProperty(required = true) String kind,
        @JsonProperty(required = true) String resourceId,
        @JsonProperty(required = true) String translationKey,
        @JsonProperty(required = true) String displayName) {
    public EntityDescription {
        EntityReference reference = new EntityReference(kind, resourceId);
        Objects.requireNonNull(translationKey, "translationKey");
        Objects.requireNonNull(displayName, "displayName");
        EntityTextValidation.require(translationKey, "translationKey");
        EntityTextValidation.require(displayName, "displayName");
        if (scalarCount(translationKey) < 1 || scalarCount(translationKey) > 256
            || scalarCount(displayName) < 1 || scalarCount(displayName) > 256) {
            throw new IllegalArgumentException(
                "Entity names are outside their bounds.");
        }
    }

    public EntityReference reference() {
        return new EntityReference(kind, resourceId);
    }

    private static int scalarCount(String value) {
        return value.codePointCount(0, value.length());
    }
}
