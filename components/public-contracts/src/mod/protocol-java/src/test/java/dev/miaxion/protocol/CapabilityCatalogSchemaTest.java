package dev.miaxion.protocol;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;
import org.junit.jupiter.api.Test;

final class CapabilityCatalogSchemaTest {
    @Test void providerDirectoryDoesNotInheritTheOld32ItemLimit() {
        var providers = java.util.stream.IntStream.range(0, 96).mapToObj(index ->
            """
            {"addonId":"addon_%d","addonVersion":"1.0","targetModId":"jei",
             "query":{"status":"GAME_NOT_READY","reason":"RUNTIME_PENDING"},
             "uses":{"status":"GAME_NOT_READY","reason":"RUNTIME_PENDING"},"sourceGeneration":1}
            """.formatted(index)).collect(java.util.stream.Collectors.joining(","));
        var response = """
            {"protocolVersion":"1.0","requestId":"00000000-0000-4000-8000-000000000001",
             "timestamp":"2026-10-17T00:00:00Z","deadline":"2026-10-17T00:00:10Z",
             "action":"recipe.providers.list","sessionToken":null,"error":null,"payload":{"providers":[%s]}}
            """.formatted(providers);
        assertTrue(ProtocolCorpusTest.validationMessages("response.schema.json", response).isEmpty());
    }

    @Test void completeFabricHostCatalogMatchesTheSharedResponseSchema() throws Exception {
        var payload = Files.readString(Path.of("../../../tests/fixtures/bridge/fabric121-capabilities.json"));
        var envelope = """
            {"protocolVersion":"1.0","requestId":"00000000-0000-4000-8000-000000000001",
             "timestamp":"2026-10-17T00:00:00Z","deadline":"2026-10-17T00:00:10Z",
             "action":"capabilities.list","sessionToken":null,"error":null,"payload":%s}
            """.formatted(payload);
        assertTrue(ProtocolCorpusTest.validationMessages("response.schema.json", envelope).isEmpty());
        var malformed = envelope.replace("\"available\" : true", "\"available\" : \"true\"");
        assertNotEquals(envelope, malformed);
        assertFalse(ProtocolCorpusTest.validationMessages("response.schema.json", malformed).isEmpty());
    }
}
