package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

public record RequestEnvelope(
        @JsonProperty(required = true) String protocolVersion,
        @JsonProperty(required = true) UUID requestId,
        @JsonProperty(required = true) OffsetDateTime timestamp,
        @JsonProperty(required = true) OffsetDateTime deadline,
        @JsonProperty(required = true) ProtocolAction action,
        @JsonProperty(required = true) String sessionToken,
        @JsonProperty(required = true) JsonNode payload,
        @JsonProperty(required = true) JsonNode error) {

    public RequestEnvelope {
        if (error != null && error.isNull()) {
            error = null;
        }
        Objects.requireNonNull(protocolVersion, "protocolVersion");
        Objects.requireNonNull(requestId, "requestId");
        Objects.requireNonNull(timestamp, "timestamp");
        Objects.requireNonNull(deadline, "deadline");
        if (!deadline.isAfter(timestamp)) {
            throw new IllegalArgumentException(
                "Request deadline must be later than its timestamp.");
        }
        Objects.requireNonNull(action, "action");
        Objects.requireNonNull(payload, "payload");
        if (payload.isNull()) {
            throw new NullPointerException("payload");
        }
        if (error != null) {
            throw new IllegalArgumentException(
                "Request error must be null.");
        }
    }
}
