package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum QuestQueryScope {
    @JsonProperty("overview") OVERVIEW,
    @JsonProperty("active") ACTIVE,
    @JsonProperty("available") AVAILABLE,
    @JsonProperty("completed") COMPLETED,
    @JsonProperty("search") SEARCH
}
