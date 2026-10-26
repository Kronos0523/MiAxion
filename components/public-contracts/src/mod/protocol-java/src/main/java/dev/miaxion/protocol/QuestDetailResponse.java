package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;

public record QuestDetailResponse(
        @JsonProperty(required = true) String questId,
        @JsonProperty(required = true) String chapterTitle,
        @JsonProperty(required = true) String title,
        @JsonProperty(required = true) String subtitle,
        @JsonProperty(required = true)
        QuestDescriptionVisibility descriptionVisibility,
        @JsonProperty(required = true) List<String> description,
        @JsonProperty(required = true) QuestState state,
        @JsonProperty(required = true) double progress,
        @JsonProperty(required = true) boolean repeatable,
        @JsonProperty(required = true) boolean optional,
        @JsonProperty(required = true) List<QuestObjective> objectives,
        @JsonProperty(required = true) List<QuestReward> rewards,
        @JsonProperty(required = true) List<QuestDependency> dependencies) {

    public QuestDetailResponse {
        QuestContractValidation.requireOpaqueId(questId, "questId");
        QuestContractValidation.requireTitle(chapterTitle, "chapterTitle");
        QuestContractValidation.requireTitle(title, "title");
        QuestContractValidation.requireTitle(subtitle, "subtitle");
        Objects.requireNonNull(descriptionVisibility, "descriptionVisibility");
        Objects.requireNonNull(state, "state");
        QuestContractValidation.requireProgress(progress, "progress");
        description = QuestContractValidation.copyDescription(
            description, "description");
        // Knowing the opaque quest ID must never reveal FTB text hidden from the player.
        if (descriptionVisibility
                == QuestDescriptionVisibility.HIDDEN_UNTIL_COMPLETE
            && !description.isEmpty()) {
            throw new IllegalArgumentException(
                "Hidden description text cannot be transported.");
        }
        objectives = QuestContractValidation.copyWithinBounds(
            objectives, ProtocolConstants.QUEST_DETAIL_COLLECTION_MAX,
            "objectives");
        rewards = QuestContractValidation.copyWithinBounds(
            rewards, ProtocolConstants.QUEST_DETAIL_COLLECTION_MAX,
            "rewards");
        dependencies = QuestContractValidation.copyWithinBounds(
            dependencies, ProtocolConstants.QUEST_DETAIL_COLLECTION_MAX,
            "dependencies");
    }
}
