package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum WorldRegistryKind {
    @JsonProperty("structure") STRUCTURE,
    @JsonProperty("biome") BIOME
}
