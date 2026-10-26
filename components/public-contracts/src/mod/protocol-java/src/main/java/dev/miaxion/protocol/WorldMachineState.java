package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum WorldMachineState {
    @JsonProperty("running") RUNNING,
    @JsonProperty("idle") IDLE,
    @JsonProperty("blocked") BLOCKED,
    @JsonProperty("offline") OFFLINE,
    @JsonProperty("unknown") UNKNOWN
}
