using System.Security.Cryptography;
using System.Text;
using System.Text.Json;
using System.Text.RegularExpressions;

namespace MiAxion.Protocol.Tests;

public sealed class RawProtocolCorpusTests
{
    private static readonly string[] RequestOrder =
    [
        "public-validation",
        "authentication",
        "rate-limit",
        "action-payload-validation"
    ];

    private static readonly string[] PairOrder =
    [
        "public-validation",
        "pair-rate-limit",
        "pair-payload-validation",
        "pairing-state-transition"
    ];

    private static readonly string[] RequestValidatorOrder =
    [
        "strict-utf8",
        "single-json-object",
        "read-constraints",
        "public-validation",
        "authentication",
        "rate-limit",
        "action-payload-validation"
    ];

    private static readonly string[] RequestTransportOrder =
    [
        "frame-type",
        "transport-size",
        "strict-utf8",
        "single-json-object",
        "read-constraints",
        "public-validation",
        "authentication",
        "rate-limit",
        "action-payload-validation"
    ];

    private static readonly string[] ResponseOrder =
    [
        "frame-type",
        "transport-size",
        "strict-utf8",
        "single-json-object",
        "correlation",
        "response-validation"
    ];

    private static readonly Dictionary<string, string[]> Orders =
        new Dictionary<string, string[]>(StringComparer.Ordinal)
        {
            ["shared"] = RequestOrder,
            ["java-session"] = [],
            ["dotnet-client"] = []
        };

    private static readonly Dictionary<string, string[]>
        BoundaryLimits =
            new Dictionary<string, string[]>(StringComparer.Ordinal)
            {
                ["java-session"] =
                [
                    "jsonDepth",
                    "rawJsonStringUtf16Units",
                    "semanticJsonStringScalars",
                    "canonicalInboundBytes",
                    "jsonNumberAsciiBytes",
                    "hudTextScalars",
                    "clientToServerMessageBytes"
                ],
                ["dotnet-client"] =
                [
                    "jsonDepth",
                    "rawJsonStringUtf16Units",
                    "semanticJsonStringScalars",
                    "jsonNumberAsciiBytes",
                    "serverToDesktopMessageBytes"
                ]
            };

    private static readonly HashSet<string> Roles = new(
        ["shared-raw", "java-session", "dotnet-client"],
        StringComparer.Ordinal);

    private static readonly HashSet<string> ErrorCodes = new(
        [
            "INVALID_REQUEST",
            "UNAUTHORIZED",
            "UNSUPPORTED_VERSION",
            "CAPABILITY_UNAVAILABLE",
            "GAME_NOT_READY",
            "NOT_FOUND",
            "TIMEOUT",
            "RATE_LIMITED",
            "INTERNAL_ERROR"
        ],
        StringComparer.Ordinal);

    private static readonly IReadOnlyDictionary<string, string> FixedRoles =
        new Dictionary<string, string>(StringComparer.Ordinal)
        {
            ["raw/duplicate-key.json.bin"] = "shared-raw",
            ["raw/invalid-utf8.bin"] = "shared-raw",
            ["raw/trailing-token.json.bin"] = "shared-raw",
            ["traces/dotnet-client/receive-and-cancel.json"] =
                "dotnet-client",
            ["traces/java-session/pair-races.json"] = "java-session",
            ["traces/shared/unknown-action-auth-order.json"] = "shared-raw"
        };

    private static string ProtocolDir => GetProtocolDir();
    private static string FixturesDir => Path.Combine(ProtocolDir, "fixtures");
    private static string RawManifestPath =>
        Path.Combine(FixturesDir, "raw", "manifest.json");

    [Fact]
    public void RawManifestHasCanonicalEntriesAndByteHashes()
    {
        var manifest = ReadRawManifest();
        Assert.NotEmpty(manifest.Fixtures);
        Assert.Equal(
            manifest.Fixtures
                .Select(fixture => fixture.Path)
                .OrderBy(path => path, StringComparer.Ordinal),
            manifest.Fixtures.Select(fixture => fixture.Path));

        var manifestPaths = new HashSet<string>(StringComparer.Ordinal);
        foreach (var fixture in manifest.Fixtures)
        {
            Assert.Equal(
                new HashSet<string>(
                    ["path", "role", "expected", "sha256"],
                    StringComparer.Ordinal),
                fixture.Source.EnumerateObject()
                    .Select(property => property.Name)
                    .ToHashSet(StringComparer.Ordinal));
            Assert.Contains(fixture.Role, Roles);
            Assert.False(string.IsNullOrWhiteSpace(fixture.Expected));
            Assert.Matches(
                new Regex(
                    "^[0-9a-f]{64}$",
                    RegexOptions.CultureInvariant),
                fixture.Sha256);
            Assert.DoesNotContain('\\', fixture.Path);
            Assert.False(Path.IsPathRooted(fixture.Path));
            Assert.True(
                manifestPaths.Add(fixture.Path),
                $"Duplicate raw manifest path: {fixture.Path}");

            var path = ResolveFixturePath(fixture.Path);
            Assert.True(File.Exists(path), $"Fixture not found: {path}");
            Assert.Equal(
                fixture.Sha256,
                Convert.ToHexString(
                        SHA256.HashData(File.ReadAllBytes(path)))
                    .ToLowerInvariant());
        }

        foreach (var expected in FixedRoles)
        {
            Assert.Equal(
                expected.Value,
                manifest.Fixtures
                    .Single(fixture => fixture.Path == expected.Key)
                    .Role);
        }

        Assert.Equal(
            EnumerateRoleFixtureFiles(),
            manifestPaths.OrderBy(path => path, StringComparer.Ordinal));
    }

    [Fact]
    public void RawInputsRepresentForbiddenWireForms()
    {
        var invalidUtf8 = File.ReadAllBytes(
            ResolveFixturePath("raw/invalid-utf8.bin"));
        Assert.Throws<DecoderFallbackException>(() =>
        {
            _ = new UTF8Encoding(false, true).GetString(invalidUtf8);
        });

        var duplicateKey = File.ReadAllBytes(
            ResolveFixturePath("raw/duplicate-key.json.bin"));
        Assert.True(HasDuplicateRootProperty(duplicateKey));

        var trailingToken = File.ReadAllBytes(
            ResolveFixturePath("raw/trailing-token.json.bin"));
        Assert.ThrowsAny<JsonException>(() =>
        {
            using var _ = JsonDocument.Parse(trailingToken);
        });
    }

    [Fact]
    public void StateTracesAreReplayableAndOwnExactlyOneTerminal()
    {
        var manifest = ReadRawManifest();
        var traces = ReadTraces(manifest);
        Assert.Equal(
            ["dotnet-client", "java-session", "shared"],
            traces
                .Select(trace => trace.Owner)
                .OrderBy(owner => owner, StringComparer.Ordinal));

        foreach (var trace in traces)
        {
            Assert.Equal(trace.Owner, trace.TerminalOwner);
            Assert.Equal(Orders[trace.Owner], trace.ProcessingOrder);
            Assert.NotEmpty(trace.Cases);
            foreach (var testCase in trace.Cases)
            {
                Assert.False(string.IsNullOrWhiteSpace(testCase.Name));
                Assert.False(string.IsNullOrWhiteSpace(
                    testCase.InitialState));
                Assert.False(string.IsNullOrWhiteSpace(
                    testCase.ExpectedFinalState));
                Assert.NotEmpty(testCase.ProcessingOrder);
                Assert.NotEmpty(testCase.Events);
                Assert.Equal(
                    Enumerable.Range(1, testCase.Events.Count),
                    testCase.Events.Select(@event => @event.Sequence));
                foreach (var @event in testCase.Events)
                {
                    Assert.False(string.IsNullOrWhiteSpace(@event.Type));
                    Assert.True(
                        trace.Inputs.TryGetProperty(
                            @event.InputRef,
                            out _),
                        $"Missing inputRef '{@event.InputRef}'");
                }

                AssertExpectedTerminal(
                    testCase.ExpectedTerminal,
                    trace.Owner);
            }
        }

        var shared = traces.Single(trace => trace.Owner == "shared");
        Assert.Equal(
            "中文🙂𝄞 \"引号\" \\反斜杠\n换行 § e\u0301",
            shared.Inputs.GetProperty("unicodeProbe").GetString());
        Assert.Empty(shared.Boundaries);

        var duplicatePair = traces
            .Single(trace => trace.Owner == "java-session")
            .Cases.Single(testCase => testCase.Name == "duplicate-pair");
        Assert.Equal("PairingPending", duplicatePair.InitialState);
        Assert.Equal("PairingPending", duplicatePair.ExpectedFinalState);
        Assert.Equal(
            "RATE_LIMITED",
            duplicatePair.ExpectedTerminal.ErrorCode);
        Assert.Equal(PairOrder, duplicatePair.ProcessingOrder);
        Assert.DoesNotContain(
            "authentication",
            duplicatePair.ProcessingOrder);

        var approval = traces
            .Single(trace => trace.Owner == "java-session")
            .Cases.Single(testCase => testCase.Name == "approve-wins");
        Assert.Equal(PairOrder, approval.ProcessingOrder);
        var invalidToken = traces
            .Single(trace => trace.Owner == "java-session")
            .Cases.Single(testCase => testCase.Name == "invalid-token");
        Assert.Equal(
            ["public-validation", "authentication"],
            invalidToken.ProcessingOrder);
        var responseWins = traces
            .Single(trace => trace.Owner == "dotnet-client")
            .Cases.Single(testCase => testCase.Name == "response-wins");
        Assert.Equal(ResponseOrder, responseWins.ProcessingOrder);
    }

    [Fact]
    public void BoundaryTracesReferenceMeasuredRawBytesAndExactLimits()
    {
        var manifest = ReadRawManifest();
        var entries = manifest.Fixtures.ToDictionary(
            fixture => fixture.Path,
            StringComparer.Ordinal);
        var traces = ReadTraces(manifest);
        var runtimeLimits = ReadRuntimeLimits();
        var referencedBoundaryPaths = new HashSet<string>(
            StringComparer.Ordinal);

        foreach (var trace in traces.Where(
            trace => trace.Owner != "shared"))
        {
            AssertBoundaries(
                trace,
                entries,
                runtimeLimits,
                referencedBoundaryPaths);
        }

        var manifestedBoundaryPaths = manifest.Fixtures
            .Where(fixture => fixture.Expected.StartsWith(
                "boundary-",
                StringComparison.Ordinal))
            .Select(fixture => fixture.Path)
            .OrderBy(path => path, StringComparer.Ordinal);
        Assert.Equal(
            manifestedBoundaryPaths,
            referencedBoundaryPaths.OrderBy(
                path => path,
                StringComparer.Ordinal));
    }

    private static void AssertBoundaries(
        Trace trace,
        Dictionary<string, RawFixture> entries,
        Dictionary<string, long> runtimeLimits,
        HashSet<string> referencedBoundaryPaths)
    {
        var expectedLimits = BoundaryLimits[trace.Owner]
            .OrderBy(name => name, StringComparer.Ordinal)
            .ToArray();
        Assert.Equal(
            expectedLimits,
            trace.Boundaries
                .Select(boundary => boundary.Limit)
                .Distinct(StringComparer.Ordinal)
                .OrderBy(name => name, StringComparer.Ordinal));

        foreach (var limitName in expectedLimits)
        {
            var limit = runtimeLimits[limitName];
            var boundaries = trace.Boundaries
                .Where(boundary => boundary.Limit == limitName)
                .ToArray();
            Assert.Equal(
                ["limit-1", "limit", "limit+1"],
                boundaries.Select(boundary => boundary.Relation));
            Assert.Equal(
                [limit - 1, limit, limit + 1],
                boundaries.Select(boundary => boundary.Value));

            foreach (var boundary in boundaries)
            {
                var directionalLimit =
                    limitName == "clientToServerMessageBytes" ||
                    limitName == "serverToDesktopMessageBytes";
                Assert.Equal(
                    directionalLimit
                        ? "websocket-message"
                        : trace.Owner == "java-session"
                            ? "strict-request-validator"
                            : "strict-response-reader",
                    boundary.EntryPoint);
                Assert.Equal(
                    boundary.EntryPoint switch
                    {
                        "strict-request-validator" =>
                            RequestValidatorOrder,
                        "strict-response-reader" => ResponseOrder,
                        "websocket-message"
                            when trace.Owner == "java-session" =>
                                RequestTransportOrder,
                        "websocket-message" => ResponseOrder,
                        _ => throw new InvalidOperationException(
                            $"Unknown entry point: {boundary.EntryPoint}")
                    },
                    boundary.ProcessingOrder);
                Assert.True(
                    trace.Inputs.TryGetProperty(
                        boundary.InputRef,
                        out var input));
                Assert.Equal(
                    "raw-bytes",
                    input.GetProperty("kind").GetString());
                var relativePath = input.GetProperty("path").GetString()!;
                Assert.True(referencedBoundaryPaths.Add(relativePath));
                var entry = entries[relativePath];
                Assert.Equal(trace.Owner, entry.Role);
                Assert.Equal(
                    $"boundary-{boundary.Relation}",
                    entry.Expected);

                var bytes = File.ReadAllBytes(
                    ResolveFixturePath(relativePath));
                AssertFragmentPlan(boundary.Fragments, bytes.Length);
                if (directionalLimit &&
                    boundary.Relation != "limit-1")
                {
                    Assert.Equal(2, boundary.Fragments.Count);
                    Assert.Equal(
                        limit - 1,
                        boundary.Fragments[0].Length);
                    Assert.Equal(
                        boundary.Value - (limit - 1),
                        boundary.Fragments[1].Length);
                }
                AssertMeasuredValue(
                    trace.Owner,
                    limitName,
                    boundary.Value,
                    bytes);
                if (directionalLimit)
                {
                    AssertTransportPadding(
                        bytes,
                        runtimeLimits[
                            "rawJsonStringUtf16Units"],
                        runtimeLimits[
                            "semanticJsonStringScalars"]);
                }
                AssertExpectedTerminal(
                    boundary.ExpectedTerminal,
                    trace.Owner);

                if (boundary.Relation == "limit+1")
                {
                    if (trace.Owner == "dotnet-client")
                    {
                        Assert.Equal(
                            "close",
                            boundary.ExpectedTerminal.Kind);
                        Assert.Equal(
                            limitName == "serverToDesktopMessageBytes"
                                ? 1009
                                : 1007,
                            boundary.ExpectedTerminal.CloseCode);
                        Assert.Equal(
                            limitName == "serverToDesktopMessageBytes"
                                ? "MessageTooLarge"
                                : "MalformedResponse",
                            boundary.ExpectedTerminal.PendingOutcome);
                    }
                    else if (limitName ==
                        "clientToServerMessageBytes")
                    {
                        Assert.Equal(
                            1009,
                            boundary.ExpectedTerminal.CloseCode);
                    }
                    else if (limitName is
                        "jsonDepth" or
                        "rawJsonStringUtf16Units" or
                        "jsonNumberAsciiBytes")
                    {
                        Assert.Equal(
                            "close",
                            boundary.ExpectedTerminal.Kind);
                        Assert.Equal(
                            1007,
                            boundary.ExpectedTerminal.CloseCode);
                        Assert.Equal(
                            "Closed",
                            boundary.ExpectedFinalState);
                    }
                    else
                    {
                        Assert.Equal(
                            "INVALID_REQUEST",
                            boundary.ExpectedTerminal.ErrorCode);
                    }
                }
            }
        }
    }

    private static void AssertFragmentPlan(
        IReadOnlyList<Fragment> fragments,
        int byteLength)
    {
        Assert.NotEmpty(fragments);
        long offset = 0;
        for (var index = 0; index < fragments.Count; index++)
        {
            var fragment = fragments[index];
            Assert.Equal(index + 1, fragment.Sequence);
            Assert.Equal(offset, fragment.Offset);
            Assert.True(fragment.Length > 0);
            Assert.Equal(
                index == fragments.Count - 1,
                fragment.EndOfMessage);
            offset += fragment.Length;
        }
        Assert.Equal(byteLength, offset);
    }

    private static void AssertMeasuredValue(
        string owner,
        string limitName,
        long expected,
        byte[] bytes)
    {
        switch (limitName)
        {
            case "clientToServerMessageBytes":
            case "serverToDesktopMessageBytes":
            case "canonicalInboundBytes":
                Assert.Equal(expected, bytes.LongLength);
                return;
            case "jsonDepth":
                Assert.Equal(expected, MeasureJsonDepth(bytes));
                return;
            case "rawJsonStringUtf16Units":
                Assert.Equal(
                    expected,
                    ReadProbeString(bytes).Length);
                return;
            case "semanticJsonStringScalars":
                Assert.Equal(
                    expected,
                    ReadProbeString(bytes).EnumerateRunes().Count());
                return;
            case "jsonNumberAsciiBytes":
                Assert.Equal(expected, MeasureProbeNumber(bytes));
                return;
            case "hudTextScalars":
                Assert.Equal("java-session", owner);
                using (var document = JsonDocument.Parse(bytes))
                {
                    Assert.Equal(
                        expected,
                        document.RootElement
                            .GetProperty("payload")
                            .GetProperty("text")
                            .GetString()!
                            .EnumerateRunes()
                            .Count());
                }
                return;
            default:
                throw new InvalidOperationException(
                    $"Unknown measured limit: {limitName}");
        }
    }

    private static void AssertTransportPadding(
        byte[] bytes,
        long rawUtf16Limit,
        long semanticScalarLimit)
    {
        using var document = JsonDocument.Parse(bytes);
        var padding = FindProperty(
            document.RootElement,
            "padding");
        Assert.Equal(JsonValueKind.Array, padding.ValueKind);
        Assert.NotEmpty(padding.EnumerateArray());
        foreach (var element in padding.EnumerateArray())
        {
            var value = element.GetString()!;
            Assert.True(value.Length <= rawUtf16Limit);
            Assert.True(
                value.EnumerateRunes().Count() <=
                    semanticScalarLimit);
        }
    }

    private static long MeasureJsonDepth(ReadOnlySpan<byte> bytes)
    {
        var reader = new Utf8JsonReader(
            bytes,
            new JsonReaderOptions
            {
                MaxDepth = 128
            });
        long maxDepth = 0;
        while (reader.Read())
        {
            maxDepth = Math.Max(maxDepth, reader.CurrentDepth);
        }
        return maxDepth;
    }

    private static string ReadProbeString(byte[] bytes)
    {
        using var document = JsonDocument.Parse(
            bytes,
            new JsonDocumentOptions
            {
                MaxDepth = 128
            });
        return FindProperty(document.RootElement, "probe").GetString()!;
    }

    private static long MeasureProbeNumber(ReadOnlySpan<byte> bytes)
    {
        var reader = new Utf8JsonReader(bytes);
        while (reader.Read())
        {
            if (reader.TokenType == JsonTokenType.PropertyName &&
                reader.ValueTextEquals("probe"))
            {
                Assert.True(reader.Read());
                Assert.Equal(JsonTokenType.Number, reader.TokenType);
                return reader.HasValueSequence
                    ? reader.ValueSequence.Length
                    : reader.ValueSpan.Length;
            }
        }
        throw new InvalidDataException("probe number not found");
    }

    private static JsonElement FindProperty(
        JsonElement element,
        string propertyName)
    {
        if (element.ValueKind == JsonValueKind.Object)
        {
            if (element.TryGetProperty(propertyName, out var value))
            {
                return value;
            }
            foreach (var property in element.EnumerateObject())
            {
                var found = FindProperty(property.Value, propertyName);
                if (found.ValueKind != JsonValueKind.Undefined)
                {
                    return found;
                }
            }
        }
        else if (element.ValueKind == JsonValueKind.Array)
        {
            foreach (var item in element.EnumerateArray())
            {
                var found = FindProperty(item, propertyName);
                if (found.ValueKind != JsonValueKind.Undefined)
                {
                    return found;
                }
            }
        }
        return default;
    }

    private static void AssertExpectedTerminal(
        ExpectedTerminal terminal,
        string owner)
    {
        Assert.Equal(owner, terminal.Owner);
        Assert.Equal(1, terminal.Count);
        if (terminal.Kind == "response")
        {
            Assert.Equal(
                new HashSet<string>(
                    [
                        "owner",
                        "count",
                        "kind",
                        "action",
                        "errorCode"
                    ],
                    StringComparer.Ordinal),
                terminal.Source.EnumerateObject()
                    .Select(property => property.Name)
                    .ToHashSet(StringComparer.Ordinal));
            Assert.False(string.IsNullOrWhiteSpace(terminal.Action));
            Assert.Null(terminal.CloseCode);
            Assert.Null(terminal.PendingOutcome);
            if (terminal.ErrorCode is not null)
            {
                Assert.Contains(terminal.ErrorCode, ErrorCodes);
            }
            return;
        }

        Assert.Equal("close", terminal.Kind);
        var expectedProperties = terminal.PendingOutcome is null
            ? new HashSet<string>(
                ["owner", "count", "kind", "closeCode"],
                StringComparer.Ordinal)
            : new HashSet<string>(
                [
                    "owner",
                    "count",
                    "kind",
                    "closeCode",
                    "pendingOutcome"
                ],
                StringComparer.Ordinal);
        Assert.Equal(
            expectedProperties,
            terminal.Source.EnumerateObject()
                .Select(property => property.Name)
                .ToHashSet(StringComparer.Ordinal));
        Assert.InRange(terminal.CloseCode!.Value, 1000, 4999);
        Assert.Null(terminal.Action);
        Assert.Null(terminal.ErrorCode);
    }

    private static RawManifest ReadRawManifest()
    {
        Assert.True(
            File.Exists(RawManifestPath),
            $"Raw manifest not found at {RawManifestPath}");
        using var document = JsonDocument.Parse(
            StrictUtf8.ReadAllText(RawManifestPath));
        Assert.Equal(
            ["fixtures"],
            document.RootElement.EnumerateObject()
                .Select(property => property.Name));
        return new RawManifest(
            document.RootElement.GetProperty("fixtures")
                .EnumerateArray()
                .Select(element => new RawFixture(
                    element.Clone(),
                    element.GetProperty("path").GetString()!,
                    element.GetProperty("role").GetString()!,
                    element.GetProperty("expected").GetString()!,
                    element.GetProperty("sha256").GetString()!))
                .ToArray());
    }

    private static Trace[] ReadTraces(RawManifest manifest) =>
        manifest.Fixtures
            .Where(fixture => fixture.Path.StartsWith(
                "traces/",
                StringComparison.Ordinal))
            .Select(fixture => ReadTrace(fixture.Path))
            .ToArray();

    private static Trace ReadTrace(string relativePath)
    {
        using var document = JsonDocument.Parse(
            StrictUtf8.ReadAllText(ResolveFixturePath(relativePath)));
        var root = document.RootElement;
        Assert.Equal(
            new HashSet<string>(
                [
                    "schemaVersion",
                    "traceId",
                    "owner",
                    "terminalOwner",
                    "inputs",
                    "processingOrder",
                    "cases",
                    "boundaries"
                ],
                StringComparer.Ordinal),
            root.EnumerateObject()
                .Select(property => property.Name)
                .ToHashSet(StringComparer.Ordinal));
        Assert.Equal(1, root.GetProperty("schemaVersion").GetInt32());

        var cases = root.GetProperty("cases")
            .EnumerateArray()
            .Select(element => new TraceCase(
                element.GetProperty("name").GetString()!,
                element.GetProperty("initialState").GetString()!,
                element.GetProperty("processingOrder")
                    .EnumerateArray()
                    .Select(stage => stage.GetString()!)
                    .ToArray(),
                element.GetProperty("events")
                    .EnumerateArray()
                    .Select(@event => new TraceEvent(
                        @event.GetProperty("sequence").GetInt32(),
                        @event.GetProperty("type").GetString()!,
                        @event.GetProperty("inputRef").GetString()!))
                    .ToArray(),
                element.GetProperty("expectedFinalState").GetString()!,
                ReadExpectedTerminal(
                    element.GetProperty("expectedTerminal"))))
            .ToArray();
        var boundaries = root.GetProperty("boundaries")
            .EnumerateArray()
            .Select(element => new TraceBoundary(
                element.GetProperty("limit").GetString()!,
                element.GetProperty("relation").GetString()!,
                element.GetProperty("value").GetInt64(),
                element.GetProperty("entryPoint").GetString()!,
                element.GetProperty("processingOrder")
                    .EnumerateArray()
                    .Select(stage => stage.GetString()!)
                    .ToArray(),
                element.GetProperty("inputRef").GetString()!,
                element.GetProperty("fragments")
                    .EnumerateArray()
                    .Select(fragment => new Fragment(
                        fragment.GetProperty("sequence").GetInt32(),
                        fragment.GetProperty("offset").GetInt64(),
                        fragment.GetProperty("length").GetInt64(),
                        fragment.GetProperty("endOfMessage")
                            .GetBoolean()))
                    .ToArray(),
                element.GetProperty("expectedFinalState").GetString()!,
                ReadExpectedTerminal(
                    element.GetProperty("expectedTerminal"))))
            .ToArray();

        return new Trace(
            root.GetProperty("owner").GetString()!,
            root.GetProperty("terminalOwner").GetString()!,
            root.GetProperty("inputs").Clone(),
            root.GetProperty("processingOrder")
                .EnumerateArray()
                .Select(element => element.GetString()!)
                .ToArray(),
            cases,
            boundaries);
    }

    private static ExpectedTerminal ReadExpectedTerminal(JsonElement expected)
    {
        var kind = expected.GetProperty("kind").GetString()!;
        return new ExpectedTerminal(
            expected.Clone(),
            expected.GetProperty("owner").GetString()!,
            expected.GetProperty("count").GetInt32(),
            kind,
            expected.TryGetProperty("action", out var action)
                ? action.GetString()
                : null,
            expected.TryGetProperty("errorCode", out var errorCode) &&
                errorCode.ValueKind != JsonValueKind.Null
                ? errorCode.GetString()
                : null,
            expected.TryGetProperty("closeCode", out var closeCode)
                ? closeCode.GetInt32()
                : null,
            expected.TryGetProperty(
                "pendingOutcome",
                out var pendingOutcome)
                ? pendingOutcome.GetString()
                : null);
    }

    private static Dictionary<string, long> ReadRuntimeLimits()
    {
        using var document = JsonDocument.Parse(
            StrictUtf8.ReadAllText(
                Path.Combine(ProtocolDir, "runtime-limits.json")));
        return document.RootElement
            .EnumerateObject()
            .Where(property => property.Name != "schemaVersion")
            .ToDictionary(
                property => property.Name,
                property => property.Value.GetInt64(),
                StringComparer.Ordinal);
    }

    private static bool HasDuplicateRootProperty(ReadOnlySpan<byte> json)
    {
        var reader = new Utf8JsonReader(json);
        var names = new HashSet<string>(StringComparer.Ordinal);
        while (reader.Read())
        {
            if (reader.TokenType == JsonTokenType.PropertyName &&
                reader.CurrentDepth == 1 &&
                !names.Add(reader.GetString()!))
            {
                return true;
            }
        }
        return false;
    }

    private static string[] EnumerateRoleFixtureFiles()
    {
        var rawFiles = Directory.EnumerateFiles(
                Path.Combine(FixturesDir, "raw"),
                "*",
                SearchOption.TopDirectoryOnly)
            .Where(path => Path.GetFileName(path) != "manifest.json");
        var traceFiles = Directory.EnumerateFiles(
            Path.Combine(FixturesDir, "traces"),
            "*.json",
            SearchOption.AllDirectories);
        return rawFiles
            .Concat(traceFiles)
            .Select(path => Path.GetRelativePath(FixturesDir, path)
                .Replace('\\', '/'))
            .OrderBy(path => path, StringComparer.Ordinal)
            .ToArray();
    }

    private static string ResolveFixturePath(string relativePath)
    {
        var path = Path.GetFullPath(Path.Combine(
            FixturesDir,
            relativePath.Replace('/', Path.DirectorySeparatorChar)));
        var root = Path.GetFullPath(FixturesDir) +
            Path.DirectorySeparatorChar;
        var comparison = OperatingSystem.IsWindows()
            ? StringComparison.OrdinalIgnoreCase
            : StringComparison.Ordinal;
        Assert.StartsWith(root, path, comparison);
        return path;
    }

    private static string GetProtocolDir()
    {
        var dir = Path.GetDirectoryName(
            typeof(RawProtocolCorpusTests).Assembly.Location);
        while (dir is not null && Path.GetFileName(dir) != "dotnet")
        {
            dir = Path.GetDirectoryName(dir);
        }
        return dir is not null
            ? Path.GetFullPath(Path.Combine(dir, "..", "..", "protocol"))
            : throw new InvalidOperationException(
                "Could not locate protocol directory");
    }

    private sealed record RawManifest(IReadOnlyList<RawFixture> Fixtures);

    private sealed record RawFixture(
        JsonElement Source,
        string Path,
        string Role,
        string Expected,
        string Sha256);

    private sealed record Trace(
        string Owner,
        string TerminalOwner,
        JsonElement Inputs,
        IReadOnlyList<string> ProcessingOrder,
        IReadOnlyList<TraceCase> Cases,
        IReadOnlyList<TraceBoundary> Boundaries);

    private sealed record TraceCase(
        string Name,
        string InitialState,
        IReadOnlyList<string> ProcessingOrder,
        IReadOnlyList<TraceEvent> Events,
        string ExpectedFinalState,
        ExpectedTerminal ExpectedTerminal);

    private sealed record TraceEvent(
        int Sequence,
        string Type,
        string InputRef);

    private sealed record TraceBoundary(
        string Limit,
        string Relation,
        long Value,
        string EntryPoint,
        IReadOnlyList<string> ProcessingOrder,
        string InputRef,
        IReadOnlyList<Fragment> Fragments,
        string ExpectedFinalState,
        ExpectedTerminal ExpectedTerminal);

    private sealed record Fragment(
        int Sequence,
        long Offset,
        long Length,
        bool EndOfMessage);

    private sealed record ExpectedTerminal(
        JsonElement Source,
        string Owner,
        int Count,
        string Kind,
        string? Action,
        string? ErrorCode,
        int? CloseCode,
        string? PendingOutcome);
}
