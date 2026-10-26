package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record WorldStructureTargetsRequest(String structureId, String blockId, String pageCursor) {
    public WorldStructureTargetsRequest {
        if (structureId != null) {
            QuestContractValidation.requireResourceLocation(structureId, "structureId");
        }
        if (blockId != null) {
            QuestContractValidation.requireResourceLocation(blockId, "blockId");
        }
        WorldQueryValidation.requirePageCursor(pageCursor, true);
    }

    public WorldStructureTargetsRequest(String structureId, String blockId) {
        this(structureId, blockId, null);
    }

    public WorldStructureTargetsRequest() {
        this(null, null, null);
    }
}
