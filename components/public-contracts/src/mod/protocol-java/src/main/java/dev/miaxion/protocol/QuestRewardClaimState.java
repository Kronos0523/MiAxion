package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum QuestRewardClaimState {
    @JsonProperty("claimed") CLAIMED,
    @JsonProperty("claimable") CLAIMABLE,
    @JsonProperty("blocked") BLOCKED
}
