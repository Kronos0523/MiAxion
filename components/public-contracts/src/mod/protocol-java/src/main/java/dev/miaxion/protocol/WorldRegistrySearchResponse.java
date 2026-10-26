package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** Registration is not evidence of nearby generation or a locate result. */
public record WorldRegistrySearchResponse(
    @JsonProperty(required = true) String revision,
    @JsonProperty(required = true) List<WorldRegistryMatch> entries,
    @JsonProperty(required = true) int total,
    String nextCursor) {
    public WorldRegistrySearchResponse {
        revision = WorldQueryValidation.requireRevision(revision);
        entries = WorldQueryValidation.copyUnique(entries, 64, WorldRegistryMatch::key, "entries");
        if (total < entries.size()) throw new IllegalArgumentException("Invalid registry total");
        if (nextCursor != null && !nextCursor.matches("[a-f0-9]{32}")) throw new IllegalArgumentException("Invalid cursor");
    }
}
