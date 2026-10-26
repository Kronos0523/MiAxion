package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Exact centered window; does not authorize loading chunks or extending deadlines. */
public record WorldScanWindow(
        @JsonProperty(required = true) int chunkWidth,
        @JsonProperty(required = true) int verticalRadius) {
    public WorldScanWindow {
        if (chunkWidth < 1 || chunkWidth > 15 || chunkWidth % 2 == 0) {
            throw new IllegalArgumentException("chunkWidth is outside its bounds.");
        }
        if (verticalRadius < 1 || verticalRadius > 128) {
            throw new IllegalArgumentException("verticalRadius is outside its bounds.");
        }
    }
}
