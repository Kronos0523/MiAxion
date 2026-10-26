package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.Objects;

public record WorldContainerObservation(
        @JsonProperty(required = true) String blockId,
        @JsonProperty(required = true) String displayName,
        @JsonProperty(required = true) WorldBlockPosition position,
        @JsonProperty(required = true) int distanceBlocks,
        @JsonProperty(required = true) WorldContainerView view,
        @JsonInclude(JsonInclude.Include.NON_EMPTY) List<WorldContainerStack> stacks,
        @JsonInclude(JsonInclude.Include.NON_EMPTY) List<WorldContainerSide> sides) {
    public WorldContainerObservation(
        String blockId,
        String displayName,
        WorldBlockPosition position,
        int distanceBlocks,
        WorldContainerView view
    ) {
        this(blockId, displayName, position, distanceBlocks, view, List.of(), List.of());
    }

    public WorldContainerObservation {
        QuestContractValidation.requireResourceLocation(blockId, "blockId");
        WorldQueryValidation.requireDisplayName(displayName, "displayName");
        Objects.requireNonNull(position, "position");
        if (distanceBlocks < 0) {
            throw new IllegalArgumentException("distanceBlocks must be non-negative.");
        }
        Objects.requireNonNull(view, "view");
        // Nearby container responses are metadata-only. Keep the legacy
        // members in the Java ABI so older callers still deserialize, but do
        // not retain slot/item/component data in the current wire contract.
        stacks = List.of();
        sides = List.of();
    }

    String key() {
        return blockId + "\u0000" + position.key();
    }
}
