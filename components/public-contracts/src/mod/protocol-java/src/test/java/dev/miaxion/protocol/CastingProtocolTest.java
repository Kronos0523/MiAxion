package dev.miaxion.protocol;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import static org.junit.jupiter.api.Assertions.*;

final class CastingProtocolTest {
    private static final UUID ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final OffsetDateTime NOW = OffsetDateTime.parse("2026-10-02T00:00:00Z");
    private static final KeybindingPayloads.Id KEY = new KeybindingPayloads.Id("minecraft", "key.togglePerspective", "neoforge-1.21.1");
    private static final KeybindingPayloads.Context CONTEXT = new KeybindingPayloads.Context("universal", "native-owner");
    private static final GameActionPayloads.Scope SCOPE = new GameActionPayloads.Scope("/instances/test", 1, 1, ID, "casting", ID, 1, 1, 1);
    private record Contract(KnownProtocolAction action, Object request, Object response) {}

    static Stream<Contract> contracts() {
        var receipt = new KeybindingPayloads.Receipt(ID, "/instances/test", "revision-2",
            List.of(new KeybindingPayloads.Change(KEY, new KeybindingPayloads.Bound("keysym", 74, "none"), new KeybindingPayloads.Unbound())),
            List.of(), "applied", NOW);
        return Stream.of(
            new Contract(KnownProtocolAction.KEYBINDINGS_CATALOG, new KeybindingPayloads.CatalogRequest(null, null),
                new KeybindingPayloads.CatalogResponse("/instances/test", "revision-1", List.of(
                    new KeybindingPayloads.CatalogEntry("key.togglePerspective", KEY, "Perspective", new KeybindingPayloads.Unbound(), CONTEXT, true, null),
                    new KeybindingPayloads.CatalogEntry("unknown.action", null, "Unknown", null, null, false, "owner_unknown")), "next_page")),
            new Contract(KnownProtocolAction.KEYBINDINGS_PREVIEW,
                new KeybindingPayloads.PreviewRequest("/instances/test", "revision-1", ID, 1,
                    List.of(new KeybindingPayloads.Selection(KEY, new KeybindingPayloads.Bound("mouse", 1, "shift"), CONTEXT))),
                new KeybindingPayloads.PreviewResponse(ID, "/instances/test", "revision-1", List.of(
                    new KeybindingPayloads.PreviewEntry(KEY, new KeybindingPayloads.Unbound(), new KeybindingPayloads.Bound("mouse", 1, "shift"), "change", null)))),
            new Contract(KnownProtocolAction.KEYBINDINGS_APPLY, new KeybindingPayloads.ApplyRequest("/instances/test", "revision-1", ID), new KeybindingPayloads.ApplyResponse(receipt)),
            new Contract(KnownProtocolAction.KEYBINDINGS_ROLLBACK, new KeybindingPayloads.RollbackRequest("/instances/test", ID), new KeybindingPayloads.RollbackResponse(receipt)),
            new Contract(KnownProtocolAction.ACTIONS_CONTEXT, new GameActionPayloads.ContextRequest("casting", ID, 1, 2), new GameActionPayloads.ContextResponse(3)),
            new Contract(KnownProtocolAction.ACTIONS_CONTEXT, new GameActionPayloads.ContextRequest("conversation", null, 0, 0), new GameActionPayloads.ContextResponse(4)),
            new Contract(KnownProtocolAction.ACTIONS_CATALOG, new GameActionPayloads.CatalogRequest("revision-1", "next_page"),
                new GameActionPayloads.CatalogResponse(SCOPE, "revision-1", List.of(
                new GameActionPayloads.CatalogEntry(new GameActionPayloads.KeyAction(KEY), "Perspective", true, null),
                 new GameActionPayloads.CatalogEntry(new GameActionPayloads.ArsPage(3), "Page 3", false, "source_unavailable",
                     "key.categories.gameplay", "游戏操作"),
                    new GameActionPayloads.CatalogEntry(new GameActionPayloads.IronSpell("irons_spellbooks:fireball"), "Fireball", true, null)), null)),
            new Contract(KnownProtocolAction.ACTIONS_GRANT, new GameActionPayloads.GrantRequest(SCOPE, List.of(
                new GameActionPayloads.KeyAction(KEY), new GameActionPayloads.ArsPage(3), new GameActionPayloads.IronSpell("irons_spellbooks:fireball"))),
                new GameActionPayloads.GrantResponse(ID, SCOPE, NOW.plusMinutes(5))),
            new Contract(KnownProtocolAction.ACTIONS_REVOKE, new GameActionPayloads.RevokeRequest(ID, SCOPE), new GameActionPayloads.RevokeResponse(true)),
            new Contract(KnownProtocolAction.ACTIONS_EXECUTE, execute(), new GameActionPayloads.ExecutionResponse(ID, new GameActionPayloads.Outcome("dispatched", null))),
            new Contract(KnownProtocolAction.ACTIONS_STATUS, new GameActionPayloads.StatusRequest(ID, ID), new GameActionPayloads.ExecutionResponse(ID, new GameActionPayloads.Outcome("rejected", "expired")))
        );
    }

    @ParameterizedTest
    @MethodSource("contracts")
    void serialized_envelopes_bind_through_production_validator_and_response_schema(Contract contract) throws Exception {
        assertEquals(contract.action(), KnownProtocolAction.fromWire(contract.action().wireValue()).orElseThrow());
        var request = envelope(contract.action(), contract.request(), true);
        var known = validate(request, contract.action());
        assertEquals(contract.request(), known.payload());
        assertEquals("1.0", request.path("protocolVersion").asText());
        for (boolean isRequest : List.of(true, false)) {
            Object payload = isRequest ? contract.request() : contract.response();
            var tree = envelope(contract.action(), payload, isRequest);
            assertSchemaValid(tree, isRequest);
            assertEquals(payload, ProtocolJson.read(tree.path("payload").toString(), payload.getClass()));
            assertNestedClosed(tree.path("payload"), tree, isRequest, contract.action());
            if (!isRequest || !contract.action().wireValue().endsWith(".catalog")) {
                for (var fields = tree.path("payload").fieldNames(); fields.hasNext();) {
                    var missing = tree.deepCopy(); ((ObjectNode)missing.path("payload")).remove(fields.next());
                    assertInvalid(missing, isRequest, contract.action());
                }
            }
            ((ObjectNode)tree.path("payload")).put("extra", true);
            assertInvalid(tree, isRequest, contract.action());
            tree = envelope(contract.action(), payload, isRequest);
            tree.put("unexpectedEnvelopeField", true);
            assertInvalid(tree, isRequest, contract.action());
            tree = envelope(contract.action(), payload, isRequest);
            tree.put("action", "actions.future");
            assertTrue(KnownProtocolAction.fromWire("actions.future").isEmpty());
            if (isRequest) {
                assertSchemaValid(tree, true);
                var unknown = tree;
                assertThrows(ProtocolValidationFailure.class, () -> validate(unknown, contract.action()));
            } else assertInvalid(tree, false, contract.action());
            tree = envelope(contract.action(), payload, isRequest);
            tree.putNull("payload");
            assertInvalid(tree, isRequest, contract.action());
        }
        request.putNull("sessionToken");
        assertInvalid(request, true, contract.action());
    }

    @Test void rejects_nested_unknown_malformed_and_unbounded_contracts() throws Exception {
        var request = envelope(KnownProtocolAction.ACTIONS_EXECUTE, execute(), true);
        for (String target : List.of(
            "{\"kind\":\"arsPage\",\"pageOneBased\":0}",
            "{\"kind\":\"arsPage\",\"pageOneBased\":3,\"hand\":\"main\"}",
            "{\"kind\":\"ironSpell\",\"namespacedSpellId\":\"bad id\"}",
            "{\"kind\":\"ironSpell\",\"namespacedSpellId\":\"mod:spell\",\"globalIndex\":1}",
            "{\"kind\":\"macro\",\"steps\":[]}")) {
            var invalid = request.deepCopy(); ((ObjectNode)invalid.path("payload")).set("target", ProtocolJson.tree(target));
            assertInvalid(invalid, true, KnownProtocolAction.ACTIONS_EXECUTE);
        }
        for (var fields = request.path("payload").path("scope").fieldNames(); fields.hasNext();) {
            var invalid = request.deepCopy(); ((ObjectNode)invalid.path("payload").path("scope")).remove(fields.next());
            assertInvalid(invalid, true, KnownProtocolAction.ACTIONS_EXECUTE);
        }
        for (String revision : List.of("-1", "9007199254740992", "\"1\"", "null")) {
            var invalid = request.deepCopy(); ((ObjectNode)invalid.path("payload").path("scope")).set("grantRevision", ProtocolJson.tree(revision));
            assertInvalid(invalid, true, KnownProtocolAction.ACTIONS_EXECUTE);
        }
        var preview = envelope(KnownProtocolAction.KEYBINDINGS_PREVIEW, new KeybindingPayloads.PreviewRequest("/instances/test", "r", ID, 1,
            List.of(new KeybindingPayloads.Selection(KEY, new KeybindingPayloads.Unbound(), CONTEXT))), true);
        for (String value : List.of("{\"kind\":\"unbound\",\"code\":0}",
            "{\"kind\":\"bound\",\"inputType\":\"keysym\",\"code\":-1,\"modifier\":\"none\"}",
            "{\"kind\":\"bound\",\"inputType\":\"mouse\",\"code\":1,\"modifier\":\"super\"}",
            "{\"kind\":\"bound\",\"inputType\":\"keysym\",\"code\":74,\"modifier\":\"none\",\"nativeConflictContext\":\"overwrite\"}")) {
            ((ObjectNode)preview.path("payload").path("bindings").get(0)).set("value", ProtocolJson.tree(value));
            assertInvalid(preview, true, KnownProtocolAction.KEYBINDINGS_PREVIEW);
        }
        assertInvalid(envelope(KnownProtocolAction.ACTIONS_CATALOG, new GameActionPayloads.CatalogRequest(null, "cursor"), true), true, KnownProtocolAction.ACTIONS_CATALOG);
        assertInvalid(envelope(KnownProtocolAction.KEYBINDINGS_CATALOG, new KeybindingPayloads.CatalogRequest("r", "a".repeat(257)), true), true, KnownProtocolAction.KEYBINDINGS_CATALOG);
        for (List<GameActionPayloads.Target> targets : List.of(List.<GameActionPayloads.Target>of(),
            List.<GameActionPayloads.Target>of(new GameActionPayloads.ArsPage(1), new GameActionPayloads.ArsPage(1)),
            IntStream.rangeClosed(1, 129).mapToObj(n -> (GameActionPayloads.Target)new GameActionPayloads.ArsPage(n)).toList()))
            assertInvalid(envelope(KnownProtocolAction.ACTIONS_GRANT, new GameActionPayloads.GrantRequest(SCOPE, targets), true), true, KnownProtocolAction.ACTIONS_GRANT);
    }

    @Test void bridge_candidate_and_existing_targets_keep_their_exact_identity() throws Exception {
        List<GameActionPayloads.Target> targets = List.of(
            new GameActionPayloads.KeyAction(new KeybindingPayloads.Id("miaxion_bridge", "key.ftbquests.quests", "minecraft-1.21.1-neoforge")),
            new GameActionPayloads.KeyAction(new KeybindingPayloads.Id("minecraft", "key.togglePerspective", "minecraft-1.21.1-neoforge")),
            new GameActionPayloads.KeyAction(new KeybindingPayloads.Id("minecraft", "key.smoothCamera", "minecraft-1.21.1-neoforge")),
            new GameActionPayloads.ArsPage(3), new GameActionPayloads.IronSpell("irons_spellbooks:fireball"));
        for (var target : targets) {
            var request = new GameActionPayloads.ExecuteRequest(ID, SCOPE, ID, ID, target, NOW);
            var tree = envelope(KnownProtocolAction.ACTIONS_EXECUTE, request, true);
            assertSchemaValid(tree, true);
            assertEquals(request, validate(tree, KnownProtocolAction.ACTIONS_EXECUTE).payload());
            var catalog = new GameActionPayloads.CatalogResponse(SCOPE, "r1", List.of(
                new GameActionPayloads.CatalogEntry(target, "Candidate", false, "m1_unverified")), null);
            var response = envelope(KnownProtocolAction.ACTIONS_CATALOG, catalog, false);
            assertSchemaValid(response, false);
            assertEquals(catalog, ProtocolJson.read(response.path("payload").toString(), GameActionPayloads.CatalogResponse.class));
        }
    }

    @Test void pending_and_observed_status_preserve_execution_identity_and_rejection_reason() throws Exception {
        for (var contract : List.of(
            new Contract(KnownProtocolAction.ACTIONS_EXECUTE, execute(), new GameActionPayloads.ExecutionResponse(ID, new GameActionPayloads.Outcome("unknown", null))),
            new Contract(KnownProtocolAction.ACTIONS_STATUS, new GameActionPayloads.StatusRequest(ID, ID), new GameActionPayloads.ExecutionResponse(ID, new GameActionPayloads.Outcome("dispatched", null))),
            new Contract(KnownProtocolAction.ACTIONS_STATUS, new GameActionPayloads.StatusRequest(ID, ID), new GameActionPayloads.ExecutionResponse(ID, new GameActionPayloads.Outcome("rejected", "target_changed"))))) {
            assertEquals(contract.request(), validate(envelope(contract.action(), contract.request(), true), contract.action()).payload());
            var response = envelope(contract.action(), contract.response(), false);
            assertSchemaValid(response, false);
            assertEquals(contract.response(), ProtocolJson.read(response.path("payload").toString(), GameActionPayloads.ExecutionResponse.class));
        }
    }

    @Test void outcomes_are_observations_and_errors_remain_closed() throws Exception {
        for (String state : List.of("accepted", "dispatched", "started", "unknown"))
            assertSchemaValid(envelope(KnownProtocolAction.ACTIONS_STATUS, new GameActionPayloads.ExecutionResponse(ID, new GameActionPayloads.Outcome(state, null)), false), false);
        for (var outcome : List.of(new GameActionPayloads.Outcome("success", null), new GameActionPayloads.Outcome("rejected", null), new GameActionPayloads.Outcome("dispatched", "success")))
            assertInvalid(envelope(KnownProtocolAction.ACTIONS_STATUS, new GameActionPayloads.ExecutionResponse(ID, outcome), false), false, KnownProtocolAction.ACTIONS_STATUS);
        var error = envelope(KnownProtocolAction.ACTIONS_EXECUTE, new GameActionPayloads.ExecutionResponse(ID, new GameActionPayloads.Outcome("unknown", null)), false);
        error.putNull("payload");
        error.set("error", ProtocolJson.tree("{\"code\":\"UNAUTHORIZED\",\"message\":\"No action grant\",\"retryable\":false,\"details\":null}"));
        assertSchemaValid(error, false);
    }

    @Test void explicit_conversation_scope_roundtrips_but_other_modes_remain_closed() throws Exception {
        var request = envelope(KnownProtocolAction.ACTIONS_EXECUTE, execute(), true);
        ((ObjectNode) request.path("payload").path("scope")).put("mode", "conversation");
        var parsed = (GameActionPayloads.ExecuteRequest) validate(request, KnownProtocolAction.ACTIONS_EXECUTE).payload();
        assertEquals("conversation", parsed.scope().mode());
        var response = envelope(KnownProtocolAction.ACTIONS_CATALOG,
            new GameActionPayloads.CatalogResponse(parsed.scope(), "r", List.of(), null), false);
        assertSchemaValid(response, false);
        for (var mode : List.of("game", "knowledge", "unknown")) {
            ((ObjectNode) request.path("payload").path("scope")).put("mode", mode);
            assertInvalid(request, true, KnownProtocolAction.ACTIONS_EXECUTE);
        }
    }

    private static GameActionPayloads.ExecuteRequest execute() {
        return new GameActionPayloads.ExecuteRequest(ID, SCOPE, ID, ID, new GameActionPayloads.ArsPage(3), NOW);
    }
    private static ObjectNode envelope(KnownProtocolAction action, Object payload, boolean request) throws Exception {
        var tree = ProtocolJson.tree(ProtocolJson.write(payload));
        byte[] bytes = request ? ProtocolJson.writeBytes(new RequestEnvelope("1.0", ID, NOW, NOW.plusSeconds(5), action.action(), "A".repeat(43), tree, null))
            : new ProtocolValidator().serialize(new ResponseEnvelope("1.0", ID, NOW, NOW.plusSeconds(5), action.action(), null, tree, null));
        return (ObjectNode)ProtocolJson.tree(new String(bytes, StandardCharsets.UTF_8));
    }
    private static KnownRequest validate(JsonNode request, KnownProtocolAction action) throws Exception {
        var validator = new ProtocolValidator();
        return validator.validateKnown(validator.parseCommon(ProtocolJson.writeBytes(request), new ReceiveStamp(NOW.toInstant(), 0)), action);
    }
    private static void assertSchemaValid(JsonNode tree, boolean request) {
        var errors = ProtocolCorpusTest.validationMessages(request ? "request.schema.json" : "response.schema.json", tree.toString());
        assertTrue(errors.isEmpty(), errors.toString());
    }
    private static void assertNestedClosed(JsonNode node, JsonNode envelope, boolean request, KnownProtocolAction action) {
        if (node instanceof ObjectNode object) {
            object.put("injected", true);
            assertInvalid(envelope, request, action);
            object.remove("injected");
        }
        for (JsonNode child : node) assertNestedClosed(child, envelope, request, action);
    }
    private static void assertInvalid(JsonNode tree, boolean request, KnownProtocolAction action) {
        assertFalse(ProtocolCorpusTest.validationMessages(request ? "request.schema.json" : "response.schema.json", tree.toString()).isEmpty(), tree.toString());
        if (request) assertThrows(ProtocolValidationFailure.class, () -> validate(tree, action));
    }
}
