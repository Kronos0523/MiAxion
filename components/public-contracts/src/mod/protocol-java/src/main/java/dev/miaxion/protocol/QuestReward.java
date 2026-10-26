package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

public record QuestReward(
        @JsonProperty(required = true) String rewardId,
        @JsonProperty(required = true) String typeId,
        @JsonProperty(required = true) String title,
        @JsonProperty(required = true) boolean teamReward,
        @JsonProperty(required = true) boolean autoClaim,
        @JsonProperty(required = true) QuestRewardClaimState claimState) {

    public QuestReward {
        QuestContractValidation.requireOpaqueId(rewardId, "rewardId");
        QuestContractValidation.requireResourceLocation(typeId, "typeId");
        QuestContractValidation.requireTitle(title, "title");
        Objects.requireNonNull(claimState, "claimState");
    }
}
