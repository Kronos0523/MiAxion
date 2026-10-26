package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Position(
        @JsonProperty(required = true) double x,
        @JsonProperty(required = true) double y,
        @JsonProperty(required = true) double z) {
}
