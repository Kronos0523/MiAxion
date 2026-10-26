package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record WorldRegistryResolveResponse(
        @JsonProperty(required = true) String revision,
        @JsonProperty(required = true) List<WorldRegistryMatch> matches,
        @JsonProperty(required = true) boolean truncated,
        boolean suggestionsOnly) {
    public WorldRegistryResolveResponse(String revision, List<WorldRegistryMatch> matches, boolean truncated) {
        this(revision, matches, truncated, false);
    }
    public WorldRegistryResolveResponse {
        revision = WorldQueryValidation.requireRevision(revision);
        matches = WorldQueryValidation.copyUnique(
            matches,
            ProtocolConstants.WORLD_QUERY_NAME_CANDIDATE_MAX,
            WorldRegistryMatch::key,
            "matches");
    }
}
