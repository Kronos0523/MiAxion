package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum WorldQueryAuthority {
    @JsonProperty("generator_located") GENERATOR_LOCATED,
    @JsonProperty("loaded_observed") LOADED_OBSERVED
}
