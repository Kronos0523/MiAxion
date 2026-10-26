package dev.miaxion.bridge.extension.api;

import java.util.Objects;

public record ExtensionDependency(String capabilityId, String capabilityVersion) {
    public ExtensionDependency {
        Objects.requireNonNull(capabilityId);
        Objects.requireNonNull(capabilityVersion);
    }
}
