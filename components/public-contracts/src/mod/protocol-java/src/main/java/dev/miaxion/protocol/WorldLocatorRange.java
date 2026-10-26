package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum WorldLocatorRange {
    @JsonProperty("near") NEAR,
    @JsonProperty("standard") STANDARD,
    @JsonProperty("far") FAR
}
