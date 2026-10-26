using System.Globalization;
using System.Text;
using System.Text.Json;
using System.Text.Json.Serialization;
using System.Text.RegularExpressions;

namespace MiAxion.Protocol.Tests;

public sealed class ProtocolCorpusStructureTests
{
    private const string SessionToken =
        "QmFzZTY0dXJsLXNlc3Npb24tdG9rZW4tdjEtMzItYnl0ZXM";
    private const string UnicodeHudText =
        "中文语音助手🙂 §a绿色 \"引号\" \\反斜杠\n换行";

    private static readonly string[] Actions =
    [
        "session.pair.request",
        "session.pair.approved",
        "session.revoke",
        "request.cancel",
        "capabilities.list",
        "extensions.list",
        "extensions.call",
        "recipe.query",
        "recipe.uses",
        "quests.query",
        "quests.detail",
        "inventory.snapshot",
        "player.snapshot",
        "entities.resolve",
        "entities.describe",
        "world.structure.locate",
        "world.biome.locate",
        "world.machines.scan",
        "world.containers.scan",
        "world.structure.targets",
        "world.registry.resolve",
        "world.registry.describe",
        "hud.show",
        "health.ping"
    ];

    private static readonly string[] ErrorCodes =
    [
        "INVALID_REQUEST",
        "UNAUTHORIZED",
        "UNSUPPORTED_VERSION",
        "CAPABILITY_UNAVAILABLE",
        "GAME_NOT_READY",
        "NOT_FOUND",
        "TIMEOUT",
        "RATE_LIMITED",
        "DETAILS_HIDDEN",
        "RESULT_TOO_LARGE",
        "INTERNAL_ERROR"
    ];

    private static readonly string[] Schemas =
    [
        "request.schema.json",
        "response.schema.json"
    ];

    private static readonly string[] Expectations =
    [
        "valid",
        "invalid"
    ];

    private static readonly string[] InvalidReasons =
    [
        "malformed-action",
        "unknown-action-success",
        "unknown-action-wrong-error",
        "additional-property",
        "missing-required",
        "null-required",
        "wrong-case",
        "unknown-enum",
        "invalid-format",
        "out-of-range",
        "invalid-token",
        "deadline-order",
        "inventory-exclusive-fields",
        "recipe-empty-alternatives",
        "recipe-slot-level-count",
        "recipe-tag-surrogate",
        "recipe-unknown-role",
        "recipe-missing-required-role"
    ];

    internal static readonly JsonSerializerOptions Options = new()
    {
        PropertyNamingPolicy = JsonNamingPolicy.CamelCase,
        PropertyNameCaseInsensitive = false,
        UnmappedMemberHandling = JsonUnmappedMemberHandling.Disallow,
        RespectRequiredConstructorParameters = true,
        RespectNullableAnnotations = true
    };

    private static string ProtocolDir => GetProtocolDir();
    private static string FixturesDir => Path.Combine(ProtocolDir, "fixtures");
    private static string ManifestPath => Path.Combine(FixturesDir, "manifest.json");

    private static string GetProtocolDir()
    {
        var dir = Path.GetDirectoryName(
            typeof(ProtocolCorpusStructureTests).Assembly.Location);

        while (dir is not null && Path.GetFileName(dir) != "dotnet")
        {
            dir = Path.GetDirectoryName(dir);
        }

        return dir is not null
            ? Path.GetFullPath(Path.Combine(dir, "..", "..", "protocol"))
            : throw new InvalidOperationException(
                "Could not locate protocol directory");
    }

    private static FixtureManifest ReadManifest()
    {
        Assert.True(
            File.Exists(ManifestPath),
            $"Manifest not found at {ManifestPath}");

        return JsonSerializer.Deserialize<FixtureManifest>(
                StrictUtf8.ReadAllText(ManifestPath),
                Options)
            ?? throw new InvalidDataException(
                $"Manifest deserialized to null: {ManifestPath}");
    }

    private static string ResolveFixturePath(string relativePath) =>
        Path.Combine(
            FixturesDir,
            relativePath.Replace('/', Path.DirectorySeparatorChar));

    [Fact]
    public void ManifestHasStrictShapeAndCanonicalEntries()
    {
        var manifest = ReadManifest();
        Assert.NotEmpty(manifest.Fixtures);
        Assert.Equal(
            manifest.Fixtures
                .Select(fixture => fixture.Path)
                .OrderBy(path => path, StringComparer.Ordinal),
            manifest.Fixtures.Select(fixture => fixture.Path));

        var paths = new HashSet<string>(StringComparer.Ordinal);
        foreach (var fixture in manifest.Fixtures)
        {
            Assert.False(string.IsNullOrWhiteSpace(fixture.Path));
            Assert.Contains(fixture.Schema, Schemas);
            Assert.Contains(fixture.Expected, Expectations);
            Assert.Matches(
                new Regex(
                    "^[a-z0-9]+(?:-[a-z0-9]+)*$",
                    RegexOptions.CultureInvariant),
                fixture.Reason);

            Assert.DoesNotContain('\\', fixture.Path);
            Assert.False(Path.IsPathRooted(fixture.Path));

            var segments = fixture.Path.Split('/');
            Assert.Equal(2, segments.Length);
            Assert.Equal(fixture.Expected, segments[0]);
            Assert.Matches(
                new Regex(
                    "^[a-z0-9]+(?:-[a-z0-9]+)*\\.json$",
                    RegexOptions.CultureInvariant),
                segments[1]);
            Assert.True(
                paths.Add(fixture.Path),
                $"Duplicate manifest path: {fixture.Path}");

            var resolved = Path.GetFullPath(ResolveFixturePath(fixture.Path));
            var fixturesRoot = Path.GetFullPath(FixturesDir) +
                Path.DirectorySeparatorChar;
            var comparison = OperatingSystem.IsWindows()
                ? StringComparison.OrdinalIgnoreCase
                : StringComparison.Ordinal;
            Assert.StartsWith(fixturesRoot, resolved, comparison);
        }

        Assert.Contains(manifest.Fixtures, fixture => fixture.Expected == "valid");
        Assert.Contains(
            manifest.Fixtures,
            fixture => fixture.Expected == "invalid");
    }

    [Fact]
    public void ManifestPathsExactlyMatchFixtureFiles()
    {
        var manifestPaths = ReadManifest().Fixtures
            .Select(fixture => fixture.Path)
            .OrderBy(path => path, StringComparer.Ordinal)
            .ToArray();
        var filePaths = Expectations
            .SelectMany(expected =>
                Directory.EnumerateFiles(
                        Path.Combine(FixturesDir, expected),
                        "*.json",
                        SearchOption.TopDirectoryOnly)
                    .Select(path =>
                        $"{expected}/{Path.GetFileName(path)}"))
            .OrderBy(path => path, StringComparer.Ordinal)
            .ToArray();

        Assert.Equal(filePaths, manifestPaths);
    }

    [Fact]
    public void ManifestAndFixturesAreStrictUtf8LfJson()
    {
        var manifest = ReadManifest();
        var paths = manifest.Fixtures
            .Select(fixture => ResolveFixturePath(fixture.Path))
            .Prepend(ManifestPath);

        foreach (var path in paths)
        {
            using var document = JsonDocument.Parse(
                StrictUtf8.ReadAllText(path));
            Assert.Equal(JsonValueKind.Object, document.RootElement.ValueKind);
        }
    }

    [Fact]
    public void StrictUtf8RejectsForbiddenEncodingsAndLineEndings()
    {
        var validJson = Encoding.UTF8.GetBytes("{}\n");
        var cases = new (string Name, byte[] Content)[]
        {
            ("bom", [0xEF, 0xBB, 0xBF, .. validJson]),
            ("invalid-utf8", [0x7B, 0xFF, 0x7D, 0x0A]),
            ("crlf", Encoding.UTF8.GetBytes("{}\r\n")),
            ("bare-cr", Encoding.UTF8.GetBytes("{}\r")),
            ("missing-final-lf", Encoding.UTF8.GetBytes("{}")),
            ("multiple-final-lf", Encoding.UTF8.GetBytes("{}\n\n"))
        };

        foreach (var testCase in cases)
        {
            var path = Path.Combine(
                Path.GetTempPath(),
                $"miaxion-strict-utf8-{testCase.Name}-{Guid.NewGuid():N}.json");
            try
            {
                File.WriteAllBytes(path, testCase.Content);
                Assert.Throws<InvalidDataException>(() =>
                {
                    _ = StrictUtf8.ReadAllText(path);
                });
            }
            finally
            {
                File.Delete(path);
            }
        }
    }

    [Fact]
    public void CorpusCoversActionsErrorsTokensAndInvalidCategories()
    {
        var manifest = ReadManifest();
        var knownActions = Actions.ToHashSet(StringComparer.Ordinal);
        var requestActions = new HashSet<string>(StringComparer.Ordinal);
        var responseActions = new HashSet<string>(StringComparer.Ordinal);
        var errorCodes = new HashSet<string>(StringComparer.Ordinal);

        foreach (var fixture in manifest.Fixtures)
        {
            using var document = JsonDocument.Parse(
                StrictUtf8.ReadAllText(ResolveFixturePath(fixture.Path)));
            var root = document.RootElement;

            if (root.TryGetProperty("action", out var actionNode) &&
                actionNode.ValueKind == JsonValueKind.String &&
                actionNode.GetString() is { } knownAction &&
                knownActions.Contains(knownAction))
            {
                if (fixture.Schema == "request.schema.json")
                {
                    requestActions.Add(knownAction);
                }
                else
                {
                    responseActions.Add(knownAction);
                }
            }

            if (fixture.Schema == "response.schema.json")
            {
                Assert.Equal(
                    JsonValueKind.Null,
                    root.GetProperty("sessionToken").ValueKind);

                if (fixture.Expected == "valid" &&
                    root.GetProperty("error") is
                        { ValueKind: JsonValueKind.Object } error)
                {
                    errorCodes.Add(
                        error.GetProperty("code").GetString()!);
                }
            }

            if (fixture.Schema == "request.schema.json" &&
                fixture.Expected == "valid")
            {
                var requestAction = root.GetProperty("action").GetString()!;
                var token = root.GetProperty("sessionToken");
                if (requestAction == "session.pair.request")
                {
                    Assert.Equal(JsonValueKind.Null, token.ValueKind);
                }
                else
                {
                    Assert.Equal(SessionToken, token.GetString());
                }

                var timestamp = DateTimeOffset.Parse(
                    root.GetProperty("timestamp").GetString()!,
                    CultureInfo.InvariantCulture);
                var deadline = DateTimeOffset.Parse(
                    root.GetProperty("deadline").GetString()!,
                    CultureInfo.InvariantCulture);
                Assert.True(
                    timestamp < deadline,
                    $"Valid request deadline is not ordered: {fixture.Path}");
            }
        }

        Assert.Equal(Actions, requestActions.OrderByAction(Actions));
        Assert.Equal(Actions, responseActions.OrderByAction(Actions));
        Assert.Equal(ErrorCodes, errorCodes.OrderByAction(ErrorCodes));
        Assert.Contains(
            manifest.Fixtures,
            fixture =>
                fixture.Path == "valid/unknown-action-request.json" &&
                fixture.Expected == "valid");
        Assert.Contains(
            manifest.Fixtures,
            fixture =>
                fixture.Path ==
                    "valid/unknown-action-capability-unavailable-response.json" &&
                fixture.Expected == "valid");

        foreach (var reason in InvalidReasons)
        {
            Assert.Contains(
                manifest.Fixtures,
                fixture =>
                    fixture.Expected == "invalid" &&
                    fixture.Reason == reason);
        }

        var hudFixture = manifest.Fixtures.Single(
            fixture => fixture.Path == "valid/hud-request.json");
        using var hudDocument = JsonDocument.Parse(
            StrictUtf8.ReadAllText(ResolveFixturePath(hudFixture.Path)));
        Assert.Equal(
            UnicodeHudText,
            hudDocument.RootElement
                .GetProperty("payload")
                .GetProperty("text")
                .GetString());
    }
}

internal static class CorpusSetExtensions
{
    internal static string[] OrderByAction(
        this IEnumerable<string> values,
        IReadOnlyList<string> authority)
    {
        var indexes = authority
            .Select((value, index) => (value, index))
            .ToDictionary(pair => pair.value, pair => pair.index);

        return values
            .OrderBy(value => indexes[value])
            .ToArray();
    }
}

internal sealed record FixtureManifest(
    IReadOnlyList<FixtureEntry> Fixtures);

internal sealed record FixtureEntry(
    string Path,
    string Schema,
    string Expected,
    string Reason);
