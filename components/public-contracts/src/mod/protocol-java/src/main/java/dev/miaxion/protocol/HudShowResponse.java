package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;

public record HudShowResponse(
        @JsonProperty(required = true) boolean shown) {
}
