package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WorldRegistryMatch(
        @JsonProperty(required = true) WorldRegistryKind kind,
        @JsonProperty(required = true) String resourceId,
        @JsonProperty(required = true) String translationKey,
        @JsonProperty(required = true) String displayName) {
    public WorldRegistryMatch {
        if (kind == null) {
            throw new NullPointerException("kind");
        }
        QuestContractValidation.requireResourceLocation(resourceId, "resourceId");
        WorldQueryValidation.requireDisplayName(translationKey, "translationKey");
        WorldQueryValidation.requireDisplayName(displayName, "displayName");
    }

    String key() {
        return kind + "\u0000" + resourceId;
    }
}
