package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RecipeResponse(
        @JsonProperty(required = true) List<RecipeDto> recipes,
        @JsonProperty(required = true) boolean truncated,
        Boolean sourceComplete,
        Boolean selectionComplete,
        Integer candidatesEvaluated,
        String selectionMode,
        String inventoryRevision,
        List<String> selectionOrder,
        String nextSourceCursor,
        Integer sourcePageSize,
        @JsonInclude(JsonInclude.Include.NON_EMPTY) List<String> resourceWarnings) {

    public RecipeResponse(List<RecipeDto> recipes, boolean truncated, Boolean sourceComplete,
        Boolean selectionComplete, Integer candidatesEvaluated, String selectionMode, String inventoryRevision,
        List<String> selectionOrder, String nextSourceCursor, Integer sourcePageSize) {
        this(recipes, truncated, sourceComplete, selectionComplete, candidatesEvaluated, selectionMode,
            inventoryRevision, selectionOrder, nextSourceCursor, sourcePageSize, null);
    }
    public RecipeResponse(List<RecipeDto> recipes, boolean truncated) {
        this(recipes, truncated, null, null, null, null, null, null, null, null);
    }

    public RecipeResponse(
        List<RecipeDto> recipes,
        boolean truncated,
        Boolean sourceComplete,
        Boolean selectionComplete,
        Integer candidatesEvaluated,
        String selectionMode,
        String inventoryRevision,
        List<String> selectionOrder) {
        this(recipes, truncated, sourceComplete, selectionComplete, candidatesEvaluated,
            selectionMode, inventoryRevision, selectionOrder, null, null);
    }

    public RecipeResponse(
        List<RecipeDto> recipes,
        boolean truncated,
        Boolean sourceComplete,
        Boolean selectionComplete,
        Integer candidatesEvaluated,
        String selectionMode,
        String inventoryRevision,
        List<String> selectionOrder,
        String nextSourceCursor) {
        this(recipes, truncated, sourceComplete, selectionComplete, candidatesEvaluated,
            selectionMode, inventoryRevision, selectionOrder, nextSourceCursor, null);
    }

    public RecipeResponse {
        resourceWarnings = resourceWarnings == null ? List.of() : List.copyOf(resourceWarnings);
        if (resourceWarnings.size() > 32 || resourceWarnings.stream().anyMatch(v -> v == null || !v.matches("[A-Z0-9_]{1,64}")))
            throw new IllegalArgumentException("Invalid bounded recipe diagnostic codes.");
        Objects.requireNonNull(recipes, "recipes");
        if (recipes.size() > 128) {
            throw new IllegalArgumentException(
                "recipes exceeds its maximum size.");
        }
        if (candidatesEvaluated != null && candidatesEvaluated < 0) {
            throw new IllegalArgumentException("candidatesEvaluated cannot be negative.");
        }
        if (selectionComplete != null && sourceComplete == null) {
            throw new IllegalArgumentException("selectionComplete requires sourceComplete.");
        }
        if (selectionMode != null
            && !selectionMode.equals("recommended-direct")
            && !selectionMode.equals("planning-direct")
            && !selectionMode.equals("recommended-process-families")) {
            throw new IllegalArgumentException("Unknown recipe selection mode.");
        }
        if (inventoryRevision != null
            && (inventoryRevision.isEmpty() || inventoryRevision.length() > 64)) {
            throw new IllegalArgumentException("Invalid inventory revision.");
        }
        if (selectionOrder != null) {
            if (selectionOrder.size() > 5 || selectionOrder.stream().anyMatch(Objects::isNull)) {
                throw new IllegalArgumentException("selectionOrder exceeds its maximum size.");
            }
            Set<String> unique = new HashSet<>();
            for (String value : selectionOrder) {
                if (value.length() > 128
                    || !Pattern.matches("^[a-z0-9_.-]+:[a-z0-9_./-]+$", value)
                    || !unique.add(value)) {
                    throw new IllegalArgumentException("selectionOrder contains an invalid ID.");
                }
            }
        }
        if (nextSourceCursor != null && (nextSourceCursor.length() < 1
            || nextSourceCursor.length() > ProtocolConstants.RECIPE_SOURCE_CURSOR_MAX
            || !nextSourceCursor.chars().allMatch(value ->
                value >= 'a' && value <= 'z' || value >= '0' && value <= '9')
            || !truncated)) {
            throw new IllegalArgumentException("nextSourceCursor is invalid.");
        }
        if (nextSourceCursor != null && sourcePageSize == null) {
            throw new IllegalArgumentException(
                "A source continuation requires the originating source page size.");
        }
        if (sourcePageSize != null && (sourcePageSize < 1
            || sourcePageSize > ProtocolConstants.RECIPE_SOURCE_PAGE_MAX)) {
            throw new IllegalArgumentException("sourcePageSize is outside its bounds.");
        }
        recipes = List.copyOf(recipes);
        selectionOrder = selectionOrder == null ? null : List.copyOf(selectionOrder);
    }
}
