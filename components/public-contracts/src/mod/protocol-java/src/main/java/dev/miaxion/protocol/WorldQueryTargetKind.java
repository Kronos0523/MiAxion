package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum WorldQueryTargetKind {
    @JsonProperty("metal_storage_block") METAL_STORAGE_BLOCK,
    @JsonProperty("loot_container") LOOT_CONTAINER,
    @JsonProperty("user_block") USER_BLOCK
}
