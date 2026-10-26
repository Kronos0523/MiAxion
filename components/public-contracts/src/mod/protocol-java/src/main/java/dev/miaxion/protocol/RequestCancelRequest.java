package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import java.util.UUID;

public record RequestCancelRequest(
        @JsonProperty(required = true) UUID targetRequestId) {

    public RequestCancelRequest {
        Objects.requireNonNull(targetRequestId, "targetRequestId");
    }
}
