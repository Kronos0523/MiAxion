package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

public record QuestQueryEntry(
        @JsonProperty(required = true) String questId,
        @JsonProperty(required = true) String chapterTitle,
        @JsonProperty(required = true) String title,
        @JsonProperty(required = true) QuestState state,
        @JsonProperty(required = true) double progress,
        @JsonProperty(required = true) int taskCount,
        @JsonProperty(required = true) int completedTaskCount,
        @JsonProperty(required = true) boolean repeatable) {

    public QuestQueryEntry {
        QuestContractValidation.requireOpaqueId(questId, "questId");
        QuestContractValidation.requireTitle(chapterTitle, "chapterTitle");
        QuestContractValidation.requireTitle(title, "title");
        Objects.requireNonNull(state, "state");
        QuestContractValidation.requireProgress(progress, "progress");
        if (taskCount < 0
            || taskCount > ProtocolConstants.QUEST_DETAIL_COLLECTION_MAX) {
            throw new IllegalArgumentException("taskCount is out of range.");
        }
        if (completedTaskCount < 0 || completedTaskCount > taskCount) {
            throw new IllegalArgumentException(
                "completedTaskCount is out of range.");
        }
    }
}
