package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/** Bounded scheduler state only; no partial world result crosses this DTO. */
public record WorldQueryProgressResponse(
        @JsonProperty(required = true) boolean active,
        @JsonProperty(required = true) String phase,
        @JsonProperty(required = true) int completedUnits,
        @JsonProperty(required = true) int totalUnits,
        @JsonProperty(required = true) int examined,
        @JsonProperty(required = true) int loaded) {
    private static final int MAX_COUNTER = 1_048_576;

    public WorldQueryProgressResponse {
        Objects.requireNonNull(phase, "phase");
        if (phase.length() < 1 || phase.length() > 48
                || !phase.matches("[a-z][a-z0-9_.-]*")) {
            throw new IllegalArgumentException("phase is outside its bounds.");
        }
        if (completedUnits < 0 || totalUnits < 0 || completedUnits > totalUnits
                || completedUnits > MAX_COUNTER || totalUnits > MAX_COUNTER
                || examined < 0 || examined > MAX_COUNTER
                || loaded < 0 || loaded > totalUnits || loaded > MAX_COUNTER) {
            throw new IllegalArgumentException("Progress counters are outside their bounds.");
        }
        if (!active && (completedUnits != 0 || totalUnits != 0 || examined != 0 || loaded != 0)) {
            throw new IllegalArgumentException("Inactive progress must not expose stale counters.");
        }
    }

    public static WorldQueryProgressResponse inactive() {
        return new WorldQueryProgressResponse(false, "unknown", 0, 0, 0, 0);
    }
}
