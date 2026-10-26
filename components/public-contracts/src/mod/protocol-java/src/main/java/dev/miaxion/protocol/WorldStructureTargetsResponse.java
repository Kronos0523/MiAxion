package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

public record WorldStructureTargetsResponse(
        @JsonProperty(required = true) WorldQuerySummary summary,
        @JsonProperty(required = true) String structureId,
        @JsonProperty(required = true) String coverage,
        @JsonProperty(required = true) java.util.List<WorldTargetObservation> targets,
        String nextCursor) {
    public WorldStructureTargetsResponse {
        WorldQueryValidation.requireNearbySummary(summary);
        WorldQueryValidation.requireContinuation(summary, nextCursor);
        QuestContractValidation.requireResourceLocation(structureId, "structureId");
        if (!"structure_covered_chunks".equals(coverage)) {
            throw new IllegalArgumentException("Unknown structure coverage.");
        }
        targets = WorldQueryValidation.copyUnique(
            targets,
            ProtocolConstants.WORLD_QUERY_RESULT_MAX,
            WorldTargetObservation::key,
            "targets");
    }

    public WorldStructureTargetsResponse(
            WorldQuerySummary summary,
            String structureId,
            String coverage,
            java.util.List<WorldTargetObservation> targets) {
        this(summary, structureId, coverage, targets, null);
    }
}
