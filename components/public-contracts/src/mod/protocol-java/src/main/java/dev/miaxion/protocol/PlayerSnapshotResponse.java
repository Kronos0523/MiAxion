package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Objects;

public record PlayerSnapshotResponse(
        @JsonProperty(required = true) String dimensionId,
        @JsonProperty(required = true) Position position,
        @JsonProperty(required = true) double health,
        @JsonProperty(required = true) double food,
        @JsonProperty(required = true) int experienceLevel,
        @JsonProperty(required = true) GameMode gameMode,
        @JsonInclude(JsonInclude.Include.NON_NULL) Double maxHealth) {

    public PlayerSnapshotResponse(
        String dimensionId,
        Position position,
        double health,
        double food,
        int experienceLevel,
        GameMode gameMode) {
        this(dimensionId, position, health, food, experienceLevel, gameMode, null);
    }

    public PlayerSnapshotResponse {
        Objects.requireNonNull(dimensionId, "dimensionId");
        Objects.requireNonNull(position, "position");
        Objects.requireNonNull(gameMode, "gameMode");
        if (maxHealth != null && (!Double.isFinite(maxHealth) || maxHealth <= 0)) {
            throw new IllegalArgumentException("maxHealth must be a positive finite value");
        }
    }
}
