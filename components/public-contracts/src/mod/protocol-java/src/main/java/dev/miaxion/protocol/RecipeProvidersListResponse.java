package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;

/** Bounded metadata only: no recipe, inventory, world or provider arguments. */
public record RecipeProvidersListResponse(
    @JsonProperty(required = true) List<Provider> providers
) {
    public RecipeProvidersListResponse { providers = List.copyOf(providers); }

    public record Provider(
        @JsonProperty(required = true) String addonId,
        @JsonProperty(required = true) String addonVersion,
        @JsonProperty(required = true) String targetModId,
        @JsonProperty(required = true) Readiness query,
        @JsonProperty(required = true) Readiness uses,
        Long sourceGeneration
    ) {
        public Provider(String addonId, String addonVersion, String targetModId,
            Readiness query, Readiness uses) {
            this(addonId, addonVersion, targetModId, query, uses, null);
        }
        public Provider {
            Objects.requireNonNull(addonId);
            Objects.requireNonNull(addonVersion);
            Objects.requireNonNull(targetModId);
            Objects.requireNonNull(query);
            Objects.requireNonNull(uses);
            if (sourceGeneration != null && sourceGeneration < 0) throw new IllegalArgumentException("sourceGeneration is invalid.");
        }
    }

    public record Readiness(
        @JsonProperty(required = true) String status,
        @JsonProperty(required = true) String reason
    ) {
        public Readiness { Objects.requireNonNull(status); Objects.requireNonNull(reason); }
    }
}
