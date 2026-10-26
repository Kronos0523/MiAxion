package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;

public record InventorySlot(
        @JsonProperty(required = true) int slot,
        @JsonProperty(required = true) boolean empty,
        String itemId,
        Integer count) {

    public InventorySlot {
        if (empty && (itemId != null || count != null)) {
            throw new IllegalArgumentException(
                "Empty inventory slots cannot contain item fields.");
        }
        if (!empty && (itemId == null || count == null)) {
            throw new IllegalArgumentException(
                "Non-empty inventory slots require item fields.");
        }
    }
}
