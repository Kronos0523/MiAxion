package dev.miaxion.bridge.recipe.api;

/** Whether a projected display is safe for deterministic material planning. */
public enum RecipePlanningEligibility {
    DETERMINISTIC,
    ACQUISITION_ONLY,
    INSUFFICIENT_DATA
}
