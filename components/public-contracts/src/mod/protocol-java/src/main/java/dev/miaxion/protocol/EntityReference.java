package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import java.util.regex.Pattern;

public record EntityReference(
        @JsonProperty(required = true) String kind,
        @JsonProperty(required = true) String resourceId) {
    private static final Pattern RESOURCE_LOCATION = Pattern.compile(
        "^[a-z0-9_.-]+:[a-z0-9_./-]+$");

    public EntityReference {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(resourceId, "resourceId");
        if (!kind.equals("item") && !kind.equals("block")) {
            throw new IllegalArgumentException("kind must be item or block.");
        }
        if (resourceId.length() > 128
            || !RESOURCE_LOCATION.matcher(resourceId).matches()) {
            throw new IllegalArgumentException(
                "resourceId must be a resource location.");
        }
    }

    String key() {
        return kind + "\u0000" + resourceId;
    }
}
