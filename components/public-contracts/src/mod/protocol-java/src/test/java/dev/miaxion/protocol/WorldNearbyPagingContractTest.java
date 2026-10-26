package dev.miaxion.protocol;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import org.junit.jupiter.api.Test;

final class WorldNearbyPagingContractTest {
    private static final String CURSOR = "0123456789abcdef0123456789abcdef";

    @Test
    void cursor_and_summary_are_consistent_for_both_result_types() {
        var summary = summary(false, true);
        var machines = new WorldMachinesResponse(summary, WorldChunkRange.THREE_BY_THREE,
            WorldVerticalRange.TEN, List.of(), null, CURSOR);
        var containers = new WorldContainersResponse(summary, WorldChunkRange.THREE_BY_THREE,
            WorldVerticalRange.TEN, List.of(), null, CURSOR);
        assertEquals(CURSOR, machines.nextCursor());
        assertEquals(CURSOR, containers.nextCursor());
        assertThrows(IllegalArgumentException.class, () -> new WorldMachinesResponse(summary(true, true),
            WorldChunkRange.THREE_BY_THREE, WorldVerticalRange.TEN, List.of(), null, CURSOR));
        assertThrows(IllegalArgumentException.class, () -> new WorldContainersResponse(summary(false, false),
            WorldChunkRange.THREE_BY_THREE, WorldVerticalRange.TEN, List.of(), null, CURSOR));
    }

    @Test
    void arbitrary_cursor_text_is_not_accepted() {
        for (String cursor : List.of("fixture-position", CURSOR.toUpperCase(java.util.Locale.ROOT), CURSOR + "0"))
            assertThrows(IllegalArgumentException.class, () -> new WorldNearbyScanRequest(
                WorldChunkRange.THREE_BY_THREE, WorldVerticalRange.TEN, null, cursor));
        assertThrows(IllegalArgumentException.class, () -> new WorldMachinesResponse(summary(false, true),
            WorldChunkRange.THREE_BY_THREE, WorldVerticalRange.TEN, List.of(), null, ""));
    }

    private static WorldQuerySummary summary(boolean complete, boolean truncated) {
        return new WorldQuerySummary(WorldQueryAuthority.LOADED_OBSERVED, "minecraft:overworld", 0,
            "q1", 1, 1, 129, 0, complete, truncated, List.of());
    }
}
