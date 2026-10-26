package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

public record WorldLocateRequest(
        @JsonProperty(required = true) String targetId,
        @JsonProperty(required = true) WorldLocatorRange range) {
    public WorldLocateRequest {
        QuestContractValidation.requireResourceLocation(targetId, "targetId");
        Objects.requireNonNull(range, "range");
    }
}
