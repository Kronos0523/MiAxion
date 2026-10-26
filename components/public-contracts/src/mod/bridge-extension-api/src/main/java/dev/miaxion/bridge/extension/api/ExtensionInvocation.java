package dev.miaxion.bridge.extension.api;

import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.function.BooleanSupplier;

/** A short-lived call context, not a pairing credential or an unbounded game handle. */
public final class ExtensionInvocation {
    private final Instant deadline;
    private final BooleanSupplier current;

    public ExtensionInvocation(Instant deadline, BooleanSupplier current) {
        this.deadline = Objects.requireNonNull(deadline);
        this.current = Objects.requireNonNull(current);
    }

    public Instant deadline() { return deadline; }

    public void ensureCurrent() {
        if (!current.getAsBoolean() || !Instant.now().isBefore(deadline)) {
            throw new CancellationException("Extension invocation expired.");
        }
    }
}
