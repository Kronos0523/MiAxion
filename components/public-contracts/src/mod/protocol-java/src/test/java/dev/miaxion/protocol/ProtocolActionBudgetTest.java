package dev.miaxion.protocol;

import static org.junit.jupiter.api.Assertions.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Path;
import java.util.HashSet;
import org.junit.jupiter.api.Test;

final class ProtocolActionBudgetTest {
    @Test void policyMatchesTheSharedCrossLanguageFixtureAndKeepsControlDeadlines() throws Exception {
        var root = new ObjectMapper().readTree(Path.of("../../../tests/fixtures/bridge/action-budget-policy.json").toFile());
        var queries = new HashSet<String>(); root.get("queryActions").forEach(n -> queries.add(n.asText()));
        var recipes = new HashSet<String>(); root.get("recipeActions").forEach(n -> recipes.add(n.asText()));
        var limits = ProtocolRuntimeLimitsLoader.load();
        assertEquals(root.get("capability").asText(), ProtocolActionBudget.CAPABILITY);
        assertEquals(root.get("version").asText(), ProtocolActionBudget.VERSION);
        for (var action : KnownProtocolAction.values()) {
            assertEquals(queries.contains(action.wireValue()), ProtocolActionBudget.isQuery(action), action.name());
            assertEquals(recipes.contains(action.wireValue()), ProtocolActionBudget.isRecipe(action), action.name());
            assertEquals(queries.contains(action.wireValue()) ? limits.queryDeadlineMaximumMs() :
                ProtocolActionBudget.defaultDeadlineMs(action, limits), ProtocolActionBudget.maximumDeadlineMs(action, limits));
        }
        assertEquals(10000, ProtocolActionBudget.maximumDeadlineMs(KnownProtocolAction.ACTIONS_EXECUTE, limits));
    }
}
