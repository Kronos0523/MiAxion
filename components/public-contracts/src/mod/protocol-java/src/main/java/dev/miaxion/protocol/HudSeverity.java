package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum HudSeverity {
    @JsonProperty("info")
    INFO,

    @JsonProperty("warning")
    WARNING,

    @JsonProperty("error")
    ERROR
}
