package dev.miaxion.bridge.recipe.api;

import java.math.BigInteger;

/** Detached typed resource and exact rational amount. Null amount/unit means unknown, never one. */
public record RecipeResourceValue(String kind, String resourceId, String ingredientTypeUid,
    String displayName, String numerator, String denominator, String unit) {
    public RecipeResourceValue {
        if (kind == null || !java.util.Set.of("item", "fluid", "chemical", "energy", "custom").contains(kind)
            || !RecipeQuery.isResourceLocation(resourceId)
            || ingredientTypeUid == null || ingredientTypeUid.isBlank() || ingredientTypeUid.length() > 512
            || ingredientTypeUid.chars().anyMatch(Character::isISOControl)
            || displayName != null && (displayName.length() > 1024 || displayName.chars().anyMatch(Character::isISOControl)))
            throw new IllegalArgumentException("Invalid typed recipe resource.");
        if ((numerator == null) != (denominator == null)) throw new IllegalArgumentException("Incomplete resource amount.");
        if (unit != null && (unit.isBlank() || unit.length() > 512 || unit.chars().anyMatch(Character::isISOControl)))
            throw new IllegalArgumentException("Invalid native resource unit.");
        if (numerator != null) {
            if (!numerator.matches("[0-9]{1,128}") || !denominator.matches("[0-9]{1,128}")) throw new IllegalArgumentException("Invalid exact amount.");
            var n = new BigInteger(numerator); var d = new BigInteger(denominator);
            if (n.signum() <= 0 || d.signum() <= 0) throw new IllegalArgumentException("Resource amount must be positive.");
            var gcd = n.gcd(d); numerator = n.divide(gcd).toString(); denominator = d.divide(gcd).toString();
        }
    }
    public boolean quantityKnown() { return numerator != null && unit != null; }
    public String identityKey() { return kind + "\u001f" + resourceId + "\u001f" + ingredientTypeUid; }
}
