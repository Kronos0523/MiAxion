package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WorldRegistryReference(
        @JsonProperty(required = true) WorldRegistryKind kind,
        @JsonProperty(required = true) String resourceId) {
    public WorldRegistryReference {
        if (kind == null) {
            throw new NullPointerException("kind");
        }
        QuestContractValidation.requireResourceLocation(resourceId, "resourceId");
    }

    String key() {
        return kind + "\u0000" + resourceId;
    }
}
