package dev.miaxion.bridge.recipe.api;

/** Bounded, non-sensitive diagnostic classification for an optional addon. */
public enum RecipeProviderFailureReason {
    READY, VERSION_UNAVAILABLE, OPERATION_UNSUPPORTED, POLICY_REJECTED,
    DEPENDENCY_MISSING, ADAPTER_UNSUPPORTED, API_INCOMPATIBLE, RUNTIME_PENDING,
    INITIALIZATION_FAILED, DISABLED
}
