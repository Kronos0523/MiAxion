package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

public record CapabilityDescriptor(
        @JsonProperty(required = true) String name,
        @JsonProperty(required = true) String version,
        @JsonProperty(required = true) boolean available) {

    public CapabilityDescriptor {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(version, "version");
    }
}
