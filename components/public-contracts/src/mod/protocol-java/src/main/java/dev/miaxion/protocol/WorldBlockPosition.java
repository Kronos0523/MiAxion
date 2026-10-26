package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WorldBlockPosition(
        @JsonProperty(required = true) int x,
        @JsonProperty(required = true) int y,
        @JsonProperty(required = true) int z) {
    public WorldBlockPosition {
        if (x < -30_000_000 || x > 30_000_000
            || y < -2048 || y > 2048
            || z < -30_000_000 || z > 30_000_000) {
            throw new IllegalArgumentException("World position is outside its bounds.");
        }
    }

    String key() {
        return x + ":" + y + ":" + z;
    }
}
