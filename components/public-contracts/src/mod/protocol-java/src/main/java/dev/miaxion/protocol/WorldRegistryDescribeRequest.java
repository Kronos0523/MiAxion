package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public record WorldRegistryDescribeRequest(
        @JsonProperty(required = true) List<WorldRegistryReference> entries) {
    public WorldRegistryDescribeRequest {
        Objects.requireNonNull(entries, "entries");
        if (entries.size() < 1 || entries.size() > 128
            || entries.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("entries must contain 1-128 references.");
        }
        var seen = new HashSet<String>();
        for (WorldRegistryReference entry : entries) {
            if (!seen.add(entry.key())) {
                throw new IllegalArgumentException("entries must be unique kind/resourceId pairs.");
            }
        }
        entries = List.copyOf(entries);
    }
}
