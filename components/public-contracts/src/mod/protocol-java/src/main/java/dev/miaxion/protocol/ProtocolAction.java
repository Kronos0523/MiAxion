package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Optional;
import java.util.regex.Pattern;

public record ProtocolAction(@JsonValue String value) {
    private static final Pattern WIRE_PATTERN = Pattern.compile(
        "^[a-z][a-z0-9]*(?:\\.[a-z][a-z0-9]*)+$");

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public ProtocolAction {
        if (value == null
            || value.length() < 3
            || value.length() > 64
            || !WIRE_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException(
                "Protocol action must be a lowercase ASCII dotted name.");
        }
    }

    public Optional<KnownProtocolAction> known() {
        return KnownProtocolAction.fromWire(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
