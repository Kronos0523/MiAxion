package dev.miaxion.bridge.recipe.api;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.TreeMap;

/** Closed reason counts plus bounded provider category IDs for failed empty-input projections. */
public record RecipeProviderDiagnostics(
    int sourceEnumerationFailures,
    int targetDisplayReadFailures,
    int unscopedDisplayReadFailures,
    int unsupportedTargetEntries,
    int unrepresentedTargetRequirements,
    int unprojectableTargetDisplays,
    Map<Reason, Integer> reasonCounts,
    Map<String, Integer> rejectedEmptyInputCategories
) {
    private static final int MAX_REASON_COUNT = 4096;
    private static final int MAX_EMPTY_INPUT_CATEGORY_BUCKETS = 16;
    private static final java.util.regex.Pattern CATEGORY_ID = java.util.regex.Pattern.compile(
        "[a-z0-9_.-]{1,64}:[a-z0-9_./-]{1,63}");

    /** Fixed diagnostic stages and projection rejection reasons; never carries game content. */
    public enum Reason {
        LEGACY_ITEM_ONLY_SOURCE("legacyItemOnlySource"),
        TARGET_RESOURCE_TYPE_UNAVAILABLE("target_resource_type_unavailable"),
        TARGET_OUTPUT_ENTRIES_READ_FAILURE("targetOutputEntriesReadFailures"),
        TARGET_INPUT_ENTRIES_READ_FAILURE("targetInputEntriesReadFailures"),
        TARGET_REQUIRED_ENTRIES_READ_FAILURE("targetRequiredEntriesReadFailures"),
        TARGET_INPUT_SLOT_PROJECTION_FAILURE("targetInputSlotProjectionFailures"),
        TARGET_OUTPUT_SLOT_PROJECTION_FAILURE("targetOutputSlotProjectionFailures"),
        TARGET_DISPLAY_IDENTITY_READ_FAILURE("targetDisplayIdentityReadFailures"),
        TARGET_QUERY_FILTER_FAILURE("targetQueryFilterFailures"),
        TARGET_CANDIDATE_PROJECTION_FAILURE("targetCandidateProjectionFailures"),
        TARGET_CANDIDATE_CLASSIFICATION_FAILURE("targetCandidateClassificationFailures"),
        TARGET_CANDIDATE_IDENTITY_FAILURE("targetCandidateIdentityFailures"),
        TARGET_CANDIDATE_CONSTRUCTION_FAILURE("targetCandidateConstructionFailures"),
        TARGET_CANDIDATE_RETENTION_FAILURE("targetCandidateRetentionFailures"),
        TARGET_EMPTY_INPUT_CATEGORY_DIAGNOSTIC_OMITTED("targetEmptyInputCategoryDiagnosticsOmitted"),
        TARGET_DISPLAY_WITHOUT_OUTPUTS("targetDisplaysWithoutOutputs"),
        TARGET_DISPLAY_WITH_REJECTED_EMPTY_INPUTS("targetDisplaysWithRejectedEmptyInputs"),
        TARGET_CANDIDATE_CONSTRUCTION_INVALID_RECIPE_ID("targetCandidateConstructionInvalidRecipeIdFailures"),
        TARGET_CANDIDATE_CONSTRUCTION_INVALID_TYPE_ID("targetCandidateConstructionInvalidTypeIdFailures"),
        TARGET_CANDIDATE_CONSTRUCTION_INVALID_SOURCE_MOD_ID("targetCandidateConstructionInvalidSourceModIdFailures"),
        TARGET_CANDIDATE_CONSTRUCTION_IDENTITY_MISMATCH("targetCandidateConstructionIdentityMismatches"),
        TARGET_CANDIDATE_CONSTRUCTION_INVALID_SLOTS("targetCandidateConstructionInvalidSlotsFailures"),
        TARGET_CANDIDATE_CONSTRUCTION_UNCLASSIFIED("targetCandidateConstructionUnclassifiedFailures");

        private final String token;

        Reason(String token) { this.token = token; }

        public String token() { return token; }
    }

    public RecipeProviderDiagnostics {
        validate(sourceEnumerationFailures);
        validate(targetDisplayReadFailures);
        validate(unscopedDisplayReadFailures);
        validate(unsupportedTargetEntries);
        validate(unrepresentedTargetRequirements);
        validate(unprojectableTargetDisplays);
        if (reasonCounts == null) throw new IllegalArgumentException("Recipe diagnostic reasons are required.");
        EnumMap<Reason, Integer> boundedReasons = new EnumMap<>(Reason.class);
        reasonCounts.forEach((reason, count) -> {
            if (reason == null || count == null) throw new IllegalArgumentException("Recipe diagnostic reason is invalid.");
            validate(count);
            if (count > 0) boundedReasons.put(reason, count);
        });
        reasonCounts = Collections.unmodifiableMap(boundedReasons);
        if (rejectedEmptyInputCategories == null
            || rejectedEmptyInputCategories.size() > MAX_EMPTY_INPUT_CATEGORY_BUCKETS) {
            throw new IllegalArgumentException("Invalid rejected empty-input category counts.");
        }
        TreeMap<String, Integer> boundedCategories = new TreeMap<>();
        rejectedEmptyInputCategories.forEach((categoryId, count) -> {
            if (categoryId == null || !CATEGORY_ID.matcher(categoryId).matches() || count == null) {
                throw new IllegalArgumentException("Invalid rejected empty-input category count.");
            }
            validate(count);
            if (count > 0) boundedCategories.put(categoryId, count);
        });
        rejectedEmptyInputCategories = Collections.unmodifiableMap(boundedCategories);
    }

    /** Retains the original public constructor for existing addon binaries and source. */
    public RecipeProviderDiagnostics(int sourceEnumerationFailures, int targetDisplayReadFailures,
        int unscopedDisplayReadFailures, int unsupportedTargetEntries,
        int unrepresentedTargetRequirements, int unprojectableTargetDisplays) {
        this(sourceEnumerationFailures, targetDisplayReadFailures, unscopedDisplayReadFailures,
            unsupportedTargetEntries, unrepresentedTargetRequirements, unprojectableTargetDisplays,
            Map.of(), Map.of());
    }

    /** Retains the previous diagnostic-reason constructor for existing addon binaries and source. */
    public RecipeProviderDiagnostics(int sourceEnumerationFailures, int targetDisplayReadFailures,
        int unscopedDisplayReadFailures, int unsupportedTargetEntries,
        int unrepresentedTargetRequirements, int unprojectableTargetDisplays,
        Map<Reason, Integer> reasonCounts) {
        this(sourceEnumerationFailures, targetDisplayReadFailures, unscopedDisplayReadFailures,
            unsupportedTargetEntries, unrepresentedTargetRequirements, unprojectableTargetDisplays,
            reasonCounts, Map.of());
    }

    public static RecipeProviderDiagnostics empty() {
        return new RecipeProviderDiagnostics(0, 0, 0, 0, 0, 0);
    }

    public boolean hasIncompleteProjection() {
        return sourceEnumerationFailures > 0
            || targetDisplayReadFailures > 0
            || unscopedDisplayReadFailures > 0
            || unsupportedTargetEntries > 0
            || unrepresentedTargetRequirements > 0
            || unprojectableTargetDisplays > 0
            || !reasonCounts.isEmpty();
    }

    /** Stable diagnostic token: fixed field names and bounded integer counts only. */
    public String diagnosticToken() {
        return "sourceEnumerationFailures=" + sourceEnumerationFailures
            + ",targetDisplayReadFailures=" + targetDisplayReadFailures
            + ",unscopedDisplayReadFailures=" + unscopedDisplayReadFailures
            + ",unsupportedTargetEntries=" + unsupportedTargetEntries
            + ",unrepresentedTargetRequirements=" + unrepresentedTargetRequirements
            + ",unprojectableTargetDisplays=" + unprojectableTargetDisplays
            + reasonCounts.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> "," + entry.getKey().token() + "=" + entry.getValue())
                .reduce("", String::concat)
            + ",rejectedEmptyInputCategories=" + rejectedEmptyInputCategories.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .reduce((left, right) -> left + "|" + right)
                .orElse("none");
    }

    private static void validate(int count) {
        if (count < 0 || count > MAX_REASON_COUNT) {
            throw new IllegalArgumentException("Invalid recipe diagnostic count.");
        }
    }
}
