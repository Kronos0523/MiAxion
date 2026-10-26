package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum CancelState {
    @JsonProperty("cancelled")
    CANCELLED,

    @JsonProperty("already_completed")
    ALREADY_COMPLETED,

    @JsonProperty("not_cancellable")
    NOT_CANCELLABLE
}
