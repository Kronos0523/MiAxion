package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public record EntityDescribeResponse(
        @JsonProperty(required = true) String revision,
        @JsonProperty(required = true) List<EntityDescription> descriptions,
        @JsonProperty(required = true) List<EntityReference> missing) {
    public EntityDescribeResponse {
        Objects.requireNonNull(revision, "revision");
        Objects.requireNonNull(descriptions, "descriptions");
        Objects.requireNonNull(missing, "missing");
        if (revision.isEmpty() || revision.length() > 64
            || descriptions.size() > 128 || missing.size() > 128
            || descriptions.size() + missing.size() > 128
            || descriptions.stream().anyMatch(Objects::isNull)
            || missing.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException(
                "Entity describe response is outside its bounds.");
        }
        var seen = new HashSet<String>();
        for (EntityDescription description : descriptions) {
            if (!seen.add(description.reference().key())) {
                throw new IllegalArgumentException(
                    "descriptions must contain unique entities.");
            }
        }
        for (EntityReference reference : missing) {
            if (!seen.add(reference.key())) {
                throw new IllegalArgumentException(
                    "descriptions and missing must not overlap.");
            }
        }
        descriptions = List.copyOf(descriptions);
        missing = List.copyOf(missing);
    }
}
