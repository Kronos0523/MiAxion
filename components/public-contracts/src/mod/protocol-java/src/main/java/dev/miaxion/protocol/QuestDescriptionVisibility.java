package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum QuestDescriptionVisibility {
    @JsonProperty("visible") VISIBLE,
    @JsonProperty("hidden_until_complete") HIDDEN_UNTIL_COMPLETE
}
