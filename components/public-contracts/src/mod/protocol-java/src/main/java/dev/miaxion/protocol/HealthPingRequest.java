package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

public record HealthPingRequest(
        @JsonProperty(required = true) String nonce) {

    public HealthPingRequest {
        Objects.requireNonNull(nonce, "nonce");
    }
}
