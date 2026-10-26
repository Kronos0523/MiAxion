package dev.miaxion.bridge.recipe.api;

import java.util.Objects;

public record RecipeIdentityCandidate(String kind, String contentSha256) {
    public RecipeIdentityCandidate {
        Objects.requireNonNull(kind);
        boolean registered = "registered".equals(kind) && contentSha256 == null;
        boolean derived = "derived".equals(kind) && contentSha256 != null
            && contentSha256.matches("[0-9a-f]{64}");
        if (!registered && !derived) throw new IllegalArgumentException("Invalid recipe identity.");
    }
    public static RecipeIdentityCandidate registered() { return new RecipeIdentityCandidate("registered", null); }
    public static RecipeIdentityCandidate derived(String value) { return new RecipeIdentityCandidate("derived", value); }
    public boolean isRegistered() { return "registered".equals(kind); }
}
