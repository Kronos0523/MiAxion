package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;

public record QuestDetailRequest(
        @JsonProperty(required = true) String questId) {

    public QuestDetailRequest {
        QuestContractValidation.requireOpaqueId(questId, "questId");
    }
}
