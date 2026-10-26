package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record QuestQueryRequest(
        @JsonProperty(required = true) QuestQueryScope scope,
        String filter,
        String continuation) {

    public QuestQueryRequest(QuestQueryScope scope, String filter) {
        this(scope, filter, null);
    }

    public QuestQueryRequest(QuestQueryScope scope) {
        this(scope, null);
    }

    public QuestQueryRequest {
        Objects.requireNonNull(scope, "scope");
        QuestContractValidation.requireContinuation(continuation);
        if (scope == QuestQueryScope.SEARCH) {
            QuestContractValidation.requireText(filter, 1, 256, "filter");
        } else if (filter != null) {
            throw new IllegalArgumentException(
                "Only search scope accepts a filter.");
        }
    }
}
