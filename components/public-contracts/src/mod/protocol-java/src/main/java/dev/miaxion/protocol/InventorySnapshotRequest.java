package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;

public record InventorySnapshotRequest(boolean includeEmpty) {

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static InventorySnapshotRequest fromJson(
        Map<String, JsonNode> properties
    ) {
        if (!properties.keySet().stream()
            .allMatch("includeEmpty"::equals)) {
            throw new IllegalArgumentException(
                "Unknown inventory snapshot request property.");
        }

        JsonNode value = properties.get("includeEmpty");
        if (value == null) {
            return new InventorySnapshotRequest(false);
        }
        if (!value.isBoolean()) {
            throw new IllegalArgumentException(
                "includeEmpty must be a boolean.");
        }

        return new InventorySnapshotRequest(value.booleanValue());
    }
}
