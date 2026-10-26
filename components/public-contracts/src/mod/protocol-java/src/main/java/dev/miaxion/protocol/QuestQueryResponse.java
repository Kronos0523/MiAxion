package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record QuestQueryResponse(
        @JsonProperty(required = true) List<QuestQueryEntry> quests,
        @JsonProperty(required = true) int matchedCount,
        @JsonProperty(required = true) boolean truncated,
        String nextContinuation) {

    public QuestQueryResponse(List<QuestQueryEntry> quests, int matchedCount, boolean truncated) {
        this(quests, matchedCount, truncated, null);
    }

    public QuestQueryResponse {
        QuestContractValidation.requireContinuation(nextContinuation);
        quests = QuestContractValidation.copyWithinBounds(
            quests, ProtocolConstants.QUEST_QUERY_RESULT_MAX, "quests");
        if (matchedCount < quests.size()) {
            throw new IllegalArgumentException("matchedCount is too small.");
        }
        if (truncated != (matchedCount > quests.size())) {
            throw new IllegalArgumentException(
                "truncated must exactly describe omitted query matches.");
        }
        if (nextContinuation != null && (!truncated || quests.size() != ProtocolConstants.QUEST_QUERY_RESULT_MAX)) {
            throw new IllegalArgumentException("A continuation requires a full non-final page.");
        }
    }
}
