package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public record WorldMachineObservation(
        @JsonProperty(required = true) String blockId,
        @JsonProperty(required = true) String displayName,
        @JsonProperty(required = true) WorldBlockPosition position,
        @JsonProperty(required = true) int distanceBlocks,
        @JsonProperty(required = true) String adapterId,
        @JsonProperty(required = true) WorldMachineCandidateDomain candidateDomain,
        @JsonProperty(required = true) WorldMachineState state,
        @JsonInclude(JsonInclude.Include.NON_EMPTY) List<WorldMachineFeature> features) {
    public WorldMachineObservation(
        String blockId,
        String displayName,
        WorldBlockPosition position,
        int distanceBlocks,
        String adapterId,
        WorldMachineCandidateDomain candidateDomain,
        WorldMachineState state
    ) {
        this(blockId, displayName, position, distanceBlocks, adapterId,
            candidateDomain, state, List.of());
    }

    public WorldMachineObservation {
        QuestContractValidation.requireResourceLocation(blockId, "blockId");
        WorldQueryValidation.requireDisplayName(displayName, "displayName");
        Objects.requireNonNull(position, "position");
        QuestContractValidation.requireOpaqueId(adapterId, "adapterId");
        if (distanceBlocks < 0) {
            throw new IllegalArgumentException("distanceBlocks must be non-negative.");
        }
        Objects.requireNonNull(candidateDomain, "candidateDomain");
        Objects.requireNonNull(state, "state");
        features = features == null ? List.of() : features;
        if (features.size() > ProtocolConstants.WORLD_QUERY_MACHINE_FEATURE_MAX
            || features.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("features exceeds its bounds.");
        }
        var seen = new HashSet<String>();
        var sorted = new ArrayList<>(features);
        sorted.sort(java.util.Comparator.comparing(WorldMachineFeature::id));
        for (WorldMachineFeature feature : sorted) {
            if (!seen.add(feature.id())) {
                throw new IllegalArgumentException("features must have unique IDs.");
            }
        }
        features = List.copyOf(sorted);
    }

    String key() {
        return blockId + "\u0000" + position.key();
    }
}
