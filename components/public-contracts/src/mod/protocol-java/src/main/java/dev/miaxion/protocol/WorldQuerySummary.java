package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

// Authority-specific response records keep server observations separate from generator-only results.
public record WorldQuerySummary(
        @JsonProperty(required = true) WorldQueryAuthority authority,
        @JsonProperty(required = true) String dimensionId,
        @JsonProperty(required = true) long observedAtTick,
        @JsonProperty(required = true) String queryGeneration,
        @JsonProperty(required = true) int requested,
        @JsonProperty(required = true) int loaded,
        @JsonProperty(required = true) int examined,
        @JsonProperty(required = true) int matched,
        @JsonProperty(required = true) boolean complete,
        @JsonProperty(required = true) boolean truncated,
        @JsonProperty(required = true) List<String> warnings) {
    public WorldQuerySummary {
        Objects.requireNonNull(authority, "authority");
        QuestContractValidation.requireResourceLocation(dimensionId, "dimensionId");
        if (observedAtTick < 0) {
            throw new IllegalArgumentException("observedAtTick must be non-negative.");
        }
        QuestContractValidation.requireOpaqueId(queryGeneration, "queryGeneration");
        // Examined is a progress counter, not the bounded size of the returned page.
        if (requested < 0 || requested > 1024 || loaded < 0 || loaded > 1024
            || examined < 0 || matched < 0 || matched > 1024) {
            throw new IllegalArgumentException("World query counts are outside their bounds.");
        }
        Objects.requireNonNull(warnings, "warnings");
        if (warnings.size() > ProtocolConstants.WORLD_QUERY_WARNING_MAX
            || warnings.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("warnings exceeds its bounds.");
        }
        var seen = new HashSet<String>();
        for (String warning : warnings) {
            WorldQueryValidation.requireWarning(warning, "warning");
            if (!seen.add(warning)) {
                throw new IllegalArgumentException("warnings must be unique.");
            }
        }
        warnings = List.copyOf(warnings);
    }
}
