package dev.miaxion.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

final class QuestContractTest {
    @Test
    void query_continuation_round_trips_and_rejects_non_opaque_values() throws Exception {
        var request = new QuestQueryRequest(QuestQueryScope.ACTIVE, null, "AQ_valid-cursor");
        assertEquals(request, ProtocolJson.read(ProtocolJson.write(request), QuestQueryRequest.class));
        var entries = IntStream.range(0, 32).mapToObj(i -> new QuestQueryEntry("q-" + i, "Chapter", "Task", QuestState.ACTIVE, 0, 1, 0, false)).toList();
        var response = new QuestQueryResponse(entries, 40, true, "AQ_next-cursor");
        assertEquals(response, ProtocolJson.read(ProtocolJson.write(response), QuestQueryResponse.class));
        assertThrows(IllegalArgumentException.class, () -> new QuestQueryRequest(QuestQueryScope.ACTIVE, null, "not a cursor"));
        assertThrows(IllegalArgumentException.class, () -> new QuestQueryResponse(List.of(), 0, false, "AQ_cursor"));
    }

    @Test
    void complete_visible_detail_round_trips_without_dropping_business_data()
        throws Exception
    {
        QuestDetailResponse detail = detail(
            List.of("Build a water wheel.", "Connect it with shafts."),
            List.of(new QuestDependency("parent-1", "Welcome")));

        QuestDetailResponse restored = ProtocolJson.read(
            ProtocolJson.write(detail), QuestDetailResponse.class);

        assertEquals(detail, restored);
        assertEquals(2, restored.description().size());
        assertEquals(1, restored.objectives().size());
        assertEquals(1, restored.rewards().size());
    }

    @Test
    void hidden_description_cannot_transport_text() {
        assertThrows(IllegalArgumentException.class, () ->
            new QuestDetailResponse(
                "quest-1", "Chapter", "Title", "",
                QuestDescriptionVisibility.HIDDEN_UNTIL_COMPLETE,
                List.of("secret"), QuestState.AVAILABLE, 0,
                false, false, List.of(), List.of(), List.of()));
    }

    @Test
    void detail_rejects_oversize_collections_and_scalar_lines() {
        List<QuestDependency> tooMany = IntStream.rangeClosed(
                0, ProtocolConstants.QUEST_DETAIL_COLLECTION_MAX)
            .mapToObj(index -> new QuestDependency(
                "quest-" + index, "Title"))
            .toList();
        String line = "🙂".repeat(
            ProtocolConstants.QUEST_DESCRIPTION_LINE_SCALARS_MAX + 1);
        QuestObjective objective = new QuestObjective(
            "task-1", "ftbquests:item", "Craft a shaft",
            1, 1, "1 / 1", true, false);
        QuestReward reward = new QuestReward(
            "reward-1", "ftbquests:item", "8 Andesite Alloy",
            false, false, QuestRewardClaimState.CLAIMABLE);

        assertThrows(IllegalArgumentException.class, () ->
            detail(List.of("ok"), List.of(), List.of(), tooMany));
        assertThrows(IllegalArgumentException.class, () ->
            detail(List.of("ok"),
                java.util.Collections.nCopies(
                    ProtocolConstants.QUEST_DETAIL_COLLECTION_MAX + 1,
                    objective),
                List.of(), List.of()));
        assertThrows(IllegalArgumentException.class, () ->
            detail(List.of("ok"), List.of(),
                java.util.Collections.nCopies(
                    ProtocolConstants.QUEST_DETAIL_COLLECTION_MAX + 1,
                    reward),
                List.of()));
        assertThrows(IllegalArgumentException.class, () ->
            detail(List.of(line), List.of(), List.of(), List.of()));
    }

    @Test
    void query_requires_search_filter_and_exact_truncation_accounting() {
        assertThrows(NullPointerException.class, () ->
            new QuestQueryRequest(QuestQueryScope.SEARCH));
        assertThrows(IllegalArgumentException.class, () ->
            new QuestQueryRequest(QuestQueryScope.ACTIVE, "ignored"));
        assertThrows(IllegalArgumentException.class, () ->
            new QuestQueryResponse(List.of(), 1, false));
    }

    @Test
    void auto_claim_mode_remains_independent_from_current_claim_state() {
        QuestReward reward = new QuestReward(
            "reward-1", "ftbquests:item", "Reward",
            false, true, QuestRewardClaimState.CLAIMED);

        assertTrue(reward.autoClaim());
        assertEquals(QuestRewardClaimState.CLAIMED, reward.claimState());
    }

    private static QuestDetailResponse detail(
        List<String> description,
        List<QuestDependency> dependencies
    ) {
        return detail(
            description,
            List.of(new QuestObjective(
                "task-1", "ftbquests:item", "Craft a shaft",
                1, 1, "1 / 1", true, false)),
            List.of(new QuestReward(
                "reward-1", "ftbquests:item", "8 Andesite Alloy",
                false, false, QuestRewardClaimState.CLAIMABLE)),
            dependencies);
    }

    private static QuestDetailResponse detail(
        List<String> description,
        List<QuestObjective> objectives,
        List<QuestReward> rewards,
        List<QuestDependency> dependencies
    ) {
        return new QuestDetailResponse(
            "quest-1", "Getting Started", "Make Power", "Create basics",
            QuestDescriptionVisibility.VISIBLE, description,
            QuestState.ACTIVE, 0.5, false, false,
            objectives,
            rewards,
            dependencies);
    }
}
