package dev.miaxion.protocol;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class ProtocolValidationFailure extends RuntimeException {
    private final Correlation correlation;
    private final ProtocolErrorCode errorCode;

    public ProtocolValidationFailure(String message) {
        this(message, null, null, ProtocolErrorCode.INVALID_REQUEST);
    }

    public ProtocolValidationFailure(String message, Throwable cause) {
        this(message, cause, null, ProtocolErrorCode.INVALID_REQUEST);
    }

    ProtocolValidationFailure(
        String message,
        Throwable cause,
        Correlation correlation,
        ProtocolErrorCode errorCode
    ) {
        super(message, cause);
        this.correlation = correlation;
        this.errorCode = Objects.requireNonNull(errorCode, "errorCode");
    }

    public Optional<Correlation> correlation() {
        return Optional.ofNullable(correlation);
    }

    public ProtocolErrorCode errorCode() {
        return errorCode;
    }

    public record Correlation(
        UUID requestId,
        String rawAction,
        OffsetDateTime wireDeadline,
        boolean invalidEnvelopeSentinel
    ) {
        public Correlation {
            Objects.requireNonNull(requestId, "requestId");
            Objects.requireNonNull(rawAction, "rawAction");
            if (!invalidEnvelopeSentinel) {
                Objects.requireNonNull(wireDeadline, "wireDeadline");
            }
        }
    }
}
