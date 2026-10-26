package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Objects;
import java.util.regex.Pattern;

/** Bounded server-side selection policy for recipe queries. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record RecipeSelectionRequest(
        @JsonProperty(required = true) String mode,
        @JsonProperty(required = true) int maxResults,
        String preferredRecipeId) {

    private static final Pattern RESOURCE_LOCATION = Pattern.compile(
        "^[a-z0-9_.-]+:[a-z0-9_./-]+$");

    public RecipeSelectionRequest(String mode, int maxResults) {
        this(mode, maxResults, null);
    }

    public RecipeSelectionRequest {
        Objects.requireNonNull(mode, "mode");
        if (!mode.equals("recommended-direct")
            && !mode.equals("planning-direct")
            && !mode.equals("recommended-process-families")) {
            throw new IllegalArgumentException("Unknown recipe selection mode.");
        }
        if (maxResults < 1 || maxResults > 5) {
            throw new IllegalArgumentException("maxResults must be between 1 and 5.");
        }
        if (preferredRecipeId != null) {
            if ((!mode.equals("recommended-direct") && !mode.equals("planning-direct"))
                || preferredRecipeId.length() > 128
                || !RESOURCE_LOCATION.matcher(preferredRecipeId).matches()) {
                throw new IllegalArgumentException(
                    "preferredRecipeId is only valid for direct selection and must be a resource location.");
            }
        }
    }

    public static RecipeSelectionRequest recommendedDirect() {
        return new RecipeSelectionRequest("recommended-direct", 3);
    }

    public static RecipeSelectionRequest recommendedDirect(String preferredRecipeId) {
        return new RecipeSelectionRequest("recommended-direct", 3, preferredRecipeId);
    }

    public static RecipeSelectionRequest recommendedProcessFamilies() {
        return new RecipeSelectionRequest("recommended-process-families", 5);
    }
}
