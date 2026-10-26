package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;

/** Typed category and planning fact carried by recipe capability 1.7. */
public record RecipeClassificationDto(
    String category,
    @JsonInclude(JsonInclude.Include.NON_NULL) String visibleCategoryName,
    String planningEligibility
) {
    public RecipeClassificationDto {
        if (!("crafting".equals(category) || "machine_processing".equals(category)
            || "drops".equals(category) || "loot".equals(category)
            || "conversion".equals(category) || "other_unclassified".equals(category)))
            throw new IllegalArgumentException("Unsupported recipe category.");
        if (!("deterministic".equals(planningEligibility)
            || "acquisition_only".equals(planningEligibility)
            || "insufficient_data".equals(planningEligibility)))
            throw new IllegalArgumentException("Unsupported recipe planning eligibility.");
        if (("drops".equals(category) || "loot".equals(category)
            || "other_unclassified".equals(category)) && "deterministic".equals(planningEligibility))
            throw new IllegalArgumentException("Acquisition and unclassified categories cannot be deterministic.");
        if (visibleCategoryName != null && (visibleCategoryName.isBlank()
            || visibleCategoryName.length() > 256
            || visibleCategoryName.chars().anyMatch(value -> Character.isISOControl(value))))
            throw new IllegalArgumentException("Invalid visible category name.");
    }
}
