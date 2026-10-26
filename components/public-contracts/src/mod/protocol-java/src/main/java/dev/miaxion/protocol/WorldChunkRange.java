package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum WorldChunkRange {
    @JsonProperty("1x1") ONE_BY_ONE,
    @JsonProperty("3x3") THREE_BY_THREE,
    @JsonProperty("5x5") FIVE_BY_FIVE
}
