package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;

public record InventorySnapshotResponse(
        @JsonProperty(required = true) String revision,
        @JsonProperty(required = true) List<InventorySlot> slots) {

    public InventorySnapshotResponse {
        Objects.requireNonNull(revision, "revision");
        slots = List.copyOf(slots);
    }
}
