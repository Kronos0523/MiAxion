package dev.miaxion.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

final class ProtocolRuntimeLimitsTest {
    private static final Path ROOT =
        Path.of("..", "..", "..").toAbsolutePath().normalize();
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final ProtocolRuntimeLimits EXPECTED =
        new ProtocolRuntimeLimits(
            1,
            8192,
            5000,
            5000,
            65536,
            1048576,
            67108864,
            60000,
            10000,
            30000,
            1800000,
            120000,
            8,
            2,
            1,
            32,
            32768,
            16384,
            65536,
            64,
            280,
            4,
            16,
            2097152,
            134217728,
            5000,
            30000,
            600,
            128,
            120,
            32,
            60,
            16,
            3,
            3,
            120,
            32,
            4194304,
            30000,
            10000,
            1,
            1,
            30,
            8,
            120,
            32,
            25,
            101,
            256,
            256,
            1024,
            128,
            32,
            128,
            256,
            8,
            256,
            2,
            8);

    @Test
    void source_copied_resource_and_public_model_are_identical()
        throws Exception
    {
        byte[] source = Files.readAllBytes(
            ROOT.resolve("protocol/runtime-limits.json"));

        assertEquals(EXPECTED, ProtocolRuntimeLimitsLoader.parse(source));
        assertEquals(EXPECTED, ProtocolRuntimeLimitsLoader.load());
        assertEquals(
            59,
            ProtocolRuntimeLimits.class.getRecordComponents().length);
    }

    @Test
    void loader_rejects_missing_unknown_and_duplicate_properties()
        throws Exception
    {
        byte[] source = Files.readAllBytes(
            ROOT.resolve("protocol/runtime-limits.json"));
        ObjectNode missing =
            (ObjectNode) MAPPER.readTree(source);
        missing.remove("hudBurst");
        ObjectNode unknown =
            (ObjectNode) MAPPER.readTree(source);
        unknown.put("surprise", 1);
        String duplicate = new String(
            source,
            StandardCharsets.UTF_8).replace(
                "\"schemaVersion\": 1,",
                "\"schemaVersion\": 1,\n  \"schemaVersion\": 1,");

        assertThrows(
            IOException.class,
            () -> ProtocolRuntimeLimitsLoader.parse(
                MAPPER.writeValueAsBytes(missing)));
        assertThrows(
            IOException.class,
            () -> ProtocolRuntimeLimitsLoader.parse(
                MAPPER.writeValueAsBytes(unknown)));
        assertThrows(
            IOException.class,
            () -> ProtocolRuntimeLimitsLoader.parse(
                duplicate.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void loader_rejects_encoding_trailing_and_numeric_coercion()
        throws Exception
    {
        byte[] source = Files.readAllBytes(
            ROOT.resolve("protocol/runtime-limits.json"));
        String text = new String(source, StandardCharsets.UTF_8);
        byte[] bom = new byte[source.length + 3];
        bom[0] = (byte) 0xEF;
        bom[1] = (byte) 0xBB;
        bom[2] = (byte) 0xBF;
        System.arraycopy(source, 0, bom, 3, source.length);

        assertThrows(
            IOException.class,
            () -> ProtocolRuntimeLimitsLoader.parse(
                new byte[] {(byte) 0xFF}));
        assertThrows(
            IOException.class,
            () -> ProtocolRuntimeLimitsLoader.parse(bom));
        assertThrows(
            IOException.class,
            () -> ProtocolRuntimeLimitsLoader.parse(
                (text + "{}").getBytes(StandardCharsets.UTF_8)));
        assertThrows(
            IOException.class,
            () -> ProtocolRuntimeLimitsLoader.parse(
                text.replace(
                    "\"hudBurst\": 16",
                    "\"hudBurst\": 16.5")
                    .getBytes(StandardCharsets.UTF_8)));
        assertThrows(
            IOException.class,
            () -> ProtocolRuntimeLimitsLoader.parse(
                text.replace(
                    "\"hudBurst\": 16",
                    "\"hudBurst\": \"16\"")
                    .getBytes(StandardCharsets.UTF_8)));
    }
}
