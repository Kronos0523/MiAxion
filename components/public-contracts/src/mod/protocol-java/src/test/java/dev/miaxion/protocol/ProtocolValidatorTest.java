package dev.miaxion.protocol;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

final class ProtocolValidatorTest {
    private static final Path RAW_FIXTURES = Path.of(
        "..", "..", "..", "protocol", "fixtures", "raw")
        .toAbsolutePath().normalize();
    private static final ReceiveStamp RECEIVED = new ReceiveStamp(
        Instant.parse("2026-07-26T05:30:01Z"),
        -17L);

    @Test
    void rejects_malformed_utf8_duplicate_keys_trailing_tokens_and_non_objects() {
        ProtocolValidator validator = new ProtocolValidator();

        assertThrows(
            ProtocolValidationFailure.class,
            () -> validator.parseCommon(
                new byte[] {'{', '"', 'x', '"', ':', '"', (byte) 0xC3,
                    (byte) 0x28, '"', '}'},
                RECEIVED));
        assertThrows(
            ProtocolValidationFailure.class,
            () -> validator.parseCommon(bytes(validRequest().replace(
                "\"action\":\"health.ping\"",
                "\"action\":\"health.ping\",\"action\":\"health.ping\"")),
                RECEIVED));
        assertThrows(
            ProtocolValidationFailure.class,
            () -> validator.parseCommon(bytes(validRequest() + " true"), RECEIVED));
        assertThrows(
            ProtocolValidationFailure.class,
            () -> validator.parseCommon(bytes("[]"), RECEIVED));
    }

    @Test
    void rejects_non_utf8_encodings_bom_and_invalid_utf8_sequences() {
        ProtocolValidator validator = new ProtocolValidator();

        for (String charsetName : new String[] {
            "UTF-16LE", "UTF-16BE", "UTF-32LE", "UTF-32BE"
        }) {
            assertThrows(
                ProtocolValidationFailure.class,
                () -> validator.parseCommon(
                    validRequest().getBytes(Charset.forName(charsetName)),
                    RECEIVED),
                charsetName);
        }
        assertThrows(
            ProtocolValidationFailure.class,
            () -> validator.parseCommon(
                withPrefix(
                    new byte[] {(byte) 0xFF, (byte) 0xFE},
                    validRequest().getBytes(Charset.forName("UTF-16LE"))),
                RECEIVED));
        assertThrows(
            ProtocolValidationFailure.class,
            () -> validator.parseCommon(
                withPrefix(
                    new byte[] {(byte) 0xFE, (byte) 0xFF},
                    validRequest().getBytes(Charset.forName("UTF-16BE"))),
                RECEIVED));
        assertThrows(
            ProtocolValidationFailure.class,
            () -> validator.parseCommon(
                withPrefix(
                    new byte[] {
                        (byte) 0xFF, (byte) 0xFE, 0x00, 0x00
                    },
                    validRequest().getBytes(Charset.forName("UTF-32LE"))),
                RECEIVED));
        assertThrows(
            ProtocolValidationFailure.class,
            () -> validator.parseCommon(
                withPrefix(
                    new byte[] {
                        0x00, 0x00, (byte) 0xFE, (byte) 0xFF
                    },
                    validRequest().getBytes(Charset.forName("UTF-32BE"))),
                RECEIVED));
        assertThrows(
            ProtocolValidationFailure.class,
            () -> validator.parseCommon(
                withPrefix(
                    new byte[] {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF},
                    bytes(validRequest())),
                RECEIVED));

        assertMalformedUtf8(
            validator,
            new byte[] {'{', '"', 'x', '"', ':', '"', (byte) 0xE2,
                (byte) 0x82});
        assertMalformedUtf8(
            validator,
            new byte[] {'{', '"', 'x', '"', ':', '"', (byte) 0xC0,
                (byte) 0xAF, '"', '}'});
        assertMalformedUtf8(
            validator,
            new byte[] {'{', '"', 'x', '"', ':', '"', (byte) 0xF5,
                (byte) 0x80, (byte) 0x80, (byte) 0x80, '"', '}'});
        assertMalformedUtf8(
            validator,
            new byte[] {'{', '"', 'x', '"', ':', '"', (byte) 0x80,
                '"', '}'});
    }

    @Test
    void enforces_depth_raw_utf16_semantic_scalar_and_number_limits() {
        ProtocolValidator validator = new ProtocolValidator();
        String deep = "{}";
        for (int index = 0; index < 32; index++) {
            deep = "{\"x\":" + deep + "}";
        }
        String overDepth = deep;
        String tooManyRawUnits = "a".repeat(32_769);
        String tooManyScalars = "界".repeat(16_385);
        String maximumSurrogatePairs = "🙂".repeat(16_384);
        String tooManyMixedScalars = "🙂".repeat(16_383) + "界界";
        String tooLongNumber = "1".repeat(65);

        assertThrows(
            ProtocolValidationFailure.class,
            () -> validator.parseCommon(
                bytes(unknownRequest(overDepth)), RECEIVED));
        assertThrows(
            ProtocolValidationFailure.class,
            () -> validator.parseCommon(
                bytes(unknownRequest("{\"probe\":\"" + tooManyRawUnits + "\"}")),
                RECEIVED));
        assertThrows(
            ProtocolValidationFailure.class,
            () -> validator.parseCommon(
                bytes(unknownRequest("{\"probe\":\"" + tooManyScalars + "\"}")),
                RECEIVED));
        assertEquals(
            "vendor.echo",
            validator.parseCommon(
                bytes(unknownRequest(
                    "{\"probe\":\"" + maximumSurrogatePairs + "\"}")),
                RECEIVED).rawAction());
        assertThrows(
            ProtocolValidationFailure.class,
            () -> validator.parseCommon(
                bytes(unknownRequest(
                    "{\"probe\":\"" + tooManyMixedScalars + "\"}")),
                RECEIVED));
        assertThrows(
            ProtocolValidationFailure.class,
            () -> validator.parseCommon(
                bytes(unknownRequest("{\"probe\":" + tooLongNumber + "}")),
                RECEIVED));
    }

    @Test
    void rejects_unpaired_surrogates_in_values_and_property_names() {
        ProtocolValidator validator = new ProtocolValidator();
        String maximumScalarName = "🙂".repeat(16_384);
        String tooManyScalarName = "🙂".repeat(16_383) + "界界";

        assertEquals(
            "vendor.echo",
            validator.parseCommon(
                bytes(unknownRequest(
                    "{\"" + maximumScalarName + "\":true}")),
                RECEIVED).rawAction());
        assertThrows(
            ProtocolValidationFailure.class,
            () -> validator.parseCommon(
                bytes(unknownRequest(
                    "{\"" + tooManyScalarName + "\":true}")),
                RECEIVED));

        for (String escaped : new String[] {"\\uD800", "\\uDC00"}) {
            ProtocolValidationFailure valueFailure = assertThrows(
                ProtocolValidationFailure.class,
                () -> validator.parseCommon(
                    bytes(unknownRequest(
                        "{\"probe\":\"" + escaped + "\"}")),
                    RECEIVED));
            assertTrue(valueFailure.getMessage().contains("Unicode scalar"));

            ProtocolValidationFailure nameFailure = assertThrows(
                ProtocolValidationFailure.class,
                () -> validator.parseCommon(
                    bytes(unknownRequest(
                        "{\"" + escaped + "\":true}")),
                    RECEIVED));
            assertTrue(nameFailure.getMessage().contains("Unicode scalar"));
        }
    }

    @Test
    void defers_canonical_inbound_size_until_after_common_validation()
        throws Exception
    {
        ProtocolValidator validator = new ProtocolValidator();
        String largeCanonicalPayload = "{\"a\":\"" + "a".repeat(16_384)
            + "\",\"b\":\"" + "b".repeat(16_384)
            + "\",\"c\":\"" + "c".repeat(16_384)
            + "\",\"d\":\"" + "d".repeat(16_384) + "\"}";

        assertEquals(
            "vendor.echo",
            validator.parseCommon(
                bytes(unknownRequest(largeCanonicalPayload)), RECEIVED)
                .rawAction());
        assertEquals(
            "future.echo",
            validator.parseCommon(
                Files.readAllBytes(RAW_FIXTURES.resolve(
                    "java-canonical-inbound-bytes-limit-minus-one.json.bin")),
                RECEIVED).rawAction());
        assertEquals(
            "future.echo",
            validator.parseCommon(
                Files.readAllBytes(RAW_FIXTURES.resolve(
                    "java-canonical-inbound-bytes-limit.json.bin")),
                RECEIVED).rawAction());
        assertEquals(
            "future.echo",
            validator.parseCommon(
                Files.readAllBytes(RAW_FIXTURES.resolve(
                    "java-canonical-inbound-bytes-limit-plus-one.json.bin")),
                RECEIVED).rawAction());

        byte[] whitespaceHeavy = bytes(validRequest().replace(
            "{",
            "{                                                                "));
        assertTrue(whitespaceHeavy.length > bytes(validRequest()).length);
        assertEquals(
            KnownProtocolAction.HEALTH_PING.wireValue(),
            validator.parseCommon(whitespaceHeavy, RECEIVED).rawAction());
    }

    @Test
    void rejects_schema_valid_known_request_after_common_at_canonical_cap() {
        ProtocolValidator validator = new ProtocolValidator(
            permissiveKnownRequestSchemas());
        String largePayload = "{\"a\":\"" + "a".repeat(16_384)
            + "\",\"b\":\"" + "b".repeat(16_384)
            + "\",\"c\":\"" + "c".repeat(16_384)
            + "\",\"d\":\"" + "d".repeat(16_384) + "\"}";

        CommonRequest common = validator.parseCommon(
            bytes(validRequest().replace(
                "{\"nonce\":\"fixed-nonce\"}",
                largePayload)),
            RECEIVED);
        int parsesAfterCommon = validator.rawParseCount();

        ProtocolValidationFailure failure = assertThrows(
            ProtocolValidationFailure.class,
            () -> validator.validateKnown(
                common,
                KnownProtocolAction.HEALTH_PING));
        assertEquals(
            "Canonical request exceeds 65536 bytes.",
            failure.getMessage());
        assertEquals(parsesAfterCommon, validator.rawParseCount());
    }

    @Test
    void stages_common_unknown_and_known_payload_validation_without_reparse() {
        ProtocolValidator validator = new ProtocolValidator();
        assertFalse(Arrays.stream(
                CommonRequest.ValidatorHandle.class.getDeclaredFields())
            .anyMatch(field -> field.getType().equals(byte[].class)));
        assertThrows(
            NullPointerException.class,
            () -> validator.validateKnown(null, KnownProtocolAction.HEALTH_PING));

        CommonRequest unknown = validator.parseCommon(
            bytes(unknownRequest("{}")), RECEIVED);
        assertFalse(KnownProtocolAction.fromWire(unknown.rawAction()).isPresent());
        assertThrows(
            ProtocolValidationFailure.class,
            () -> validator.validateKnown(unknown, KnownProtocolAction.HEALTH_PING));

        CommonRequest invalidKnown = validator.parseCommon(
            bytes(validRequest().replace(
                "{\"nonce\":\"fixed-nonce\"}",
                "{\"nonce\":7}")),
            RECEIVED);
        int parsesAfterCommon = validator.rawParseCount();
        ProtocolValidationFailure payloadFailure = assertThrows(
            ProtocolValidationFailure.class,
            () -> validator.validateKnown(
                invalidKnown,
                KnownProtocolAction.HEALTH_PING));
        assertTrue(payloadFailure.getMessage().contains("payload"));
        assertEquals(parsesAfterCommon, validator.rawParseCount());
    }

    @Test
    void validates_known_request_and_serializes_response_as_utf8_bytes()
        throws Exception
    {
        ProtocolValidator validator = new ProtocolValidator();
        CommonRequest common = validator.parseCommon(bytes(validRequest()), RECEIVED);
        KnownRequest known = validator.validateKnown(
            common,
            KnownProtocolAction.HEALTH_PING);

        assertEquals(KnownProtocolAction.HEALTH_PING, known.action());
        assertEquals("fixed-nonce", ((HealthPingRequest) known.payload()).nonce());
        assertEquals(common.requestId(), known.envelope().requestId());
        assertArrayEquals(known.canonicalBytes(), ProtocolJson.writeBytes(known.envelope()));

        ResponseEnvelope response = new ResponseEnvelope(
            known.envelope().protocolVersion(),
            known.envelope().requestId(),
            known.envelope().timestamp(),
            known.envelope().deadline(),
            known.envelope().action(),
            null,
            ProtocolJson.tree("{\"nonce\":\"fixed-nonce\",\"gameReady\":false,\"protocolVersion\":\"1.0\"}"),
            null);
        assertArrayEquals(ProtocolJson.writeBytes(response), validator.serialize(response));
    }

    @Test
    void leaves_receive_relative_deadline_windows_to_the_session_engine() {
        ProtocolValidator validator = new ProtocolValidator();
        CommonRequest expired = validator.parseCommon(
            bytes(validRequest()),
            new ReceiveStamp(Instant.parse("2026-07-26T05:30:03Z"), 10));
        CommonRequest beyondActionMaximum = validator.parseCommon(
            bytes(validRequest().replace(
                "2026-07-26T05:30:03Z",
                "2026-07-26T05:30:12Z")),
            new ReceiveStamp(Instant.parse("2026-07-26T05:30:01Z"), 20));

        assertEquals(Instant.parse("2026-07-26T05:30:03Z"),
            expired.deadline().toInstant());
        assertEquals(Instant.parse("2026-07-26T05:30:12Z"),
            beyondActionMaximum.deadline().toInstant());
    }

    @Test
    void exposesOnlySafeCorrelationForCommonEnvelopeFailures() {
        ProtocolValidator validator = new ProtocolValidator();
        ProtocolValidationFailure unknownField = assertThrows(
            ProtocolValidationFailure.class,
            () -> validator.parseCommon(bytes(validRequest().replace(
                "\"error\":null}",
                "\"error\":null,\"unexpected\":true}")), RECEIVED));
        assertTrue(unknownField.correlation().isPresent());
        assertFalse(unknownField.correlation().orElseThrow()
            .invalidEnvelopeSentinel());
        assertEquals(ProtocolErrorCode.INVALID_REQUEST,
            unknownField.errorCode());

        ProtocolValidationFailure invalidDeadline = assertThrows(
            ProtocolValidationFailure.class,
            () -> validator.parseCommon(bytes(validRequest().replace(
                "\"deadline\":\"2026-07-26T05:30:03Z\"",
                "\"deadline\":7")), RECEIVED));
        assertTrue(invalidDeadline.correlation().orElseThrow()
            .invalidEnvelopeSentinel());

        ProtocolValidationFailure malformed = assertThrows(
            ProtocolValidationFailure.class,
            () -> validator.parseCommon(new byte[] {(byte) 0xC3}, RECEIVED));
        assertTrue(malformed.correlation().isEmpty());
    }

    @Test
    void unknownRawActionSchemaAllowsOnlyClosedProtocolErrors()
        throws Exception
    {
        ProtocolValidator validator = new ProtocolValidator();
        for (ProtocolErrorCode code : List.of(
            ProtocolErrorCode.CAPABILITY_UNAVAILABLE,
            ProtocolErrorCode.UNAUTHORIZED,
            ProtocolErrorCode.INVALID_REQUEST,
            ProtocolErrorCode.TIMEOUT,
            ProtocolErrorCode.UNSUPPORTED_VERSION,
            ProtocolErrorCode.RATE_LIMITED)) {
            ResponseEnvelope response = unknownActionResponse(
                new ProtocolError(code, "safe error", false, null), null);
            String serialized = new String(
                validator.serialize(response), StandardCharsets.UTF_8);
            assertTrue(ProtocolCorpusTest.validationMessages(
                "response.schema.json", serialized).isEmpty(), code.name());
        }

        String success = new String(validator.serialize(
            unknownActionResponse(null,
                ProtocolJson.tree("{\"accepted\":true}"))),
            StandardCharsets.UTF_8);
        assertFalse(ProtocolCorpusTest.validationMessages(
            "response.schema.json", success).isEmpty());

        String disallowed = new String(validator.serialize(
            unknownActionResponse(new ProtocolError(
                ProtocolErrorCode.INTERNAL_ERROR,
                "safe error", false, null), null)),
            StandardCharsets.UTF_8);
        assertFalse(ProtocolCorpusTest.validationMessages(
            "response.schema.json", disallowed).isEmpty());
    }

    private static ResponseEnvelope unknownActionResponse(
        ProtocolError error,
        com.fasterxml.jackson.databind.JsonNode payload
    ) {
        return new ResponseEnvelope(
            "1.0",
            java.util.UUID.fromString(
                "b8b88ff4-36c0-4bdc-a5c1-8a976dc7d2cf"),
            java.time.OffsetDateTime.parse("2026-07-26T05:30:01Z"),
            java.time.OffsetDateTime.parse("2026-07-26T05:30:03Z"),
            new ProtocolAction("future.echo"),
            null,
            payload,
            error);
    }

    private static String validRequest() {
        return "{\"protocolVersion\":\"" + ProtocolConstants.VERSION + "\","
            + "\"requestId\":\"b8b88ff4-36c0-4bdc-a5c1-8a976dc7d2cf\","
            + "\"timestamp\":\"2026-07-26T05:30:00Z\","
            + "\"deadline\":\"2026-07-26T05:30:03Z\","
            + "\"action\":\"health.ping\","
            + "\"sessionToken\":\"QmFzZTY0dXJsLXNlc3Npb24tdG9rZW4tdjEtMzItYnl0ZXM\","
            + "\"payload\":{\"nonce\":\"fixed-nonce\"},\"error\":null}";
    }

    private static String unknownRequest(String payload) {
        return validRequest()
            .replace("\"health.ping\"", "\"vendor.echo\"")
            .replace("{\"nonce\":\"fixed-nonce\"}", payload);
    }

    private static byte[] bytes(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }

    private static byte[] withPrefix(byte[] prefix, byte[] bytes) {
        byte[] result = Arrays.copyOf(prefix, prefix.length + bytes.length);
        System.arraycopy(bytes, 0, result, prefix.length, bytes.length);
        return result;
    }

    private static void assertMalformedUtf8(
        ProtocolValidator validator,
        byte[] malformed
    ) {
        ProtocolValidationFailure failure = assertThrows(
            ProtocolValidationFailure.class,
            () -> validator.parseCommon(malformed, RECEIVED));
        assertTrue(failure.getCause() instanceof CharacterCodingException);
    }

    private static SchemaRegistry permissiveKnownRequestSchemas() {
        String schemaPrefix =
            "{\"$schema\":\"https://json-schema.org/draft/2020-12/schema\","
                + "\"$id\":\"https://schemas.miaxion.dev/bridge/v1/";
        String envelope = schemaPrefix + "request-envelope.schema.json\","
            + "\"type\":\"object\",\"required\":["
            + "\"protocolVersion\",\"requestId\",\"timestamp\","
            + "\"deadline\",\"action\",\"sessionToken\","
            + "\"payload\",\"error\"]}";
        String request = schemaPrefix + "request.schema.json\","
            + "\"type\":\"object\"}";
        String terminologyChanged = schemaPrefix
            + "terminology-changed.schema.json\",\"type\":\"object\"}";
        return SchemaRegistry.compile(Map.of(
            "request-envelope.schema.json", envelope,
            "request.schema.json", request,
            "terminology-changed.schema.json", terminologyChanged));
    }
}
