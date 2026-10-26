package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A bounded, read-only machine fact.  Unknown facts are omitted rather than
 * guessed; an optional resource ID identifies a fluid or other registered
 * value without carrying a Minecraft object across the protocol boundary.
 */
public record WorldMachineFeature(
        @JsonProperty(required = true) String id,
        @JsonProperty(required = true) String unit,
        @JsonProperty(required = true) long value,
        @JsonInclude(JsonInclude.Include.NON_NULL) Long capacity,
        @JsonInclude(JsonInclude.Include.NON_NULL) String resourceId) {
    private static final long MAX_SAFE_INTEGER = 9_007_199_254_740_991L;

    public WorldMachineFeature {
        QuestContractValidation.requireOpaqueId(id, "id");
        if (id.length() > 64) {
            throw new IllegalArgumentException("id is outside its bounds.");
        }
        QuestContractValidation.requireOpaqueId(unit, "unit");
        if (unit.length() > 32) {
            throw new IllegalArgumentException("unit is outside its bounds.");
        }
        if (value < 0 || value > MAX_SAFE_INTEGER) {
            throw new IllegalArgumentException("value is outside the safe range.");
        }
        if (capacity != null && (capacity < 0 || capacity > MAX_SAFE_INTEGER
            || capacity < value)) {
            throw new IllegalArgumentException("capacity is outside the safe range.");
        }
        if (resourceId != null) {
            QuestContractValidation.requireResourceLocation(resourceId, "resourceId");
        }
    }

}
