package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Objects;

public record WorldNearbyScanRequest(
        @JsonProperty(required = true) WorldChunkRange chunkRange,
        @JsonProperty(required = true) WorldVerticalRange verticalRange,
        @JsonInclude(JsonInclude.Include.NON_NULL) WorldScanWindow window,
        @JsonInclude(JsonInclude.Include.NON_NULL) String pageCursor) {
    public WorldNearbyScanRequest {
        Objects.requireNonNull(chunkRange, "chunkRange");
        Objects.requireNonNull(verticalRange, "verticalRange");
        WorldQueryValidation.requirePageCursor(pageCursor, true);
    }

    public WorldNearbyScanRequest() {
        this(WorldChunkRange.THREE_BY_THREE, WorldVerticalRange.TEN);
    }

    public WorldNearbyScanRequest(WorldChunkRange chunkRange, WorldVerticalRange verticalRange) {
        this(chunkRange, verticalRange, null);
    }

    public WorldNearbyScanRequest(WorldChunkRange chunkRange, WorldVerticalRange verticalRange, WorldScanWindow window) {
        this(chunkRange, verticalRange, window, null);
    }
}
