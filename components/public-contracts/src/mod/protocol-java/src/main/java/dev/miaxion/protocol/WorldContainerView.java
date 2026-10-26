package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum WorldContainerView {
    @JsonProperty("full") FULL,
    @JsonProperty("automation_unsided") AUTOMATION_UNSIDED,
    @JsonProperty("automation_sided") AUTOMATION_SIDED,
    @JsonProperty("loot_ungenerated") LOOT_UNGENERATED,
    @JsonProperty("locked") LOCKED,
    @JsonProperty("unavailable") UNAVAILABLE
}
