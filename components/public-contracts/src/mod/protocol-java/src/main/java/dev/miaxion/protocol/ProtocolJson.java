package dev.miaxion.protocol;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

final class ProtocolJson {
    private static final ObjectMapper MAPPER = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .disable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
            .disable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
            .enable(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS)
            .build();

    private ProtocolJson() {}

    static String write(Object value) throws JsonProcessingException {
        return MAPPER.writeValueAsString(value);
    }

    static byte[] writeBytes(Object value) throws JsonProcessingException {
        return MAPPER.writeValueAsBytes(value);
    }

    static <T> T read(String json, Class<T> type) throws JsonProcessingException {
        T result = MAPPER.readValue(json, type);
        if (result == null) {
            throw MismatchedInputException.from(
                null, type, "JSON did not contain " + type.getSimpleName());
        }
        return result;
    }

    static <T> T read(byte[] json, Class<T> type)
        throws IOException
    {
        T result = MAPPER.readValue(json, type);
        if (result == null) {
            throw MismatchedInputException.from(
                null, type, "JSON did not contain " + type.getSimpleName());
        }
        return result;
    }

    static JsonNode tree(String json) throws JsonProcessingException {
        return MAPPER.readTree(json);
    }

    static tools.jackson.databind.JsonNode validatorTree(byte[] utf8) {
        return ValidatorJson.read(utf8);
    }

    static byte[] validatorBytes(tools.jackson.databind.JsonNode tree) {
        return ValidatorJson.write(tree);
    }

    private static final class ValidatorJson {
        private static final ProtocolRuntimeLimits LIMITS = loadLimits();
        private static final tools.jackson.databind.ObjectMapper MAPPER =
            tools.jackson.databind.json.JsonMapper.builder(
                tools.jackson.core.json.JsonFactory.builder()
                    .enable(
                        tools.jackson.core.StreamReadFeature
                            .STRICT_DUPLICATE_DETECTION)
                    .streamReadConstraints(
                        tools.jackson.core.StreamReadConstraints.builder()
                            .maxNestingDepth(LIMITS.jsonDepth())
                            .maxStringLength(
                                LIMITS.rawJsonStringUtf16Units())
                            .maxNameLength(
                                LIMITS.rawJsonStringUtf16Units())
                            .maxNumberLength(
                                LIMITS.jsonNumberAsciiBytes())
                            .build())
                    .build())
                .enable(
                    tools.jackson.databind.DeserializationFeature
                        .FAIL_ON_TRAILING_TOKENS)
                .enable(
                    tools.jackson.databind.DeserializationFeature
                        .FAIL_ON_READING_DUP_TREE_KEY)
                .build();

        private ValidatorJson() {}

        private static ProtocolRuntimeLimits loadLimits() {
            try {
                return ProtocolRuntimeLimitsLoader.load();
            } catch (IOException exception) {
                throw new ExceptionInInitializerError(exception);
            }
        }

        static tools.jackson.databind.JsonNode read(byte[] utf8) {
            if (utf8 == null) {
                throw new NullPointerException("utf8");
            }
            if (utf8.length >= 3
                && utf8[0] == (byte) 0xEF
                && utf8[1] == (byte) 0xBB
                && utf8[2] == (byte) 0xBF) {
                throw new ProtocolValidationFailure("UTF-8 BOM is forbidden.");
            }
            String json;
            try {
                json = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(utf8))
                    .toString();
            } catch (CharacterCodingException exception) {
                throw new ProtocolValidationFailure(
                    "Protocol bytes are not strict UTF-8.", exception);
            }
            try {
                return MAPPER.readTree(json);
            } catch (tools.jackson.core.JacksonException exception) {
                throw new ProtocolValidationFailure(
                    "Malformed protocol JSON.", exception);
            }
        }

        static byte[] write(tools.jackson.databind.JsonNode tree) {
            try {
                return MAPPER.writeValueAsBytes(tree);
            } catch (tools.jackson.core.JacksonException exception) {
                throw new ProtocolValidationFailure(
                    "Could not canonicalize protocol JSON.", exception);
            }
        }
    }
}
