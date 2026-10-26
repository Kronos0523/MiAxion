package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

public record SessionRevokeRequest(
        @JsonProperty(required = true) SessionRevokeReason reason) {

    public SessionRevokeRequest {
        Objects.requireNonNull(reason, "reason");
    }
}
