package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

public record WorldRegistryResolveRequest(
        @JsonProperty(required = true) WorldRegistryKind kind,
        @JsonProperty(required = true) String query) {
    public WorldRegistryResolveRequest {
        Objects.requireNonNull(kind, "kind");
        QuestContractValidation.requireText(query, 1, 512, "query");
        EntityTextValidation.require(query, "query");
    }
}
