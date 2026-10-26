package dev.miaxion.bridge.extension.api;

import java.util.List;
import java.util.Objects;

/** Schemas describe data only. Registration does not grant execution permission. */
public record ExtensionCapability(
    String id,
    String version,
    String title,
    String description,
    String inputSchema,
    String outputSchema,
    List<ExtensionDependency> dependencies,
    ExtensionExecutionContext executionContext,
    int maximumDurationMs,
    int maximumResultBytes,
    ExtensionHandler handler
) {
    public ExtensionCapability {
        Objects.requireNonNull(id);
        Objects.requireNonNull(version);
        Objects.requireNonNull(title);
        Objects.requireNonNull(description);
        Objects.requireNonNull(inputSchema);
        Objects.requireNonNull(outputSchema);
        dependencies = List.copyOf(dependencies);
        Objects.requireNonNull(executionContext);
        Objects.requireNonNull(handler);
    }
}
