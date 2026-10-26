package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

public record WorldLocatedResponse(
        @JsonProperty(required = true) WorldQuerySummary summary,
        @JsonProperty(required = true) String targetId,
        @JsonProperty(required = true) String displayName,
        @JsonProperty(required = true) int x,
        @JsonProperty(required = true) int z,
        @JsonProperty(required = true) Integer y,
        @JsonProperty(required = true) int distanceBlocks,
        @JsonProperty(required = true) WorldLocatorRange radius,
        @JsonProperty(required = true) int queryCount) {
    public WorldLocatedResponse {
        Objects.requireNonNull(summary, "summary");
        if (summary.authority() != WorldQueryAuthority.GENERATOR_LOCATED) {
            throw new IllegalArgumentException("Locator responses require generator authority.");
        }
        QuestContractValidation.requireResourceLocation(targetId, "targetId");
        WorldQueryValidation.requireDisplayName(displayName, "displayName");
        new WorldBlockPosition(x, y == null ? 0 : y, z);
        if (distanceBlocks < 0 || queryCount < 1 || queryCount > 1024) {
            throw new IllegalArgumentException("Locator response is outside its bounds.");
        }
        Objects.requireNonNull(radius, "radius");
    }
}
