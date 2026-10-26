package dev.miaxion.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

final class ProtocolJsonTest {
    private static final ObjectMapper TREE_MAPPER = new ObjectMapper();
    private static final UUID REQUEST_ID =
        UUID.fromString("b8b88ff4-36c0-4bdc-a5c1-8a976dc7d2cf");
    private static final OffsetDateTime TIMESTAMP =
        OffsetDateTime.parse("2026-07-26T05:30:00Z");
    private static final OffsetDateTime DEADLINE =
        OffsetDateTime.parse("2026-07-26T05:30:03Z");
    private static final String TOKEN =
        "QmFzZTY0dXJsLXNlc3Npb24tdG9rZW4tdjEtMzItYnl0ZXM";

    static Stream<String> forbiddenRequests() {
        return Stream.of(
            requestJson(
                "\"requestId\":\"" + REQUEST_ID + "\",",
                "\"action\":\"health.ping\",",
                "\"unexpected\":true,"),
            requestJson(
                "",
                "\"action\":\"health.ping\",",
                ""),
            requestJson(
                "\"requestId\":null,",
                "\"action\":\"health.ping\",",
                ""),
            requestJson(
                "\"requestId\":\"" + REQUEST_ID + "\",",
                "\"Action\":\"health.ping\",",
                ""),
            requestJson(
                "\"requestId\":\"" + REQUEST_ID + "\",",
                "\"action\":\"missingdot\",",
                ""),
            requestJson(
                "\"requestId\":\"" + REQUEST_ID + "\",",
                "\"action\":\"future..snapshot\",",
                ""),
            requestJson(
                "\"requestId\":\"" + REQUEST_ID + "\",",
                "\"action\":\"HUD.SHOW\",",
                ""),
            requestJson(
                "\"requestId\":\"" + REQUEST_ID + "\",",
                "\"action\":\"HudShow\",",
                ""),
            requestJson(
                "\"requestId\":\"" + REQUEST_ID + "\",",
                "\"action\":10,",
                ""));
    }

    static Stream<Arguments> actionWireNames() {
        return Stream.of(
            Arguments.of(
                KnownProtocolAction.SESSION_PAIR_REQUEST.action(),
                "session.pair.request"),
            Arguments.of(
                KnownProtocolAction.SESSION_PAIR_APPROVED.action(),
                "session.pair.approved"),
            Arguments.of(
                KnownProtocolAction.SESSION_REVOKE.action(),
                "session.revoke"),
            Arguments.of(
                KnownProtocolAction.REQUEST_CANCEL.action(),
                "request.cancel"),
            Arguments.of(
                KnownProtocolAction.CAPABILITIES_LIST.action(),
                "capabilities.list"),
            Arguments.of(
                KnownProtocolAction.EXTENSIONS_LIST.action(),
                "extensions.list"),
            Arguments.of(
                KnownProtocolAction.EXTENSIONS_CALL.action(),
                "extensions.call"),
            Arguments.of(
                KnownProtocolAction.RECIPE_QUERY.action(),
                "recipe.query"),
            Arguments.of(
                KnownProtocolAction.RECIPE_USES.action(),
                "recipe.uses"),
            Arguments.of(
                KnownProtocolAction.RECIPE_QUERY_SOURCE.action(),
                "recipe.query.source"),
            Arguments.of(
                KnownProtocolAction.RECIPE_USES_SOURCE.action(),
                "recipe.uses.source"),
            Arguments.of(
                KnownProtocolAction.RECIPE_PROVIDERS_LIST.action(),
                "recipe.providers.list"),
            Arguments.of(
                KnownProtocolAction.QUESTS_QUERY.action(),
                "quests.query"),
            Arguments.of(
                KnownProtocolAction.QUESTS_DETAIL.action(),
                "quests.detail"),
            Arguments.of(
                KnownProtocolAction.INVENTORY_SNAPSHOT.action(),
                "inventory.snapshot"),
            Arguments.of(
                KnownProtocolAction.TERMINOLOGY_CONFIGURE.action(),
                "terminology.configure"),
            Arguments.of(
                KnownProtocolAction.TERMINOLOGY_SNAPSHOT.action(),
                "terminology.snapshot"),
            Arguments.of(
                KnownProtocolAction.TERMINOLOGY_CLEAR.action(),
                "terminology.clear"),
            Arguments.of(
                KnownProtocolAction.TERMINOLOGY_CHANGED.action(),
                "terminology.changed"),
            Arguments.of(
                KnownProtocolAction.PLAYER_SNAPSHOT.action(),
                "player.snapshot"),
            Arguments.of(
                KnownProtocolAction.ENTITY_RESOLVE.action(),
                "entities.resolve"),
            Arguments.of(
                KnownProtocolAction.ENTITY_DESCRIBE.action(),
                "entities.describe"),
            Arguments.of(
                KnownProtocolAction.MOD_VERSIONS_RESOLVE.action(),
                "mods.versions.resolve"),
            Arguments.of(
                KnownProtocolAction.WORLD_STRUCTURE_LOCATE.action(),
                "world.structure.locate"),
            Arguments.of(
                KnownProtocolAction.WORLD_BIOME_LOCATE.action(),
                "world.biome.locate"),
            Arguments.of(
                KnownProtocolAction.WORLD_MACHINES_SCAN.action(),
                "world.machines.scan"),
            Arguments.of(
                KnownProtocolAction.WORLD_CONTAINERS_SCAN.action(),
                "world.containers.scan"),
            Arguments.of(
                KnownProtocolAction.WORLD_STRUCTURE_TARGETS.action(),
                "world.structure.targets"),
            Arguments.of(
                KnownProtocolAction.WORLD_REGISTRY_SEARCH.action(),
                "world.registry.search"),
            Arguments.of(
                KnownProtocolAction.WORLD_REGISTRY_RESOLVE.action(),
                "world.registry.resolve"),
            Arguments.of(
                KnownProtocolAction.WORLD_REGISTRY_DESCRIBE.action(),
                "world.registry.describe"),
            Arguments.of(
                KnownProtocolAction.WORLD_QUERY_PROGRESS.action(),
                "world.query.progress"),
            Arguments.of(
                KnownProtocolAction.HUD_SHOW.action(),
                "hud.show"),
            Arguments.of(
                KnownProtocolAction.HEALTH_PING.action(),
                "health.ping"));
    }

    static Stream<Arguments> errorWireNames() {
        return Stream.of(
            Arguments.of(
                ProtocolErrorCode.INVALID_REQUEST,
                "INVALID_REQUEST"),
            Arguments.of(
                ProtocolErrorCode.UNAUTHORIZED,
                "UNAUTHORIZED"),
            Arguments.of(
                ProtocolErrorCode.UNSUPPORTED_VERSION,
                "UNSUPPORTED_VERSION"),
            Arguments.of(
                ProtocolErrorCode.CAPABILITY_UNAVAILABLE,
                "CAPABILITY_UNAVAILABLE"),
            Arguments.of(
                ProtocolErrorCode.GAME_NOT_READY,
                "GAME_NOT_READY"),
            Arguments.of(ProtocolErrorCode.NOT_FOUND, "NOT_FOUND"),
            Arguments.of(ProtocolErrorCode.TIMEOUT, "TIMEOUT"),
            Arguments.of(ProtocolErrorCode.RATE_LIMITED, "RATE_LIMITED"),
            Arguments.of(
                ProtocolErrorCode.DETAILS_HIDDEN,
                "DETAILS_HIDDEN"),
            Arguments.of(
                ProtocolErrorCode.RESULT_TOO_LARGE,
                "RESULT_TOO_LARGE"),
            Arguments.of(
                ProtocolErrorCode.INTERNAL_ERROR,
                "INTERNAL_ERROR"));
    }

    @ParameterizedTest
    @MethodSource("forbiddenRequests")
    void request_envelope_rejects_forbidden_json(String json) {
        assertThrows(
            JsonProcessingException.class,
            () -> ProtocolJson.read(json, RequestEnvelope.class));
    }

    @Test
    void opt_in_fluid_name_lookup_uses_the_existing_schema_without_changing_legacy_any() throws Exception {
        var request = TREE_MAPPER.readTree(java.nio.file.Files.readString(java.nio.file.Path.of("..", "..", "..").resolve("protocol/fixtures/valid/entities-resolve-kind-request.json")));
        ((com.fasterxml.jackson.databind.node.ObjectNode) request.get("payload")).put("kind", "fluid");
        assertTrue(ProtocolCorpusTest.validationMessages("request.schema.json", TREE_MAPPER.writeValueAsString(request)).isEmpty());
        var response = TREE_MAPPER.readTree(java.nio.file.Files.readString(java.nio.file.Path.of("..", "..", "..").resolve("protocol/fixtures/valid/entities-resolve-response.json")));
        ((com.fasterxml.jackson.databind.node.ObjectNode) response.get("payload").get("matches").get(0)).put("kind", "fluid");
        assertTrue(ProtocolCorpusTest.validationMessages("response.schema.json", TREE_MAPPER.writeValueAsString(response)).isEmpty());
    }

    @Test
    void request_envelope_accepts_and_preserves_unknown_well_formed_action()
        throws Exception
    {
        RequestEnvelope request = ProtocolJson.read(
            requestJson(
                "\"requestId\":\"" + REQUEST_ID + "\",",
                "\"action\":\"future.read.snapshot\",",
                ""),
            RequestEnvelope.class);

        assertEquals("future.read.snapshot", request.action().toString());
        assertEquals(
            TREE_MAPPER.readTree("{\"nonce\":\"fixed-nonce\"}"),
            request.payload());
    }

    @ParameterizedTest
    @MethodSource("forbiddenEnumValues")
    void enums_reject_unknown_wrong_case_alias_and_numeric_values(
        String json
    ) {
        assertThrows(
            JsonProcessingException.class,
            () -> ProtocolJson.read(json, HudSeverity.class));
    }

    static Stream<String> forbiddenEnumValues() {
        return Stream.of("\"notice\"", "\"INFO\"", "\"Info\"", "0");
    }

    @Test
    void payload_rejects_missing_required_component() {
        assertThrows(
            JsonProcessingException.class,
            () -> ProtocolJson.read("{}", HealthPingRequest.class));
    }

    @Test
    void primitive_payload_rejects_missing_required_component() {
        assertThrows(
            JsonProcessingException.class,
            () -> ProtocolJson.read("{}", HudShowResponse.class));
    }

    @Test
    void payload_rejects_null_for_non_nullable_component() {
        assertThrows(
            JsonProcessingException.class,
            () -> ProtocolJson.read(
                "{\"capabilities\":null}",
                CapabilitiesListResponse.class));
    }

    @Test
    void response_requires_explicit_session_token_member() {
        String json = """
            {
              "protocolVersion":"1.0",
              "requestId":"b8b88ff4-36c0-4bdc-a5c1-8a976dc7d2cf",
              "timestamp":"2026-07-26T05:30:00Z",
              "deadline":"2026-07-26T05:30:03Z",
              "action":"health.ping",
              "payload":{
                "nonce":"fixed-nonce",
                "gameReady":false,
                "protocolVersion":"1.0"
              },
              "error":null
            }
            """;

        assertThrows(
            JsonProcessingException.class,
            () -> ProtocolJson.read(json, ResponseEnvelope.class));
    }

    @Test
    void inventory_request_defaults_include_empty_to_false()
        throws Exception
    {
        InventorySnapshotRequest request = ProtocolJson.read(
            "{}",
            InventorySnapshotRequest.class);

        assertFalse(request.includeEmpty());
    }

    @Test
    void inventory_request_rejects_explicit_null_include_empty() {
        assertThrows(
            JsonProcessingException.class,
            () -> ProtocolJson.read(
                "{\"includeEmpty\":null}",
                InventorySnapshotRequest.class));
    }

    @Test
    void inventory_request_rejects_unknown_property() {
        assertThrows(
            JsonProcessingException.class,
            () -> ProtocolJson.read(
                "{\"unexpected\":true}",
                InventorySnapshotRequest.class));
    }

    @Test
    void response_serializes_explicit_null_session_token()
        throws Exception
    {
        JsonNode payload = TREE_MAPPER.readTree(
            """
            {
              "nonce":"fixed-nonce",
              "gameReady":false,
              "protocolVersion":"1.0"
            }
            """);
        ResponseEnvelope response = new ResponseEnvelope(
            ProtocolConstants.VERSION,
            REQUEST_ID,
            TIMESTAMP,
            DEADLINE,
            KnownProtocolAction.HEALTH_PING.action(),
            null,
            payload,
            null);

        JsonNode json = TREE_MAPPER.readTree(
            ProtocolJson.write(response));

        assertTrue(json.has("sessionToken"));
        assertTrue(json.get("sessionToken").isNull());
    }

    @ParameterizedTest
    @MethodSource("actionWireNames")
    void actions_use_exact_wire_names(
        ProtocolAction action,
        String wireName
    ) throws Exception {
        String json = ProtocolJson.write(action);

        assertEquals("\"" + wireName + "\"", json);
        assertEquals(
            action,
            ProtocolJson.read(json, ProtocolAction.class));
    }

    @Test
    void known_action_catalog_maps_exact_wire_values() {
        assertEquals(52, KnownProtocolAction.values().length);
        for (KnownProtocolAction known : KnownProtocolAction.values()) {
            assertEquals(
                known,
                known.action().known().orElseThrow());
            assertEquals(known.wireValue(), known.action().value());
        }
        assertTrue(
            new ProtocolAction("future.read.snapshot").known().isEmpty());
    }

    @Test
    void mod_version_contracts_are_bounded_and_round_trip() throws Exception {
        var request = new ModVersionsResolveRequest(List.of("create", "jei"));
        assertEquals(request, ProtocolJson.read(ProtocolJson.write(request), ModVersionsResolveRequest.class));
        assertThrows(IllegalArgumentException.class,
            () -> new ModVersionsResolveRequest(List.of("create", "create")));
        assertThrows(IllegalArgumentException.class,
            () -> new ModVersionsResolveRequest(List.of("example:create")));

        var response = new ModVersionsResolveResponse(Map.of("create", "0.8.0"));
        assertEquals(response, ProtocolJson.read(ProtocolJson.write(response), ModVersionsResolveResponse.class));
        assertTrue(new ModVersionsResolveResponse(Map.of()).versions().isEmpty());
    }

    @Test
    void mod_version_success_response_obeys_the_closed_response_schema() throws Exception {
        var success = new ResponseEnvelope(
            ProtocolConstants.VERSION,
            REQUEST_ID,
            TIMESTAMP,
            DEADLINE,
            KnownProtocolAction.MOD_VERSIONS_RESOLVE.action(),
            null,
            ProtocolJson.tree("{\"versions\":{\"create\":\"0.8.0\"}}"),
            null);
        String serialized = new String(ProtocolJson.writeBytes(success), java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(ProtocolCorpusTest.validationMessages("response.schema.json", serialized).isEmpty());

        var open = new ResponseEnvelope(
            ProtocolConstants.VERSION,
            REQUEST_ID,
            TIMESTAMP,
            DEADLINE,
            KnownProtocolAction.MOD_VERSIONS_RESOLVE.action(),
            null,
            ProtocolJson.tree("{\"versions\":{},\"allMods\":true}"),
            null);
        String openSerialized = new String(ProtocolJson.writeBytes(open), java.nio.charset.StandardCharsets.UTF_8);
        assertFalse(ProtocolCorpusTest.validationMessages("response.schema.json", openSerialized).isEmpty());
    }

    @Test
    void terminology_requests_and_changed_event_round_trip_without_becoming_a_response()
        throws Exception
    {
        var settings = new TerminologyPayloads.Settings(
            true, java.util.List.of("native_crosshair"), 64, 180, 1800);
        var snapshot = new TerminologyPayloads.Snapshot(
            2, 3, "en_us", settings, java.util.List.of(new TerminologyPayloads.Entry(
                new TerminologyPayloads.Identity("entity", "minecraft:pillager"), "Pillager",
                java.util.List.of("native_crosshair"), "2026-09-16T00:00:00Z")),
            java.util.List.of(), java.util.Map.of(), true);
        var event = new TerminologyPayloads.ChangedEnvelope(
            ProtocolConstants.VERSION, KnownProtocolAction.TERMINOLOGY_CHANGED.action(), snapshot);

        assertEquals(new TerminologyPayloads.ConfigureRequest(settings),
            ProtocolJson.read(ProtocolJson.write(new TerminologyPayloads.ConfigureRequest(settings)),
                TerminologyPayloads.ConfigureRequest.class));
        assertEquals(new TerminologyPayloads.SnapshotRequest(),
            ProtocolJson.read(ProtocolJson.write(new TerminologyPayloads.SnapshotRequest()),
                TerminologyPayloads.SnapshotRequest.class));
        assertEquals(new TerminologyPayloads.ClearRequest(),
            ProtocolJson.read(ProtocolJson.write(new TerminologyPayloads.ClearRequest()),
                TerminologyPayloads.ClearRequest.class));
        String serialized = ProtocolJson.write(event);
        assertEquals(event, ProtocolJson.read(serialized, TerminologyPayloads.ChangedEnvelope.class));
        assertTrue(ProtocolJson.tree(serialized).path("payload").path("truncated").asBoolean());
        assertEquals("entity", ProtocolJson.tree(serialized).path("payload").path("entries").get(0)
            .path("identity").path("kind").asText());
        assertTrue(ProtocolCorpusTest.validationMessages(
            "terminology-changed.schema.json", serialized).isEmpty());
        assertThrows(JsonProcessingException.class,
            () -> ProtocolJson.read(serialized, ResponseEnvelope.class));
    }

    @Test
    void terminology_changed_schema_accepts_legacy_and_all_typed_jei_identities()
        throws Exception
    {
        var settings = new TerminologyPayloads.Settings(
            true, java.util.List.of("item_browser"), 64, 180, 1800);
        var entries = java.util.List.of(
            new TerminologyPayloads.Entry(
                new TerminologyPayloads.Identity("item", "minecraft:stone"),
                "Stone", java.util.List.of("item_browser"), "2026-09-16T00:00:00Z"),
            new TerminologyPayloads.Entry(
                new TerminologyPayloads.Identity("item", "minecraft:dirt", "net.minecraft.world.item.ItemStack"),
                "Dirt", java.util.List.of("item_browser"), "2026-09-16T00:00:00Z"),
            new TerminologyPayloads.Entry(
                new TerminologyPayloads.Identity("fluid", "sauce:source_fluid", "net.neoforged.neoforge.fluids.FluidStack"),
                "Source", java.util.List.of("item_browser"), "2026-09-16T00:00:00Z"),
            new TerminologyPayloads.Entry(
                new TerminologyPayloads.Identity("chemical", "mekanism:source", "mekanism.api.chemical.gas.GasStack"),
                "Source Chemical", java.util.List.of("item_browser"), "2026-09-16T00:00:00Z"),
            new TerminologyPayloads.Entry(
                new TerminologyPayloads.Identity("energy", "forge_energy:fe", "net.neoforged.neoforge.energy.EnergyStorage"),
                "Forge Energy", java.util.List.of("item_browser"), "2026-09-16T00:00:00Z"),
            new TerminologyPayloads.Entry(
                new TerminologyPayloads.Identity("custom", "example:ingredient", "com.example.CustomIngredient"),
                "Custom Ingredient", java.util.List.of("item_browser"), "2026-09-16T00:00:00Z"));
        var event = new TerminologyPayloads.ChangedEnvelope(
            ProtocolConstants.VERSION, KnownProtocolAction.TERMINOLOGY_CHANGED.action(),
            new TerminologyPayloads.Snapshot(2, 4, "en_us", settings, entries,
                java.util.List.of(), java.util.Map.of(), false));

        String serialized = ProtocolJson.write(event);
        assertTrue(ProtocolCorpusTest.validationMessages(
            "terminology-changed.schema.json", serialized).isEmpty());
        assertEquals(serialized, new String(
            new ProtocolValidator().serializeTerminologyChanged(event),
            java.nio.charset.StandardCharsets.UTF_8));
        assertEquals(event, ProtocolJson.read(serialized,
            TerminologyPayloads.ChangedEnvelope.class));

        var invalidCustom = new TerminologyPayloads.ChangedEnvelope(
            ProtocolConstants.VERSION, KnownProtocolAction.TERMINOLOGY_CHANGED.action(),
            new TerminologyPayloads.Snapshot(2, 5, "en_us", settings,
                java.util.List.of(new TerminologyPayloads.Entry(
                    new TerminologyPayloads.Identity("custom", "example:ingredient"),
                    "Custom Ingredient", java.util.List.of("item_browser"),
                    "2026-09-16T00:00:00Z")), java.util.List.of(),
                java.util.Map.of(), false));
        assertFalse(ProtocolCorpusTest.validationMessages(
            "terminology-changed.schema.json", ProtocolJson.write(invalidCustom)).isEmpty());
        org.junit.jupiter.api.Assertions.assertThrows(ProtocolValidationFailure.class,
            () -> new ProtocolValidator().serializeTerminologyChanged(invalidCustom));

        var invalidTypeUid = new TerminologyPayloads.ChangedEnvelope(
            ProtocolConstants.VERSION, KnownProtocolAction.TERMINOLOGY_CHANGED.action(),
            new TerminologyPayloads.Snapshot(2, 6, "en_us", settings,
                java.util.List.of(new TerminologyPayloads.Entry(
                    new TerminologyPayloads.Identity("custom", "example:ingredient", "bad\u0001uid"),
                    "Custom Ingredient", java.util.List.of("item_browser"),
                    "2026-09-16T00:00:00Z")), java.util.List.of(),
                java.util.Map.of(), false));
        assertFalse(ProtocolCorpusTest.validationMessages(
            "terminology-changed.schema.json", ProtocolJson.write(invalidTypeUid)).isEmpty());
        org.junit.jupiter.api.Assertions.assertThrows(ProtocolValidationFailure.class,
            () -> new ProtocolValidator().serializeTerminologyChanged(invalidTypeUid));
    }

    @ParameterizedTest
    @MethodSource("errorWireNames")
    void error_codes_use_exact_wire_names(
        ProtocolErrorCode code,
        String wireName
    ) throws Exception {
        String json = ProtocolJson.write(code);

        assertEquals("\"" + wireName + "\"", json);
        assertEquals(
            code,
            ProtocolJson.read(json, ProtocolErrorCode.class));
    }

    @ParameterizedTest
    @MethodSource("forbiddenErrorCodeValues")
    void error_codes_reject_non_wire_values(String json) {
        assertThrows(
            JsonProcessingException.class,
            () -> ProtocolJson.read(json, ProtocolErrorCode.class));
    }

    static Stream<String> forbiddenErrorCodeValues() {
        return Stream.of(
            "\"invalid_request\"",
            "\"InvalidRequest\"",
            "0");
    }

    @Test
    void envelope_error_and_inventory_invariants_are_enforced()
        throws Exception
    {
        JsonNode payload = TREE_MAPPER.readTree(
            "{\"nonce\":\"fixed-nonce\"}");
        ProtocolError error = new ProtocolError(
            ProtocolErrorCode.INVALID_REQUEST,
            "request was invalid",
            false,
            null);

        assertThrows(
            NullPointerException.class,
            () -> new RequestEnvelope(
                null,
                REQUEST_ID,
                TIMESTAMP,
                DEADLINE,
                KnownProtocolAction.HEALTH_PING.action(),
                null,
                payload,
                null));
        assertThrows(
            NullPointerException.class,
            () -> new RequestEnvelope(
                ProtocolConstants.VERSION,
                REQUEST_ID,
                TIMESTAMP,
                DEADLINE,
                KnownProtocolAction.HEALTH_PING.action(),
                null,
                TREE_MAPPER.nullNode(),
                null));
        assertThrows(
            IllegalArgumentException.class,
            () -> new RequestEnvelope(
                ProtocolConstants.VERSION,
                REQUEST_ID,
                TIMESTAMP,
                DEADLINE,
                KnownProtocolAction.HEALTH_PING.action(),
                null,
                payload,
                TREE_MAPPER.createObjectNode()));
        assertThrows(
            IllegalArgumentException.class,
            () -> new RequestEnvelope(
                ProtocolConstants.VERSION,
                REQUEST_ID,
                TIMESTAMP,
                TIMESTAMP,
                KnownProtocolAction.HEALTH_PING.action(),
                TOKEN,
                payload,
                null));
        assertThrows(
            IllegalArgumentException.class,
            () -> new ResponseEnvelope(
                ProtocolConstants.VERSION,
                REQUEST_ID,
                TIMESTAMP,
                DEADLINE,
                KnownProtocolAction.HEALTH_PING.action(),
                TOKEN,
                payload,
                null));
        assertThrows(
            IllegalArgumentException.class,
            () -> new ResponseEnvelope(
                ProtocolConstants.VERSION,
                REQUEST_ID,
                TIMESTAMP,
                DEADLINE,
                KnownProtocolAction.HEALTH_PING.action(),
                null,
                null,
                null));
        assertThrows(
            IllegalArgumentException.class,
            () -> new ResponseEnvelope(
                ProtocolConstants.VERSION,
                REQUEST_ID,
                TIMESTAMP,
                DEADLINE,
                KnownProtocolAction.HEALTH_PING.action(),
                null,
                payload,
                error));
        assertThrows(
            NullPointerException.class,
            () -> new ProtocolError(
                null,
                "message",
                false,
                null));
        assertThrows(
            NullPointerException.class,
            () -> new ProtocolError(
                ProtocolErrorCode.INTERNAL_ERROR,
                null,
                false,
                null));
        assertThrows(
            IllegalArgumentException.class,
            () -> new InventorySlot(
                0,
                true,
                "minecraft:diamond",
                1));
        assertThrows(
            IllegalArgumentException.class,
            () -> new InventorySlot(
                0,
                false,
                null,
                null));
        assertThrows(
            IllegalArgumentException.class,
            () -> new WorldQueryProgressResponse(
                true, "nearby.scan", 1_048_577, 1_048_577, 1_048_577, 1_048_577));
    }

    private static String requestJson(
        String requestIdProperty,
        String actionProperty,
        String extraProperty
    ) {
        return "{"
            + "\"protocolVersion\":\"1.0\","
            + requestIdProperty
            + "\"timestamp\":\"2026-07-26T05:30:00Z\","
            + "\"deadline\":\"2026-07-26T05:30:03Z\","
            + actionProperty
            + "\"sessionToken\":"
            + "\"QmFzZTY0dXJsLXNlc3Npb24tdG9rZW4tdjEtMzItYnl0ZXM\","
            + extraProperty
            + "\"payload\":{\"nonce\":\"fixed-nonce\"},"
            + "\"error\":null"
            + "}";
    }
}
