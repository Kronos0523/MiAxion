package dev.miaxion.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.Error;
import com.networknt.schema.InputFormat;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SpecificationVersion;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

final class ProtocolCorpusTest {
    private static final Path ROOT =
        Path.of("..", "..", "..").toAbsolutePath().normalize();
    private static final Path SCHEMA_ROOT =
        ROOT.resolve("protocol/schema");
    private static final String SCHEMA_BASE =
        "https://schemas.miaxion.dev/bridge/v1/";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final SchemaRegistry REGISTRY = createRegistry();

    @Test
    void logical_manifest_is_ordinal_and_unknown_action_stays_open()
        throws Exception {
        JsonNode manifest = MAPPER.readTree(readStrictUtf8(
            ROOT.resolve("protocol/fixtures/manifest.json")));
        List<String> paths = StreamSupport.stream(
                manifest.get("fixtures").spliterator(),
                false)
            .map(entry -> entry.get("path").asText())
            .toList();
        assertEquals(paths.stream().sorted().toList(), paths);
        assertTrue(paths.contains("valid/unknown-action-request.json"));
        assertTrue(paths.contains(
            "valid/unknown-action-capability-unavailable-response.json"));
        assertFalse(paths.stream().anyMatch(
            path -> path.contains("mod-inventory")));
    }

    static Stream<Arguments> fixtures() throws Exception {
        JsonNode manifest = MAPPER.readTree(readStrictUtf8(
            ROOT.resolve("protocol/fixtures/manifest.json")));

        return StreamSupport.stream(
            manifest.get("fixtures").spliterator(),
            false).map(entry -> Arguments.of(
                entry.get("path").asText(),
                entry.get("schema").asText(),
                entry.get("expected").asText(),
                entry.get("reason").asText()));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("fixtures")
    void fixture_matches_expected_result(
        String fixturePath,
        String schemaName,
        String expectation,
        String reason
    ) throws Exception {
        String fixtureJson = readStrictUtf8(
            ROOT.resolve("protocol/fixtures").resolve(fixturePath));
        List<Error> messages =
            validationMessages(schemaName, fixtureJson);
        JsonNode instance = MAPPER.readTree(fixtureJson);
        boolean temporalValid =
            !schemaName.equals("request.schema.json")
                || requestTimeIsStrictlyOrdered(instance);
        boolean effectiveValid =
            messages.isEmpty() && temporalValid;

        assertEquals(
            expectation.equals("valid"),
            effectiveValid,
            () -> "path=" + fixturePath
                + System.lineSeparator()
                + "schema=" + schemaName
                + System.lineSeparator()
                + "reason=" + reason
                + System.lineSeparator()
                + "evaluation=" + messages);
    }

    static List<Error> validationMessages(
        String schemaName,
        String json
    ) {
        Schema schema = REGISTRY.getSchema(
            SchemaLocation.of(SCHEMA_BASE + schemaName));

        return schema.validate(
            json,
            InputFormat.JSON,
            context -> context.executionConfig(
                config -> config.formatAssertionsEnabled(true)));
    }

    static boolean requestTimeIsStrictlyOrdered(JsonNode instance) {
        JsonNode timestamp = instance.get("timestamp");
        JsonNode deadline = instance.get("deadline");

        return timestamp != null
            && timestamp.isTextual()
            && deadline != null
            && deadline.isTextual()
            && OffsetDateTime.parse(timestamp.asText())
                .isBefore(OffsetDateTime.parse(deadline.asText()));
    }

    private static SchemaRegistry createRegistry() {
        try {
            Map<String, String> schemas = Map.of(
                SCHEMA_BASE + "extensions.schema.json",
                readStrictUtf8(SCHEMA_ROOT.resolve("extensions.schema.json")),
                SCHEMA_BASE + "common.schema.json",
                readStrictUtf8(
                    SCHEMA_ROOT.resolve("common.schema.json")),
                SCHEMA_BASE + "world-query.schema.json",
                readStrictUtf8(
                    SCHEMA_ROOT.resolve("world-query.schema.json")),
                SCHEMA_BASE + "player-observation.schema.json",
                readStrictUtf8(SCHEMA_ROOT.resolve("player-observation.schema.json")),
                SCHEMA_BASE + "quest.schema.json",
                readStrictUtf8(
                    SCHEMA_ROOT.resolve("quest.schema.json")),
                SCHEMA_BASE + "recipe.schema.json",
                readStrictUtf8(
                    SCHEMA_ROOT.resolve("recipe.schema.json")),
                SCHEMA_BASE + "request-envelope.schema.json",
                readStrictUtf8(
                    SCHEMA_ROOT.resolve("request-envelope.schema.json")),
                SCHEMA_BASE + "request.schema.json",
                readStrictUtf8(
                    SCHEMA_ROOT.resolve("request.schema.json")),
                SCHEMA_BASE + "response.schema.json",
                readStrictUtf8(
                    SCHEMA_ROOT.resolve("response.schema.json")),
                SCHEMA_BASE + "terminology-changed.schema.json",
                readStrictUtf8(
                    SCHEMA_ROOT.resolve("terminology-changed.schema.json")));

            return SchemaRegistry.withDefaultDialect(
                SpecificationVersion.DRAFT_2020_12,
                builder -> builder.schemas(schemas));
        } catch (IOException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    private static String readStrictUtf8(Path path) throws IOException {
        byte[] bytes = Files.readAllBytes(path);
        if (bytes.length >= 3
            && bytes[0] == (byte) 0xEF
            && bytes[1] == (byte) 0xBB
            && bytes[2] == (byte) 0xBF) {
            throw new IOException("UTF-8 BOM is forbidden: " + path);
        }

        String text = StandardCharsets.UTF_8.newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT)
            .decode(ByteBuffer.wrap(bytes))
            .toString();
        if (text.contains("\r")) {
            throw new IOException("CR line endings are forbidden: " + path);
        }
        if (!text.endsWith("\n") || text.endsWith("\n\n")) {
            throw new IOException(
                "Expected exactly one final LF: " + path);
        }

        return text;
    }
}
