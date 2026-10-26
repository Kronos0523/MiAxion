package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

public record SessionPairRequest(
        @JsonProperty(required = true) String clientName,
        @JsonProperty(required = true) String clientVersion) {

    public SessionPairRequest {
        Objects.requireNonNull(clientName, "clientName");
        Objects.requireNonNull(clientVersion, "clientVersion");
    }
}
