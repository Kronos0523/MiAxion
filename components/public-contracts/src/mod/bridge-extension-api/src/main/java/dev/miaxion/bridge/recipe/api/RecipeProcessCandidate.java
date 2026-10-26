package dev.miaxion.bridge.recipe.api;

import java.util.Objects;
import java.util.regex.Pattern;

/** Opaque process facts retained by a recipe addon without exposing base implementation types. */
public record RecipeProcessCandidate(String machineFamily, Integer durationTicks,
    String energyUnit, Long energyAmount) {
    private static final Pattern RESOURCE_LOCATION = Pattern.compile(
        "^[a-z0-9_.-]+:[a-z0-9_./-]+$");
    private static final long MAX_SAFE_INTEGER = 9_007_199_254_740_991L;

    public RecipeProcessCandidate {
        Objects.requireNonNull(machineFamily, "machineFamily");
        if (machineFamily.length() > 128 || !RESOURCE_LOCATION.matcher(machineFamily).matches()) {
            throw new IllegalArgumentException("machineFamily must be a resource location.");
        }
        if (durationTicks != null && durationTicks < 1) {
            throw new IllegalArgumentException("durationTicks must be positive.");
        }
        if ((energyUnit == null) != (energyAmount == null)) {
            throw new IllegalArgumentException("Energy unit and amount must appear together.");
        }
        if (energyUnit != null && (energyUnit.isBlank() || energyUnit.length() > 64 || energyUnit.chars().anyMatch(Character::isISOControl))) {
            throw new IllegalArgumentException("Unsupported energy unit.");
        }
        if (energyAmount != null && (energyAmount < 1 || energyAmount > MAX_SAFE_INTEGER)) {
            throw new IllegalArgumentException("energyAmount is outside the safe integer range.");
        }
    }
}
