package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

public record ResponseEnvelope(
        @JsonProperty(required = true) String protocolVersion,
        @JsonProperty(required = true) UUID requestId,
        @JsonProperty(required = true) OffsetDateTime timestamp,
        @JsonProperty(required = true) OffsetDateTime deadline,
        @JsonProperty(required = true) ProtocolAction action,
        @JsonProperty(required = true) String sessionToken,
        @JsonProperty(required = true) JsonNode payload,
        @JsonProperty(required = true) ProtocolError error) {

    public ResponseEnvelope {
        if (payload != null && payload.isNull()) {
            payload = null;
        }
        Objects.requireNonNull(protocolVersion, "protocolVersion");
        Objects.requireNonNull(requestId, "requestId");
        Objects.requireNonNull(timestamp, "timestamp");
        Objects.requireNonNull(deadline, "deadline");
        Objects.requireNonNull(action, "action");
        if (sessionToken != null) {
            throw new IllegalArgumentException(
                "Response sessionToken must be null.");
        }
        if ((payload == null) == (error == null)) {
            throw new IllegalArgumentException(
                "Response must contain exactly one of payload or error.");
        }
    }
}
