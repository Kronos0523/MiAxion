package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashSet;
import java.util.List;

public record WorldRegistryDescribeResponse(
        @JsonProperty(required = true) String revision,
        @JsonProperty(required = true) List<WorldRegistryMatch> descriptions,
        @JsonProperty(required = true) List<WorldRegistryReference> missing) {
    public WorldRegistryDescribeResponse {
        revision = WorldQueryValidation.requireRevision(revision);
        descriptions = WorldQueryValidation.copyUnique(
            descriptions,
            ProtocolConstants.WORLD_QUERY_RESULT_MAX,
            WorldRegistryMatch::key,
            "descriptions");
        missing = WorldQueryValidation.copyUnique(
            missing,
            ProtocolConstants.WORLD_QUERY_RESULT_MAX,
            WorldRegistryReference::key,
            "missing");
        if (descriptions.size() + missing.size() > ProtocolConstants.WORLD_QUERY_RESULT_MAX) {
            throw new IllegalArgumentException("descriptions and missing exceed their bounds.");
        }
        var seen = new HashSet<String>();
        descriptions.forEach(value -> seen.add(value.key()));
        for (WorldRegistryReference reference : missing) {
            if (!seen.add(reference.key())) {
                throw new IllegalArgumentException("descriptions and missing must not overlap.");
            }
        }
    }
}
