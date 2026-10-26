package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Objects;

public record WorldMachinesResponse(
        @JsonProperty(required = true) WorldQuerySummary summary,
        @JsonProperty(required = true) WorldChunkRange chunkRange,
        @JsonProperty(required = true) WorldVerticalRange verticalRange,
        @JsonProperty(required = true) java.util.List<WorldMachineObservation> machines,
        @JsonInclude(JsonInclude.Include.NON_NULL) WorldScanWindow window,
        @JsonInclude(JsonInclude.Include.NON_NULL) String nextCursor) {
    public WorldMachinesResponse(WorldQuerySummary summary, WorldChunkRange chunkRange,
            WorldVerticalRange verticalRange, java.util.List<WorldMachineObservation> machines, WorldScanWindow window) {
        this(summary, chunkRange, verticalRange, machines, window, null);
    }
    public WorldMachinesResponse(WorldQuerySummary summary, WorldChunkRange chunkRange,
            WorldVerticalRange verticalRange, java.util.List<WorldMachineObservation> machines) {
        this(summary, chunkRange, verticalRange, machines, null);
    }
    public WorldMachinesResponse {
        WorldQueryValidation.requireNearbySummary(summary);
        WorldQueryValidation.requireContinuation(summary, nextCursor);
        Objects.requireNonNull(chunkRange, "chunkRange");
        Objects.requireNonNull(verticalRange, "verticalRange");
        machines = WorldQueryValidation.copyUnique(
            machines,
            ProtocolConstants.WORLD_QUERY_RESULT_MAX,
            WorldMachineObservation::key,
            "machines");
    }
}
