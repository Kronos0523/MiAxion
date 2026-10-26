package dev.miaxion.bridge.extension.api;

import java.util.List;
import java.util.Objects;

/** Compile-only SPI. Only the Bridge Mod supplies these classes at runtime. */
public record BridgeExtension(
    String id,
    String version,
    int apiMajor,
    List<String> requiredMods,
    List<ExtensionCapability> capabilities
) {
    public static final int API_MAJOR = 1;
    public static final String IMC_METHOD = "register_read_only_extension_v1";

    public BridgeExtension {
        Objects.requireNonNull(id);
        Objects.requireNonNull(version);
        requiredMods = List.copyOf(requiredMods);
        capabilities = List.copyOf(capabilities);
    }
}
