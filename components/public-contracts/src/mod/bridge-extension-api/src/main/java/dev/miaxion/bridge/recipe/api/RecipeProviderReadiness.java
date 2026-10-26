package dev.miaxion.bridge.recipe.api;

import java.util.Objects;

public record RecipeProviderReadiness(RecipeProviderStatus status, RecipeProviderFailureReason reason) {
    public RecipeProviderReadiness {
        Objects.requireNonNull(status); Objects.requireNonNull(reason);
        if ((status == RecipeProviderStatus.READY) != (reason == RecipeProviderFailureReason.READY))
            throw new IllegalArgumentException("Ready status and reason must agree.");
    }
    public static RecipeProviderReadiness ready() { return new RecipeProviderReadiness(RecipeProviderStatus.READY, RecipeProviderFailureReason.READY); }
    public static RecipeProviderReadiness gameNotReady(RecipeProviderFailureReason reason) {
        if (reason != RecipeProviderFailureReason.RUNTIME_PENDING) throw new IllegalArgumentException("Invalid pending reason.");
        return new RecipeProviderReadiness(RecipeProviderStatus.GAME_NOT_READY, reason);
    }
    public static RecipeProviderReadiness unavailable(RecipeProviderFailureReason reason) {
        if (reason == RecipeProviderFailureReason.READY || reason == RecipeProviderFailureReason.RUNTIME_PENDING) throw new IllegalArgumentException("Invalid unavailable reason.");
        return new RecipeProviderReadiness(RecipeProviderStatus.CAPABILITY_UNAVAILABLE, reason);
    }
}
