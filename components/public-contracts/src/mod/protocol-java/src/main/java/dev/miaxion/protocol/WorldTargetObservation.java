package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

public record WorldTargetObservation(
        @JsonProperty(required = true) String targetId,
        @JsonProperty(required = true) String displayName,
        @JsonProperty(required = true) WorldBlockPosition position,
        @JsonProperty(required = true) int distanceBlocks,
        @JsonProperty(required = true) WorldQueryTargetKind kind,
        @JsonProperty(required = true) int priority) {
    public WorldTargetObservation {
        QuestContractValidation.requireResourceLocation(targetId, "targetId");
        WorldQueryValidation.requireDisplayName(displayName, "displayName");
        Objects.requireNonNull(position, "position");
        if (distanceBlocks < 0 || priority < 0 || priority > 1000) {
            throw new IllegalArgumentException("Target observation is outside its bounds.");
        }
        Objects.requireNonNull(kind, "kind");
    }

    String key() {
        return targetId + "\u0000" + position.key();
    }
}
