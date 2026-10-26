package dev.miaxion.protocol;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public final class ProtocolRuntimeLimitsLoader {
    private static final String RESOURCE_NAME = "runtime-limits.json";
    private static final ObjectMapper MAPPER = JsonMapper.builder(
            JsonFactory.builder()
                .enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION)
                .build())
        .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .enable(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES)
        .enable(DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES)
        .enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
        .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
        .disable(DeserializationFeature.ACCEPT_FLOAT_AS_INT)
        .disable(MapperFeature.ALLOW_COERCION_OF_SCALARS)
        .build();

    private ProtocolRuntimeLimitsLoader() {}

    public static ProtocolRuntimeLimits load() throws IOException {
        try (InputStream stream =
                ProtocolRuntimeLimitsLoader.class.getResourceAsStream(
                    RESOURCE_NAME)) {
            if (stream == null) {
                throw new IOException(
                    "Classpath resource is missing: " + RESOURCE_NAME);
            }
            return parse(stream.readAllBytes());
        }
    }

    public static ProtocolRuntimeLimits parse(byte[] utf8Json)
        throws IOException
    {
        if (utf8Json.length >= 3
            && utf8Json[0] == (byte) 0xEF
            && utf8Json[1] == (byte) 0xBB
            && utf8Json[2] == (byte) 0xBF) {
            throw new IOException("UTF-8 BOM is forbidden.");
        }

        String json = StandardCharsets.UTF_8.newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT)
            .decode(ByteBuffer.wrap(utf8Json))
            .toString();
        ProtocolRuntimeLimits limits = MAPPER.readValue(
            json,
            ProtocolRuntimeLimits.class);

        if (limits.schemaVersion() != 1
            || Arrays.stream(
                ProtocolRuntimeLimits.class.getRecordComponents())
                .anyMatch(component -> {
                    try {
                        return (int) component.getAccessor().invoke(limits) <= 0;
                    } catch (ReflectiveOperationException exception) {
                        throw new IllegalStateException(exception);
                    }
                })) {
            throw new IOException(
                "Runtime limit values and schema version must be positive and supported.");
        }

        return limits;
    }
}
