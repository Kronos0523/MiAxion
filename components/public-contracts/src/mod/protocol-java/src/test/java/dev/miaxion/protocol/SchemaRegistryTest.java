package dev.miaxion.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

final class SchemaRegistryTest {
    @Test
    void mod_version_lookup_schema_accepts_only_a_bounded_exact_id_list() {
        var registry = SchemaRegistry.load();
        String request = new String(ProtocolValidatorTestFixture.validRequest(), java.nio.charset.StandardCharsets.UTF_8)
            .replace("health.ping", "mods.versions.resolve")
            .replace("\"nonce\":\"fixed-nonce\"", "\"modIds\":[\"create\",\"jei\"]");
        assertTrue(registry.validateKnown(
            ProtocolJson.validatorTree(request.getBytes(java.nio.charset.StandardCharsets.UTF_8)),
            KnownProtocolAction.MOD_VERSIONS_RESOLVE).isEmpty());

        String duplicate = request.replace("[\"create\",\"jei\"]", "[\"create\",\"create\"]");
        assertTrue(!registry.validateKnown(
            ProtocolJson.validatorTree(duplicate.getBytes(java.nio.charset.StandardCharsets.UTF_8)),
            KnownProtocolAction.MOD_VERSIONS_RESOLVE).isEmpty());
        String namespaced = request.replace("[\"create\",\"jei\"]", "[\"example:create\"]");
        assertTrue(!registry.validateKnown(
            ProtocolJson.validatorTree(namespaced.getBytes(java.nio.charset.StandardCharsets.UTF_8)),
            KnownProtocolAction.MOD_VERSIONS_RESOLVE).isEmpty());
        String open = request.replace("\"modIds\":[\"create\",\"jei\"]", "\"modIds\":[\"create\"],\"allMods\":true");
        assertTrue(!registry.validateKnown(
            ProtocolJson.validatorTree(open.getBytes(java.nio.charset.StandardCharsets.UTF_8)),
            KnownProtocolAction.MOD_VERSIONS_RESOLVE).isEmpty());
    }

    @Test
    void source_bound_recipe_request_requires_exact_addon_and_generation() {
        var registry = SchemaRegistry.load();
        String request = new String(ProtocolValidatorTestFixture.validRequest(), java.nio.charset.StandardCharsets.UTF_8)
            .replace("health.ping", "recipe.query.source")
            .replace("\"nonce\":\"fixed-nonce\"", "\"addonId\":\"miaxion_rei\",\"sourceGeneration\":2,\"itemId\":\"minecraft:stick\"");
        assertTrue(registry.validateKnown(ProtocolJson.validatorTree(request.getBytes(java.nio.charset.StandardCharsets.UTF_8)),
            KnownProtocolAction.RECIPE_QUERY_SOURCE).isEmpty());
        for (String invalid : new String[] {
            request.replace("\"sourceGeneration\":2,", ""),
            request.replace("\"sourceGeneration\":2", "\"sourceGeneration\":-1"),
            request.replace("miaxion_rei", "Bad ID"),
            request.replace("\"itemId\":\"minecraft:stick\"", "\"itemId\":\"minecraft:stick\",\"providerArgs\":{}")
        }) assertTrue(!registry.validateKnown(ProtocolJson.validatorTree(invalid.getBytes(java.nio.charset.StandardCharsets.UTF_8)),
            KnownProtocolAction.RECIPE_QUERY_SOURCE).isEmpty());
    }
    @Test
    void recipe_provider_directory_request_is_closed_and_session_bound() {
        var registry = SchemaRegistry.load();
        String request = new String(ProtocolValidatorTestFixture.validRequest(), java.nio.charset.StandardCharsets.UTF_8)
            .replace("health.ping", "recipe.providers.list")
            .replace("\"nonce\":\"fixed-nonce\"", "");
        var errors = registry.validateKnown(ProtocolJson.validatorTree(request.getBytes(java.nio.charset.StandardCharsets.UTF_8)),
            KnownProtocolAction.RECIPE_PROVIDERS_LIST);
        assertTrue(errors.isEmpty(), errors.toString());
        assertTrue(!registry.validateKnown(ProtocolJson.validatorTree(request.replace("\"payload\":{}", "\"payload\":{\"sourceId\":\"miaxion_rei\"}")
            .getBytes(java.nio.charset.StandardCharsets.UTF_8)), KnownProtocolAction.RECIPE_PROVIDERS_LIST).isEmpty());
    }

    @Test
    void quest_page_request_schema_accepts_only_the_closed_continuation_field() {
        var registry = SchemaRegistry.load();
        String request = new String(ProtocolValidatorTestFixture.validRequest(), java.nio.charset.StandardCharsets.UTF_8)
            .replace("health.ping", "quests.query")
            .replace("\"nonce\":\"fixed-nonce\"", "\"scope\":\"active\",\"continuation\":\"AQ_cursor\"");
        assertTrue(registry.validateKnown(ProtocolJson.validatorTree(request.getBytes(java.nio.charset.StandardCharsets.UTF_8)), KnownProtocolAction.QUESTS_QUERY).isEmpty());
        for (String invalid : new String[] {request.replace("AQ_cursor", "bad cursor"), request.replace("continuation", "pageOverride")}) {
            assertTrue(!registry.validateKnown(ProtocolJson.validatorTree(invalid.getBytes(java.nio.charset.StandardCharsets.UTF_8)), KnownProtocolAction.QUESTS_QUERY).isEmpty());
        }
    }

    @Test
    void packaged_registry_contains_request_schemas() {
        SchemaRegistry registry = SchemaRegistry.load();

        assertTrue(registry.validateEnvelope(ProtocolJson.validatorTree(
            ProtocolValidatorTestFixture.validRequest())).isEmpty());
        assertTrue(registry.validateKnown(
            ProtocolJson.validatorTree(ProtocolValidatorTestFixture.validRequest()),
            KnownProtocolAction.HEALTH_PING).isEmpty());
    }

    @Test
    void rejects_remote_references_before_compilation() {
        for (String keyword : new String[] {
            "$ref", "$dynamicRef", "$recursiveRef"
        }) {
            String remote = "https://attacker.invalid/schema.json";
            ProtocolValidationFailure failure = assertThrows(
                ProtocolValidationFailure.class,
                () -> SchemaRegistry.compile(minimalRegistry(
                    "\"" + keyword + "\":\"" + remote + "\"")));
            assertEquals(
                "Non-local schema reference is forbidden: " + remote,
                failure.getMessage());
        }
    }

    @Test
    void rejects_nested_remote_id_before_resolving_relative_references() {
        for (String keyword : new String[] {
            "$ref", "$dynamicRef", "$recursiveRef"
        }) {
            ProtocolValidationFailure failure = assertThrows(
                ProtocolValidationFailure.class,
                () -> SchemaRegistry.compile(minimalRegistry(
                    "\"allOf\":[{"
                        + "\"$id\":\"https://attacker.invalid/base/\","
                        + "\"" + keyword
                        + "\":\"relative.schema.json\"}]")));
            assertEquals(
                "Nested schema $id is forbidden: "
                    + "https://attacker.invalid/base/",
                failure.getMessage());
        }
    }

    private static Map<String, String> minimalRegistry(
        String requestProperties
    ) {
        String prefix =
            "{\"$schema\":\"https://json-schema.org/draft/2020-12/schema\","
                + "\"$id\":\"https://schemas.miaxion.dev/bridge/v1/";
        return Map.of(
            "request-envelope.schema.json",
            prefix + "request-envelope.schema.json\",\"type\":\"object\"}",
            "request.schema.json",
            prefix + "request.schema.json\"," + requestProperties + "}",
            "terminology-changed.schema.json",
            prefix + "terminology-changed.schema.json\",\"type\":\"object\"}",
            "relative.schema.json",
            prefix + "relative.schema.json\",\"type\":\"object\"}");
    }
}

final class ProtocolValidatorTestFixture {
    private ProtocolValidatorTestFixture() {}

    static byte[] validRequest() {
        return ("{\"protocolVersion\":\"1.0\","
            + "\"requestId\":\"b8b88ff4-36c0-4bdc-a5c1-8a976dc7d2cf\","
            + "\"timestamp\":\"2026-07-26T05:30:00Z\","
            + "\"deadline\":\"2026-07-26T05:30:03Z\","
            + "\"action\":\"health.ping\","
            + "\"sessionToken\":\"QmFzZTY0dXJsLXNlc3Npb24tdG9rZW4tdjEtMzItYnl0ZXM\","
            + "\"payload\":{\"nonce\":\"fixed-nonce\"},\"error\":null}")
            .getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }
}
