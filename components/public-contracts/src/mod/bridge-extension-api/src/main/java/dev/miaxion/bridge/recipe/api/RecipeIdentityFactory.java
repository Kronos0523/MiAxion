package dev.miaxion.bridge.recipe.api;

import com.fasterxml.jackson.databind.JsonNode;
import java.security.MessageDigest;
import java.util.HexFormat;

/** Creates the same stable derived IDs without exposing the base game package. */
public final class RecipeIdentityFactory {
    private RecipeIdentityFactory() { }
    public static DerivedIdentity derived(JsonNode material) {
        try {
            String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(RecipeCanonicalJson.canonicalUtf8(material)));
            return new DerivedIdentity(RecipeIdentityCandidate.derived(hash), "miaxion:derived/" + hash);
        } catch (java.security.NoSuchAlgorithmException impossible) { throw new AssertionError(impossible); }
    }
    public record DerivedIdentity(RecipeIdentityCandidate identity, String recipeId) {
        public DerivedIdentity { if (identity == null || identity.isRegistered() || recipeId == null || !recipeId.equals("miaxion:derived/" + identity.contentSha256())) throw new IllegalArgumentException("Inconsistent derived identity."); }
    }
}
