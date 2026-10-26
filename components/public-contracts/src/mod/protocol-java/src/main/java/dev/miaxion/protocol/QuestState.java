package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum QuestState {
    @JsonProperty("locked") LOCKED,
    @JsonProperty("available") AVAILABLE,
    @JsonProperty("active") ACTIVE,
    @JsonProperty("completed") COMPLETED
}
