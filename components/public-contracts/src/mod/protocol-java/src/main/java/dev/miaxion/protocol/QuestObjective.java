package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;

public record QuestObjective(
        @JsonProperty(required = true) String taskId,
        @JsonProperty(required = true) String typeId,
        @JsonProperty(required = true) String title,
        @JsonProperty(required = true) long currentProgress,
        @JsonProperty(required = true) long maximumProgress,
        @JsonProperty(required = true) String formattedProgress,
        @JsonProperty(required = true) boolean completed,
        @JsonProperty(required = true) boolean optional) {

    public QuestObjective {
        QuestContractValidation.requireOpaqueId(taskId, "taskId");
        QuestContractValidation.requireResourceLocation(typeId, "typeId");
        QuestContractValidation.requireTitle(title, "title");
        QuestContractValidation.requireTitle(
            formattedProgress, "formattedProgress");
        if (maximumProgress < 1
            || maximumProgress > ProtocolConstants.QUEST_PROGRESS_INTEGER_MAX) {
            throw new IllegalArgumentException(
                "maximumProgress is out of range.");
        }
        if (currentProgress < 0 || currentProgress > maximumProgress) {
            throw new IllegalArgumentException(
                "currentProgress is out of range.");
        }
    }
}
