package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;

public record QuestDependency(
        @JsonProperty(required = true) String questId,
        @JsonProperty(required = true) String title) {

    public QuestDependency {
        QuestContractValidation.requireOpaqueId(questId, "questId");
        QuestContractValidation.requireTitle(title, "title");
    }
}
