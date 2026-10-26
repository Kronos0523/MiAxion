package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record EntityResolveResponse(
        @JsonProperty(required = true) String revision,
        @JsonProperty(required = true) List<EntityResolveMatch> matches,
        @JsonProperty(required = true) boolean truncated,
        String nextContinuation) {

    public EntityResolveResponse(String revision, List<EntityResolveMatch> matches, boolean truncated) {
        this(revision, matches, truncated, null);
    }

    public EntityResolveResponse {
        Objects.requireNonNull(revision, "revision");
        Objects.requireNonNull(matches, "matches");
        if (revision.isEmpty() || revision.length() > 64 || matches.size() > 8 ||
            matches.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Entity resolve response is outside its bounds.");
        }
        matches = List.copyOf(matches);
        EntityResolveRequest.requireContinuation(nextContinuation);
        if (nextContinuation != null && (matches.size() != 8 || !truncated))
            throw new IllegalArgumentException("An entity continuation requires a full incomplete page.");
    }
}
