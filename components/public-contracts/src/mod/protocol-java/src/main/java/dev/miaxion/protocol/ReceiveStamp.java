package dev.miaxion.protocol;

import java.time.Instant;
import java.util.Objects;

public record ReceiveStamp(Instant utc, long monotonicNanos) {
    public ReceiveStamp {
        Objects.requireNonNull(utc, "utc");
    }
}
