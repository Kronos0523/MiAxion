package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public record EntityDescribeRequest(
        @JsonProperty(required = true) List<EntityReference> entities) {
    public EntityDescribeRequest {
        Objects.requireNonNull(entities, "entities");
        if (entities.isEmpty() || entities.size() > 128
            || entities.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException(
                "entities must contain 1-128 non-null references.");
        }
        var seen = new HashSet<String>();
        for (EntityReference entity : entities) {
            if (!seen.add(entity.key())) {
                throw new IllegalArgumentException(
                    "entities must contain unique kind/resourceId pairs.");
            }
        }
        entities = List.copyOf(entities);
    }
}
