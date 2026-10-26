package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum GameMode {
    @JsonProperty("survival")
    SURVIVAL,

    @JsonProperty("creative")
    CREATIVE,

    @JsonProperty("adventure")
    ADVENTURE,

    @JsonProperty("spectator")
    SPECTATOR
}
