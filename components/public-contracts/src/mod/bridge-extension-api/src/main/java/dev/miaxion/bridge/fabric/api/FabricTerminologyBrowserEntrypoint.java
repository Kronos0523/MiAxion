package dev.miaxion.bridge.fabric.api;

import dev.miaxion.bridge.neoforge.api.TerminologyBrowserProvider;

/** Optional Fabric browser source. The base owns capture settings and all transport/lifecycle. */
@FunctionalInterface
public interface FabricTerminologyBrowserEntrypoint {
    TerminologyBrowserProvider create();
}
