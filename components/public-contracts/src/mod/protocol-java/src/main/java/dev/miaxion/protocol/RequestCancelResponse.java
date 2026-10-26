package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import java.util.UUID;

public record RequestCancelResponse(
        @JsonProperty(required = true) UUID targetRequestId,
        @JsonProperty(required = true) boolean cancelled,
        @JsonProperty(required = true) CancelState state) {

    public RequestCancelResponse {
        Objects.requireNonNull(targetRequestId, "targetRequestId");
        Objects.requireNonNull(state, "state");
    }
}
