package dev.miaxion.bridge.recipe.api;

import java.util.List;
import java.util.Objects;

/** Typed, loader-neutral recipe snapshot sent from an addon to the base. */
public record RecipeCandidate(String recipeId, String typeId, String sourceModId,
    List<RecipeSlotCandidate> inputs, List<RecipeSlotCandidate> catalysts,
    List<RecipeSlotCandidate> outputs, RecipeIdentityCandidate identity,
    RecipeProcessCandidate process, RecipeClassificationCandidate classification) {
    public RecipeCandidate(String recipeId, String typeId, String sourceModId,
        List<RecipeSlotCandidate> inputs, List<RecipeSlotCandidate> catalysts, List<RecipeSlotCandidate> outputs) {
        this(recipeId, typeId, sourceModId, inputs, catalysts, outputs,
            RecipeIdentityCandidate.registered(), null, RecipeClassificationCandidate.unclassified());
    }
    public RecipeCandidate(String recipeId, String typeId, String sourceModId,
        List<RecipeSlotCandidate> inputs, List<RecipeSlotCandidate> catalysts,
        List<RecipeSlotCandidate> outputs, RecipeIdentityCandidate identity) {
        this(recipeId, typeId, sourceModId, inputs, catalysts, outputs, identity,
            null, RecipeClassificationCandidate.unclassified());
    }
    public RecipeCandidate(String recipeId, String typeId, String sourceModId,
        List<RecipeSlotCandidate> inputs, List<RecipeSlotCandidate> catalysts,
        List<RecipeSlotCandidate> outputs, RecipeIdentityCandidate identity,
        RecipeClassificationCandidate classification) {
        this(recipeId, typeId, sourceModId, inputs, catalysts, outputs, identity, null, classification);
    }
    public RecipeCandidate {
        if (!RecipeQuery.isResourceLocation(recipeId) || !RecipeQuery.isResourceLocation(typeId)
            || sourceModId == null || !sourceModId.matches("[a-z0-9_.-]{1,64}")) throw new IllegalArgumentException("Invalid recipe identity.");
        Objects.requireNonNull(identity);
        Objects.requireNonNull(classification);
        if (identity.isRegistered() == recipeId.startsWith("miaxion:derived/")) throw new IllegalArgumentException("Recipe identity does not match its ID.");
        inputs = copy(inputs); catalysts = copy(catalysts); outputs = copy(outputs);
    }
    private static List<RecipeSlotCandidate> copy(List<RecipeSlotCandidate> value) {
        Objects.requireNonNull(value); if (value.size() > 256 || value.stream().anyMatch(Objects::isNull)) throw new IllegalArgumentException("Invalid recipe slots."); return List.copyOf(value);
    }
}
