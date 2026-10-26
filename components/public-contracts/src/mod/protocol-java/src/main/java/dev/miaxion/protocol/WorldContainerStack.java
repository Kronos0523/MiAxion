package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/** One non-empty, read-only slot observation from a world container. */
public record WorldContainerStack(
        @JsonProperty(required = true) int slot,
        @JsonProperty(required = true) String itemId,
        @JsonProperty(required = true) int count,
        @JsonInclude(JsonInclude.Include.NON_NULL) String componentIdentity) {
    public static final int MAX_SLOT = 255;
    public static final int MAX_COMPONENT_IDENTITY_SCALARS = 512;

    public WorldContainerStack {
        if (slot < 0 || slot > MAX_SLOT) {
            throw new IllegalArgumentException("slot is outside its bounds.");
        }
        QuestContractValidation.requireResourceLocation(itemId, "itemId");
        if (count < 1) {
            throw new IllegalArgumentException("count must be positive.");
        }
        if (componentIdentity != null) {
            if (componentIdentity.codePointCount(0, componentIdentity.length())
                > MAX_COMPONENT_IDENTITY_SCALARS
                || componentIdentity.codePoints().anyMatch(WorldContainerStack::isUnsafe)) {
                throw new IllegalArgumentException(
                    "componentIdentity is outside its bounds.");
            }
        }
    }

    private static boolean isUnsafe(int codePoint) {
        return codePoint < 0x20 || codePoint == 0x7f
            || (codePoint >= 0x202a && codePoint <= 0x202e)
            || (codePoint >= 0x2066 && codePoint <= 0x2069);
    }
}
