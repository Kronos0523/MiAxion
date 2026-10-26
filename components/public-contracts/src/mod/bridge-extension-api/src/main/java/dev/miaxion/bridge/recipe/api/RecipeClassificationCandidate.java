package dev.miaxion.bridge.recipe.api;

import java.util.Objects;

/** Typed category fact supplied by an addon; typeId continues to carry the browser category ID. */
public record RecipeClassificationCandidate(
    RecipeCategory category,
    String visibleCategoryName,
    RecipePlanningEligibility planningEligibility
) {
    public RecipeClassificationCandidate {
        Objects.requireNonNull(category, "category");
        Objects.requireNonNull(planningEligibility, "planningEligibility");
        if ((category == RecipeCategory.DROPS || category == RecipeCategory.LOOT
            || category == RecipeCategory.OTHER_UNCLASSIFIED)
            && planningEligibility == RecipePlanningEligibility.DETERMINISTIC) {
            throw new IllegalArgumentException("Acquisition and unclassified categories cannot be deterministic.");
        }
        if (visibleCategoryName != null && (visibleCategoryName.isBlank()
            || visibleCategoryName.length() > 256
            || visibleCategoryName.chars().anyMatch(value -> Character.isISOControl(value)))) {
            throw new IllegalArgumentException("Invalid visible category name.");
        }
    }

    public static RecipeClassificationCandidate unclassified() {
        return new RecipeClassificationCandidate(
            RecipeCategory.OTHER_UNCLASSIFIED, null, RecipePlanningEligibility.INSUFFICIENT_DATA);
    }

    /**
     * Compatibility mapping for exact known JEI category identifiers. Unknown
     * IDs remain unclassified; category titles and identifier substrings never
     * imply recipe semantics.
     */
    public static RecipeClassificationCandidate fromKnownTypeId(
        String typeId,
        String visibleCategoryName,
        boolean projectionComplete
    ) {
        Objects.requireNonNull(typeId, "typeId");
        RecipeCategory category = switch (typeId) {
            case "minecraft:crafting", "minecraft:crafting_shaped", "minecraft:crafting_shapeless",
                "minecraft:plugins/crafting", "minecraft:smithing", "minecraft:plugins/smithing" ->
                RecipeCategory.CRAFTING;
            case "minecraft:smelting", "minecraft:plugins/smelting",
                "minecraft:blasting", "minecraft:plugins/blasting",
                "minecraft:smoking", "minecraft:plugins/smoking",
                "minecraft:campfire_cooking", "minecraft:plugins/campfire",
                "minecraft:plugins/brewing",
                "create:crushing", "create:milling", "create:pressing", "create:compacting",
                "create:mixing", "create:splashing", "create:haunting", "create:deploying",
                "create:cutting", "create:sandpaper_polishing", "create:sequenced_assembly" ->
                RecipeCategory.MACHINE_PROCESSING;
            case "minecraft:stonecutting", "minecraft:plugins/stonecutting" ->
                RecipeCategory.CONVERSION;
            default -> RecipeCategory.OTHER_UNCLASSIFIED;
        };
        boolean needsUnrepresentedProcessData =
            category == RecipeCategory.MACHINE_PROCESSING && typeId.startsWith("create:")
                || typeId.equals("minecraft:plugins/brewing");
        RecipePlanningEligibility eligibility = category == RecipeCategory.OTHER_UNCLASSIFIED
                || !projectionComplete || needsUnrepresentedProcessData
            ? RecipePlanningEligibility.INSUFFICIENT_DATA
            : RecipePlanningEligibility.DETERMINISTIC;
        String safeName = visibleCategoryName == null || visibleCategoryName.isBlank()
                || visibleCategoryName.length() > 256
                || visibleCategoryName.codePoints().anyMatch(Character::isISOControl)
            ? null : visibleCategoryName;
        return new RecipeClassificationCandidate(category, safeName, eligibility);
    }
}
