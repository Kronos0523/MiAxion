package dev.miaxion.bridge.recipe.api;

import java.util.List;
import java.util.Objects;

public record RecipeSlotCandidate(List<RecipeAlternativeCandidate> alternatives) {
    // Match the recipe protocol limit without depending on Bridge implementation classes.
    public static final int MAX_ALTERNATIVES = 16384;

    public RecipeSlotCandidate {
        Objects.requireNonNull(alternatives);
        if (alternatives.isEmpty() || alternatives.size() > MAX_ALTERNATIVES || alternatives.stream().anyMatch(Objects::isNull))
            throw new IllegalArgumentException("Invalid recipe slot alternatives.");
        alternatives = List.copyOf(alternatives);
    }
}
