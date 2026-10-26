package dev.miaxion.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.Test;

final class RawProtocolCorpusTest {
    private static final Path ROOT =
        Path.of("..", "..", "..").toAbsolutePath().normalize();
    private static final Path FIXTURES = ROOT.resolve("protocol/fixtures");
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final List<String> REQUEST_ORDER = List.of(
        "public-validation",
        "authentication",
        "rate-limit",
        "action-payload-validation");
    private static final List<String> PAIR_ORDER = List.of(
        "public-validation",
        "pair-rate-limit",
        "pair-payload-validation",
        "pairing-state-transition");
    private static final List<String> REQUEST_VALIDATOR_ORDER = List.of(
        "strict-utf8",
        "single-json-object",
        "read-constraints",
        "public-validation",
        "authentication",
        "rate-limit",
        "action-payload-validation");
    private static final List<String> REQUEST_TRANSPORT_ORDER = List.of(
        "frame-type",
        "transport-size",
        "strict-utf8",
        "single-json-object",
        "read-constraints",
        "public-validation",
        "authentication",
        "rate-limit",
        "action-payload-validation");
    private static final List<String> RESPONSE_ORDER = List.of(
        "frame-type",
        "transport-size",
        "strict-utf8",
        "single-json-object",
        "correlation",
        "response-validation");
    private static final Map<String, List<String>> ORDERS = Map.of(
        "shared", REQUEST_ORDER,
        "java-session", List.of(),
        "dotnet-client", List.of());
    private static final Map<String, List<String>> BOUNDARY_LIMITS =
        Map.of(
            "java-session",
            List.of(
                "jsonDepth",
                "rawJsonStringUtf16Units",
                "semanticJsonStringScalars",
                "canonicalInboundBytes",
                "jsonNumberAsciiBytes",
                "hudTextScalars",
                "clientToServerMessageBytes"),
            "dotnet-client",
            List.of(
                "jsonDepth",
                "rawJsonStringUtf16Units",
                "semanticJsonStringScalars",
                "jsonNumberAsciiBytes",
                "serverToDesktopMessageBytes"));
    private static final Set<String> ROLES = Set.of(
        "shared-raw",
        "java-session",
        "dotnet-client");
    private static final Set<String> ERROR_CODES = Set.of(
        "INVALID_REQUEST",
        "UNAUTHORIZED",
        "UNSUPPORTED_VERSION",
        "CAPABILITY_UNAVAILABLE",
        "GAME_NOT_READY",
        "NOT_FOUND",
        "TIMEOUT",
        "RATE_LIMITED",
        "INTERNAL_ERROR");
    private static final Map<String, String> FIXED_ROLES = Map.of(
        "raw/duplicate-key.json.bin", "shared-raw",
        "raw/invalid-utf8.bin", "shared-raw",
        "raw/trailing-token.json.bin", "shared-raw",
        "traces/dotnet-client/receive-and-cancel.json", "dotnet-client",
        "traces/java-session/pair-races.json", "java-session",
        "traces/shared/unknown-action-auth-order.json", "shared-raw");

    @Test
    void raw_manifest_has_canonical_entries_and_byte_hashes()
        throws Exception {
        List<RawFixture> entries = readManifest();
        assertFalse(entries.isEmpty());
        List<String> paths = entries.stream().map(RawFixture::path).toList();
        assertEquals(paths.stream().sorted().toList(), paths);
        assertEquals(
            enumerateRoleFixtureFiles(),
            paths.stream().sorted().toList());

        Set<String> uniquePaths = new HashSet<>();
        for (RawFixture entry : entries) {
            assertEquals(
                Set.of("path", "role", "expected", "sha256"),
                propertyNames(entry.source()));
            assertTrue(ROLES.contains(entry.role()));
            assertFalse(entry.expected().isBlank());
            assertTrue(entry.sha256().matches("^[0-9a-f]{64}$"));
            assertFalse(entry.path().contains("\\"));
            assertFalse(Path.of(entry.path()).isAbsolute());
            assertTrue(uniquePaths.add(entry.path()));
            assertEquals(
                entry.sha256(),
                sha256(resolveFixture(entry.path())));
        }

        Map<String, RawFixture> byPath = indexEntries(entries);
        for (Map.Entry<String, String> expected : FIXED_ROLES.entrySet()) {
            assertEquals(
                expected.getValue(),
                byPath.get(expected.getKey()).role());
        }
    }

    @Test
    void raw_inputs_represent_forbidden_wire_forms() throws Exception {
        byte[] invalidUtf8 = Files.readAllBytes(
            resolveFixture("raw/invalid-utf8.bin"));
        assertThrows(
            CharacterCodingException.class,
            () -> StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(invalidUtf8)));

        ObjectMapper duplicateRejectingMapper = new ObjectMapper(
            JsonFactory.builder()
                .enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION)
                .build());
        assertThrows(
            IOException.class,
            () -> duplicateRejectingMapper.readTree(
                Files.readAllBytes(resolveFixture(
                    "raw/duplicate-key.json.bin"))));

        try (JsonParser parser = MAPPER.createParser(
            Files.readAllBytes(resolveFixture(
                "raw/trailing-token.json.bin")))) {
            assertTrue(parser.readValueAsTree().isObject());
            assertNotNull(parser.nextToken());
        }
    }

    @Test
    void state_traces_are_replayable_and_own_exactly_one_terminal()
        throws Exception {
        List<Trace> traces = readTraces(readManifest());
        assertEquals(
            List.of("dotnet-client", "java-session", "shared"),
            traces.stream().map(Trace::owner).sorted().toList());

        for (Trace trace : traces) {
            assertEquals(trace.owner(), trace.terminalOwner());
            assertEquals(ORDERS.get(trace.owner()), trace.processingOrder());
            assertFalse(trace.cases().isEmpty());
            for (TraceCase testCase : trace.cases()) {
                assertFalse(testCase.name().isBlank());
                assertFalse(testCase.initialState().isBlank());
                assertFalse(testCase.expectedFinalState().isBlank());
                assertFalse(testCase.processingOrder().isEmpty());
                assertFalse(testCase.events().isEmpty());
                for (int index = 0; index < testCase.events().size(); index++) {
                    TraceEvent event = testCase.events().get(index);
                    assertEquals(index + 1, event.sequence());
                    assertFalse(event.type().isBlank());
                    assertTrue(trace.inputs().has(event.inputRef()));
                }
                assertExpectedTerminal(
                    testCase.expectedTerminal(),
                    trace.owner());
            }
        }

        Trace shared = traceByOwner(traces, "shared");
        assertEquals(
            "中文🙂𝄞 \"引号\" \\反斜杠\n换行 § e\u0301",
            shared.inputs().get("unicodeProbe").asText());
        assertTrue(shared.boundaries().isEmpty());

        TraceCase duplicatePair = traceByOwner(traces, "java-session")
            .cases().stream()
            .filter(testCase -> testCase.name().equals("duplicate-pair"))
            .findFirst()
            .orElseThrow();
        assertEquals("PairingPending", duplicatePair.initialState());
        assertEquals("PairingPending", duplicatePair.expectedFinalState());
        assertEquals(
            "RATE_LIMITED",
            duplicatePair.expectedTerminal().errorCode());
        assertEquals(PAIR_ORDER, duplicatePair.processingOrder());
        assertFalse(
            duplicatePair.processingOrder().contains("authentication"));
        TraceCase approval = traceByOwner(traces, "java-session")
            .cases().stream()
            .filter(testCase -> testCase.name().equals("approve-wins"))
            .findFirst()
            .orElseThrow();
        assertEquals(PAIR_ORDER, approval.processingOrder());
        TraceCase invalidToken = traceByOwner(traces, "java-session")
            .cases().stream()
            .filter(testCase -> testCase.name().equals("invalid-token"))
            .findFirst()
            .orElseThrow();
        assertEquals(
            List.of("public-validation", "authentication"),
            invalidToken.processingOrder());
        TraceCase responseWins = traceByOwner(traces, "dotnet-client")
            .cases().stream()
            .filter(testCase -> testCase.name().equals("response-wins"))
            .findFirst()
            .orElseThrow();
        assertEquals(RESPONSE_ORDER, responseWins.processingOrder());
    }

    @Test
    void boundary_traces_reference_measured_raw_bytes_and_exact_limits()
        throws Exception {
        List<RawFixture> manifest = readManifest();
        Map<String, RawFixture> entries = indexEntries(manifest);
        List<Trace> traces = readTraces(manifest);
        Map<String, Long> runtimeLimits = runtimeLimits();
        Set<String> referencedBoundaryPaths = new HashSet<>();

        for (Trace trace : traces) {
            if (!trace.owner().equals("shared")) {
                assertBoundaries(
                    trace,
                    entries,
                    runtimeLimits,
                    referencedBoundaryPaths);
            }
        }

        List<String> manifestedBoundaryPaths = manifest.stream()
            .filter(entry -> entry.expected().startsWith("boundary-"))
            .map(RawFixture::path)
            .sorted()
            .toList();
        assertEquals(
            manifestedBoundaryPaths,
            referencedBoundaryPaths.stream().sorted().toList());
    }

    private static void assertBoundaries(
        Trace trace,
        Map<String, RawFixture> entries,
        Map<String, Long> runtimeLimits,
        Set<String> referencedBoundaryPaths
    ) throws Exception {
        List<String> expectedLimits = BOUNDARY_LIMITS.get(trace.owner())
            .stream()
            .sorted()
            .toList();
        assertEquals(
            expectedLimits,
            trace.boundaries().stream()
                .map(TraceBoundary::limit)
                .distinct()
                .sorted()
                .toList());

        for (String limitName : expectedLimits) {
            long limit = runtimeLimits.get(limitName);
            List<TraceBoundary> boundaries = trace.boundaries().stream()
                .filter(boundary -> boundary.limit().equals(limitName))
                .toList();
            assertEquals(
                List.of("limit-1", "limit", "limit+1"),
                boundaries.stream().map(TraceBoundary::relation).toList());
            assertEquals(
                List.of(limit - 1, limit, limit + 1),
                boundaries.stream().map(TraceBoundary::value).toList());

            for (TraceBoundary boundary : boundaries) {
                boolean directionalLimit =
                    limitName.equals("clientToServerMessageBytes")
                        || limitName.equals(
                            "serverToDesktopMessageBytes");
                assertEquals(
                    directionalLimit
                        ? "websocket-message"
                        : trace.owner().equals("java-session")
                            ? "strict-request-validator"
                            : "strict-response-reader",
                    boundary.entryPoint());
                List<String> expectedOrder = switch (
                    boundary.entryPoint()) {
                    case "strict-request-validator" ->
                        REQUEST_VALIDATOR_ORDER;
                    case "strict-response-reader" -> RESPONSE_ORDER;
                    case "websocket-message" ->
                        trace.owner().equals("java-session")
                            ? REQUEST_TRANSPORT_ORDER
                            : RESPONSE_ORDER;
                    default -> throw new IllegalArgumentException(
                        "Unknown entry point: " + boundary.entryPoint());
                };
                assertEquals(
                    expectedOrder,
                    boundary.processingOrder());
                JsonNode input = trace.inputs().get(boundary.inputRef());
                assertNotNull(input);
                assertEquals("raw-bytes", input.get("kind").asText());
                String relativePath = input.get("path").asText();
                assertTrue(referencedBoundaryPaths.add(relativePath));
                RawFixture entry = entries.get(relativePath);
                assertEquals(trace.owner(), entry.role());
                assertEquals(
                    "boundary-" + boundary.relation(),
                    entry.expected());

                byte[] bytes = Files.readAllBytes(
                    resolveFixture(relativePath));
                assertFragmentPlan(boundary.fragments(), bytes.length);
                if (directionalLimit
                    && !boundary.relation().equals("limit-1")) {
                    assertEquals(2, boundary.fragments().size());
                    assertEquals(
                        limit - 1,
                        boundary.fragments().get(0).length());
                    assertEquals(
                        boundary.value() - (limit - 1),
                        boundary.fragments().get(1).length());
                }
                assertMeasuredValue(
                    trace.owner(),
                    limitName,
                    boundary.value(),
                    bytes);
                if (directionalLimit) {
                    assertTransportPadding(
                        bytes,
                        runtimeLimits.get(
                            "rawJsonStringUtf16Units"),
                        runtimeLimits.get(
                            "semanticJsonStringScalars"));
                }
                assertExpectedTerminal(
                    boundary.expectedTerminal(),
                    trace.owner());

                if (boundary.relation().equals("limit+1")) {
                    if (trace.owner().equals("dotnet-client")) {
                        assertEquals(
                            "close",
                            boundary.expectedTerminal().kind());
                        assertEquals(
                            limitName.equals(
                                "serverToDesktopMessageBytes")
                                ? 1009
                                : 1007,
                            boundary.expectedTerminal().closeCode());
                        assertEquals(
                            limitName.equals(
                                "serverToDesktopMessageBytes")
                                ? "MessageTooLarge"
                                : "MalformedResponse",
                            boundary.expectedTerminal().pendingOutcome());
                    } else if (limitName.equals(
                        "clientToServerMessageBytes")) {
                        assertEquals(
                            1009,
                            boundary.expectedTerminal().closeCode());
                    } else if (Set.of(
                        "jsonDepth",
                        "rawJsonStringUtf16Units",
                        "jsonNumberAsciiBytes").contains(limitName)) {
                        assertEquals(
                            "close",
                            boundary.expectedTerminal().kind());
                        assertEquals(
                            1007,
                            boundary.expectedTerminal().closeCode());
                        assertEquals(
                            "Closed",
                            boundary.expectedFinalState());
                    } else {
                        assertEquals(
                            "INVALID_REQUEST",
                            boundary.expectedTerminal().errorCode());
                    }
                }
            }
        }
    }

    private static void assertFragmentPlan(
        List<Fragment> fragments,
        int byteLength
    ) {
        assertFalse(fragments.isEmpty());
        long offset = 0;
        for (int index = 0; index < fragments.size(); index++) {
            Fragment fragment = fragments.get(index);
            assertEquals(index + 1, fragment.sequence());
            assertEquals(offset, fragment.offset());
            assertTrue(fragment.length() > 0);
            assertEquals(
                index == fragments.size() - 1,
                fragment.endOfMessage());
            offset += fragment.length();
        }
        assertEquals(byteLength, offset);
    }

    private static void assertMeasuredValue(
        String owner,
        String limitName,
        long expected,
        byte[] bytes
    ) throws Exception {
        switch (limitName) {
            case "clientToServerMessageBytes",
                 "serverToDesktopMessageBytes",
                 "canonicalInboundBytes" ->
                assertEquals(expected, bytes.length);
            case "jsonDepth" ->
                assertEquals(expected, measureJsonDepth(bytes));
            case "rawJsonStringUtf16Units" ->
                assertEquals(expected, readProbeString(bytes).length());
            case "semanticJsonStringScalars" -> {
                String value = readProbeString(bytes);
                assertEquals(
                    expected,
                    value.codePointCount(0, value.length()));
            }
            case "jsonNumberAsciiBytes" ->
                assertEquals(expected, measureProbeNumber(bytes));
            case "hudTextScalars" -> {
                assertEquals("java-session", owner);
                String value = MAPPER.readTree(bytes)
                    .get("payload")
                    .get("text")
                    .asText();
                assertEquals(
                    expected,
                    value.codePointCount(0, value.length()));
            }
            default -> throw new IllegalArgumentException(
                "Unknown measured limit: " + limitName);
        }
    }

    private static void assertTransportPadding(
        byte[] bytes,
        long rawUtf16Limit,
        long semanticScalarLimit
    ) throws Exception {
        JsonNode padding = findProperty(
            MAPPER.readTree(bytes),
            "padding");
        assertNotNull(padding);
        assertTrue(padding.isArray());
        assertFalse(padding.isEmpty());
        for (JsonNode element : padding) {
            String value = element.asText();
            assertTrue(value.length() <= rawUtf16Limit);
            assertTrue(
                value.codePointCount(0, value.length())
                    <= semanticScalarLimit);
        }
    }

    private static long measureJsonDepth(byte[] bytes) {
        boolean inString = false;
        boolean escaped = false;
        long depth = 0;
        long maximum = 0;
        for (byte current : bytes) {
            char character = (char) (current & 0xFF);
            if (inString) {
                if (escaped) {
                    escaped = false;
                } else if (character == '\\') {
                    escaped = true;
                } else if (character == '"') {
                    inString = false;
                }
            } else if (character == '"') {
                inString = true;
            } else if (character == '{' || character == '[') {
                depth++;
                maximum = Math.max(maximum, depth);
            } else if (character == '}' || character == ']') {
                depth--;
            }
        }
        assertEquals(0, depth);
        return maximum;
    }

    private static String readProbeString(byte[] bytes) throws Exception {
        JsonNode probe = findProperty(MAPPER.readTree(bytes), "probe");
        assertNotNull(probe);
        assertTrue(probe.isTextual());
        return probe.asText();
    }

    private static long measureProbeNumber(byte[] bytes) {
        String json = new String(bytes, StandardCharsets.UTF_8);
        String marker = "\"probe\":";
        int start = json.indexOf(marker);
        assertTrue(start >= 0);
        start += marker.length();
        int end = start;
        while (end < json.length()
            && Character.isDigit(json.charAt(end))) {
            end++;
        }
        return end - start;
    }

    private static JsonNode findProperty(JsonNode node, String name) {
        if (node.isObject()) {
            if (node.has(name)) {
                return node.get(name);
            }
            var fieldNames = node.fieldNames();
            while (fieldNames.hasNext()) {
                JsonNode found = findProperty(
                    node.get(fieldNames.next()),
                    name);
                if (found != null) {
                    return found;
                }
            }
        } else if (node.isArray()) {
            for (JsonNode child : node) {
                JsonNode found = findProperty(child, name);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private static void assertExpectedTerminal(
        ExpectedTerminal terminal,
        String owner
    ) {
        assertEquals(owner, terminal.owner());
        assertEquals(1, terminal.count());
        if (terminal.kind().equals("response")) {
            assertEquals(
                Set.of(
                    "owner",
                    "count",
                    "kind",
                    "action",
                    "errorCode"),
                propertyNames(terminal.source()));
            assertFalse(terminal.action().isBlank());
            assertEquals(null, terminal.closeCode());
            assertEquals(null, terminal.pendingOutcome());
            if (terminal.errorCode() != null) {
                assertTrue(ERROR_CODES.contains(terminal.errorCode()));
            }
            return;
        }

        assertEquals("close", terminal.kind());
        Set<String> expectedProperties =
            terminal.pendingOutcome() == null
                ? Set.of("owner", "count", "kind", "closeCode")
                : Set.of(
                    "owner",
                    "count",
                    "kind",
                    "closeCode",
                    "pendingOutcome");
        assertEquals(expectedProperties, propertyNames(terminal.source()));
        assertTrue(
            terminal.closeCode() >= 1000
                && terminal.closeCode() <= 4999);
        assertEquals(null, terminal.action());
        assertEquals(null, terminal.errorCode());
    }

    private static List<RawFixture> readManifest() throws Exception {
        JsonNode root = readJson(FIXTURES.resolve("raw/manifest.json"));
        assertEquals(Set.of("fixtures"), propertyNames(root));
        return elements(root.get("fixtures")).stream()
            .map(entry -> new RawFixture(
                entry,
                entry.get("path").asText(),
                entry.get("role").asText(),
                entry.get("expected").asText(),
                entry.get("sha256").asText()))
            .toList();
    }

    private static List<Trace> readTraces(List<RawFixture> manifest)
        throws Exception {
        List<Trace> traces = new ArrayList<>();
        for (RawFixture entry : manifest) {
            if (entry.path().startsWith("traces/")) {
                traces.add(readTrace(entry.path()));
            }
        }
        return traces;
    }

    private static Trace readTrace(String path) throws Exception {
        JsonNode root = readJson(resolveFixture(path));
        assertEquals(
            Set.of(
                "schemaVersion",
                "traceId",
                "owner",
                "terminalOwner",
                "inputs",
                "processingOrder",
                "cases",
                "boundaries"),
            propertyNames(root));
        assertEquals(1, root.get("schemaVersion").asInt());

        List<TraceCase> cases = elements(root.get("cases")).stream()
            .map(element -> new TraceCase(
                element.get("name").asText(),
                element.get("initialState").asText(),
                elements(element.get("processingOrder")).stream()
                    .map(JsonNode::asText)
                    .toList(),
                elements(element.get("events")).stream()
                    .map(event -> new TraceEvent(
                        event.get("sequence").asInt(),
                        event.get("type").asText(),
                        event.get("inputRef").asText()))
                    .toList(),
                element.get("expectedFinalState").asText(),
                readExpectedTerminal(
                    element.get("expectedTerminal"))))
            .toList();
        List<TraceBoundary> boundaries =
            elements(root.get("boundaries")).stream()
                .map(element -> new TraceBoundary(
                    element.get("limit").asText(),
                    element.get("relation").asText(),
                    element.get("value").asLong(),
                    element.get("entryPoint").asText(),
                    elements(element.get("processingOrder")).stream()
                        .map(JsonNode::asText)
                        .toList(),
                    element.get("inputRef").asText(),
                    elements(element.get("fragments")).stream()
                        .map(fragment -> new Fragment(
                            fragment.get("sequence").asInt(),
                            fragment.get("offset").asLong(),
                            fragment.get("length").asLong(),
                            fragment.get("endOfMessage").asBoolean()))
                        .toList(),
                    element.get("expectedFinalState").asText(),
                    readExpectedTerminal(
                        element.get("expectedTerminal"))))
                .toList();
        return new Trace(
            root.get("owner").asText(),
            root.get("terminalOwner").asText(),
            root.get("inputs"),
            elements(root.get("processingOrder")).stream()
                .map(JsonNode::asText)
                .toList(),
            cases,
            boundaries);
    }

    private static ExpectedTerminal readExpectedTerminal(JsonNode expected) {
        return new ExpectedTerminal(
            expected,
            expected.get("owner").asText(),
            expected.get("count").asInt(),
            expected.get("kind").asText(),
            expected.has("action")
                ? expected.get("action").asText()
                : null,
            expected.hasNonNull("errorCode")
                ? expected.get("errorCode").asText()
                : null,
            expected.has("closeCode")
                ? expected.get("closeCode").asInt()
                : null,
            expected.has("pendingOutcome")
                ? expected.get("pendingOutcome").asText()
                : null);
    }

    private static Map<String, Long> runtimeLimits() throws Exception {
        JsonNode root = readJson(ROOT.resolve("protocol/runtime-limits.json"));
        Map<String, Long> limits = new HashMap<>();
        root.fieldNames().forEachRemaining(name -> {
            if (!name.equals("schemaVersion")) {
                limits.put(name, root.get(name).asLong());
            }
        });
        return limits;
    }

    private static Trace traceByOwner(List<Trace> traces, String owner) {
        return traces.stream()
            .filter(trace -> trace.owner().equals(owner))
            .findFirst()
            .orElseThrow();
    }

    private static Map<String, RawFixture> indexEntries(
        List<RawFixture> entries
    ) {
        Map<String, RawFixture> result = new HashMap<>();
        for (RawFixture entry : entries) {
            assertEquals(null, result.put(entry.path(), entry));
        }
        return result;
    }

    private static JsonNode readJson(Path path) throws Exception {
        return MAPPER.readTree(readStrictUtf8(path));
    }

    private static String readStrictUtf8(Path path) throws Exception {
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
        if (text.contains("\r")
            || !text.endsWith("\n")
            || text.endsWith("\n\n")) {
            throw new IOException(
                "Expected LF text with exactly one final LF: " + path);
        }
        return text;
    }

    private static Path resolveFixture(String relativePath) {
        Path resolved = FIXTURES.resolve(relativePath).normalize();
        assertTrue(resolved.startsWith(FIXTURES));
        return resolved;
    }

    private static String sha256(Path path) throws Exception {
        return HexFormat.of().formatHex(
            MessageDigest.getInstance("SHA-256")
                .digest(Files.readAllBytes(path)));
    }

    private static List<String> enumerateRoleFixtureFiles()
        throws Exception {
        List<Path> files = new ArrayList<>();
        try (Stream<Path> rawFiles = Files.list(FIXTURES.resolve("raw"))) {
            rawFiles
                .filter(Files::isRegularFile)
                .filter(path -> !path.getFileName().toString()
                    .equals("manifest.json"))
                .forEach(files::add);
        }
        try (Stream<Path> traceFiles =
            Files.walk(FIXTURES.resolve("traces"))) {
            traceFiles
                .filter(Files::isRegularFile)
                .forEach(files::add);
        }
        return files.stream()
            .map(path -> FIXTURES.relativize(path)
                .toString()
                .replace('\\', '/'))
            .sorted()
            .toList();
    }

    private static Set<String> propertyNames(JsonNode node) {
        Set<String> names = new HashSet<>();
        node.fieldNames().forEachRemaining(names::add);
        return names;
    }

    private static List<JsonNode> elements(JsonNode node) {
        return StreamSupport.stream(node.spliterator(), false).toList();
    }

    private record RawFixture(
        JsonNode source,
        String path,
        String role,
        String expected,
        String sha256) {
    }

    private record Trace(
        String owner,
        String terminalOwner,
        JsonNode inputs,
        List<String> processingOrder,
        List<TraceCase> cases,
        List<TraceBoundary> boundaries) {
    }

    private record TraceCase(
        String name,
        String initialState,
        List<String> processingOrder,
        List<TraceEvent> events,
        String expectedFinalState,
        ExpectedTerminal expectedTerminal) {
    }

    private record TraceEvent(
        int sequence,
        String type,
        String inputRef) {
    }

    private record TraceBoundary(
        String limit,
        String relation,
        long value,
        String entryPoint,
        List<String> processingOrder,
        String inputRef,
        List<Fragment> fragments,
        String expectedFinalState,
        ExpectedTerminal expectedTerminal) {
    }

    private record Fragment(
        int sequence,
        long offset,
        long length,
        boolean endOfMessage) {
    }

    private record ExpectedTerminal(
        JsonNode source,
        String owner,
        int count,
        String kind,
        String action,
        String errorCode,
        Integer closeCode,
        String pendingOutcome) {
    }
}
