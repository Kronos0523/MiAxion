package dev.miaxion.protocol;

import java.util.Objects;

public final class KnownRequest {
    private final RequestEnvelope envelope;
    private final KnownProtocolAction action;
    private final Object payload;
    private final byte[] canonicalBytes;

    KnownRequest(
        RequestEnvelope envelope,
        KnownProtocolAction action,
        Object payload,
        byte[] canonicalBytes
    ) {
        this.envelope = Objects.requireNonNull(envelope, "envelope");
        this.action = Objects.requireNonNull(action, "action");
        this.payload = Objects.requireNonNull(payload, "payload");
        this.canonicalBytes = canonicalBytes.clone();
    }

    public RequestEnvelope envelope() {
        return envelope;
    }

    public KnownProtocolAction action() {
        return action;
    }

    public Object payload() {
        return payload;
    }

    public byte[] canonicalBytes() {
        return canonicalBytes.clone();
    }
}
