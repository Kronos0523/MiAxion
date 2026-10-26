package dev.miaxion.bridge.recipe.api;

import java.util.concurrent.CancellationException;
import java.util.function.BooleanSupplier;

/** Stable addon contract; no Bridge game/session implementation type appears here. */
public interface RecipeProvider {
    RecipeProviderStatus status();
    /** Optional native ingredient directory. Legacy addons retain normal recipe queries. */
    default boolean supportsIngredientDirectory() { return false; }
    default IngredientDirectoryPage ingredients(IngredientDirectoryQuery query, BooleanSupplier cancelled) {
        throw new UnsupportedOperationException("Native ingredient directory unavailable");
    }
    /** Opt in only when query/uses accept typed identities and retain mixed slots/unknown amounts.
     * Legacy binaries inherit false; the host preserves item reads with an explicit coverage warning. */
    default boolean supportsTypedResources() { return false; }
    /**
     * Monotonic data generation for source-bound dispatch. Return a nonnegative
     * value only when the addon can guarantee it advances on every runtime
     * replacement or recipe-data reload. The default opts out of that path;
     * legacy single-source dispatch remains available.
     */
    default long sourceGeneration() { return -1; }
    RecipeProviderResult query(RecipeQuery query);
    RecipeProviderResult uses(RecipeQuery query);
    default RecipeProviderReadiness readiness(RecipeProviderOperation operation) { return status() == RecipeProviderStatus.READY ? RecipeProviderReadiness.ready() : status() == RecipeProviderStatus.GAME_NOT_READY ? RecipeProviderReadiness.gameNotReady(RecipeProviderFailureReason.RUNTIME_PENDING) : RecipeProviderReadiness.unavailable(RecipeProviderFailureReason.INITIALIZATION_FAILED); }
    default RecipeProviderResult query(RecipeQuery query, BooleanSupplier cancelled) { if (cancelled.getAsBoolean()) throw new CancellationException(); return query(query); }
    default RecipeProviderResult uses(RecipeQuery query, BooleanSupplier cancelled) { if (cancelled.getAsBoolean()) throw new CancellationException(); return uses(query); }
    default void disable(RecipeProviderOperation operation, RecipeProviderFailureReason reason) { }
}
