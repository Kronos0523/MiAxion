package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import java.util.regex.Pattern;

/** Optional, observed process facts. Missing values stay unknown. */
public record RecipeProcessDto(
        @JsonProperty(required = true) String machineFamily,
        @JsonInclude(JsonInclude.Include.NON_NULL) Integer durationTicks,
        @JsonInclude(JsonInclude.Include.NON_NULL) String energyUnit,
        @JsonInclude(JsonInclude.Include.NON_NULL) Long energyAmount) {
    private static final Pattern RESOURCE_LOCATION = Pattern.compile(
        "^[a-z0-9_.-]+:[a-z0-9_./-]+$");
    private static final long MAX_SAFE_INTEGER = 9_007_199_254_740_991L;

    public RecipeProcessDto {
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
