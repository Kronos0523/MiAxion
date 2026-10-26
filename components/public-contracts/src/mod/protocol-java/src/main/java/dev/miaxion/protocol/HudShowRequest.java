package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

public record HudShowRequest(
        @JsonProperty(required = true) String text,
        @JsonProperty(required = true) HudSeverity severity,
        @JsonProperty(required = true) int durationMs) {

    public HudShowRequest {
        Objects.requireNonNull(text, "text");
        Objects.requireNonNull(severity, "severity");
    }
}
