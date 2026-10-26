package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Objects;

public record WorldContainersResponse(
        @JsonProperty(required = true) WorldQuerySummary summary,
        @JsonProperty(required = true) WorldChunkRange chunkRange,
        @JsonProperty(required = true) WorldVerticalRange verticalRange,
        @JsonProperty(required = true) java.util.List<WorldContainerObservation> containers,
        @JsonInclude(JsonInclude.Include.NON_NULL) WorldScanWindow window,
        @JsonInclude(JsonInclude.Include.NON_NULL) String nextCursor) {
    public WorldContainersResponse(WorldQuerySummary summary, WorldChunkRange chunkRange,
            WorldVerticalRange verticalRange, java.util.List<WorldContainerObservation> containers, WorldScanWindow window) {
        this(summary, chunkRange, verticalRange, containers, window, null);
    }
    public WorldContainersResponse(WorldQuerySummary summary, WorldChunkRange chunkRange,
            WorldVerticalRange verticalRange, java.util.List<WorldContainerObservation> containers) {
        this(summary, chunkRange, verticalRange, containers, null);
    }
    public WorldContainersResponse {
        WorldQueryValidation.requireNearbySummary(summary);
        WorldQueryValidation.requireContinuation(summary, nextCursor);
        Objects.requireNonNull(chunkRange, "chunkRange");
        Objects.requireNonNull(verticalRange, "verticalRange");
        containers = WorldQueryValidation.copyUnique(
            containers,
            ProtocolConstants.WORLD_QUERY_RESULT_MAX,
            WorldContainerObservation::key,
            "containers");
        int stacks = containers.stream()
            .mapToInt(value -> value.stacks().size()
                + value.sides().stream().mapToInt(side -> side.stacks().size()).sum())
            .sum();
        if (stacks > ProtocolConstants.WORLD_QUERY_CONTAINER_STACK_MAX) {
            throw new IllegalArgumentException("container stack count exceeds its bounds.");
        }
    }
}
