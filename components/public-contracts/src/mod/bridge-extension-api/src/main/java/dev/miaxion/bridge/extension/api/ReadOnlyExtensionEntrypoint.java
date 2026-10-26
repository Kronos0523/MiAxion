package dev.miaxion.bridge.extension.api;

/** Fabric entrypoint; the host binds the actual provider Mod ID, not this descriptor's claimed owner. */
@FunctionalInterface
public interface ReadOnlyExtensionEntrypoint {
    String ENTRYPOINT = "miaxion:read_only_extension";
    BridgeExtension extension();
}
