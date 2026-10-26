package dev.miaxion.protocol;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;
import tools.jackson.databind.JsonNode;

public final class CommonRequest {
    private final String protocolVersion;
    private final UUID requestId;
    private final OffsetDateTime timestamp;
    private final OffsetDateTime deadline;
    private final String rawAction;
    private final String sessionToken;
    private final ReceiveStamp received;
    private final ValidatorHandle validatorHandle;

    CommonRequest(
        String protocolVersion,
        UUID requestId,
        OffsetDateTime timestamp,
        OffsetDateTime deadline,
        String rawAction,
        String sessionToken,
        ReceiveStamp received,
        ValidatorHandle validatorHandle
    ) {
        this.protocolVersion = Objects.requireNonNull(
            protocolVersion, "protocolVersion");
        this.requestId = Objects.requireNonNull(requestId, "requestId");
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp");
        this.deadline = Objects.requireNonNull(deadline, "deadline");
        this.rawAction = Objects.requireNonNull(rawAction, "rawAction");
        this.sessionToken = sessionToken;
        this.received = Objects.requireNonNull(received, "received");
        this.validatorHandle = Objects.requireNonNull(
            validatorHandle, "validatorHandle");
    }

    public String protocolVersion() {
        return protocolVersion;
    }

    public UUID requestId() {
        return requestId;
    }

    public OffsetDateTime timestamp() {
        return timestamp;
    }

    public OffsetDateTime deadline() {
        return deadline;
    }

    public String rawAction() {
        return rawAction;
    }

    public String sessionToken() {
        return sessionToken;
    }

    public ReceiveStamp received() {
        return received;
    }

    ValidatorHandle validatorHandle() {
        return validatorHandle;
    }

    static final class ValidatorHandle {
        private final ProtocolValidator owner;
        private final JsonNode tree;

        ValidatorHandle(
            ProtocolValidator owner,
            JsonNode tree
        ) {
            this.owner = owner;
            this.tree = tree;
        }

        ProtocolValidator owner() {
            return owner;
        }

        JsonNode tree() {
            return tree;
        }
    }
}
