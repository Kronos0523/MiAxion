package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import java.util.regex.Pattern;

/** Stable identity metadata for a recipe response. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record RecipeIdentityDto(
        @JsonProperty(required = true) String kind,
        String algorithm,
        String contentSha256) {
    private static final String REGISTERED = "registered";
    private static final String DERIVED = "derived";
    private static final String SHA256_CANONICAL_V1 = "sha256-canonical-v1";
    private static final Pattern SHA256 = Pattern.compile("^[0-9a-f]{64}$");

    public RecipeIdentityDto {
        Objects.requireNonNull(kind, "kind");
        switch (kind) {
            case REGISTERED -> {
                if (algorithm != null || contentSha256 != null) {
                    throw new IllegalArgumentException(
                        "registered identity cannot contain derived fields.");
                }
            }
            case DERIVED -> {
                if (!SHA256_CANONICAL_V1.equals(algorithm)
                    || contentSha256 == null
                    || !SHA256.matcher(contentSha256).matches()) {
                    throw new IllegalArgumentException(
                        "derived identity requires sha256-canonical-v1 and a lowercase SHA-256.");
                }
            }
            default -> throw new IllegalArgumentException(
                "identity kind must be registered or derived.");
        }
    }

    public static RecipeIdentityDto registered() {
        return new RecipeIdentityDto(REGISTERED, null, null);
    }

    public static RecipeIdentityDto derived(String algorithm, String contentSha256) {
        return new RecipeIdentityDto(DERIVED, algorithm, contentSha256);
    }

    @JsonIgnore
    public boolean isRegistered() {
        return REGISTERED.equals(kind);
    }

    @JsonIgnore
    public boolean isDerived() {
        return DERIVED.equals(kind);
    }
}
