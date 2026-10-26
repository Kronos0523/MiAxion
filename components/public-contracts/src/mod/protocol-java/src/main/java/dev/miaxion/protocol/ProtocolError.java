package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Objects;

public record ProtocolError(
        @JsonProperty(required = true) ProtocolErrorCode code,
        @JsonProperty(required = true) String message,
        @JsonProperty(required = true) boolean retryable,
        @JsonProperty(required = true) JsonNode details) {

    public ProtocolError {
        if (details != null && details.isNull()) {
            details = null;
        }
        Objects.requireNonNull(code, "code");
        Objects.requireNonNull(message, "message");
    }
}
