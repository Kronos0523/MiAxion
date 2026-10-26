using System.Globalization;
using System.Text.Json;
using Json.Schema;

namespace MiAxion.Protocol.Tests;

public sealed class SchemaFixtureTests
{
    private static readonly string ProtocolDir = GetProtocolDir();
    private static readonly string SchemaDir =
        Path.Combine(ProtocolDir, "schema");
    private static readonly string FixturesDir =
        Path.Combine(ProtocolDir, "fixtures");
    private static readonly SchemaRegistry Registry = new()
    {
        Fetch = (_, _) => null
    };
    private static readonly BuildOptions BuildOptions = new()
    {
        SchemaRegistry = Registry
    };
    private static readonly EvaluationOptions EvaluationOptions = new()
    {
        RequireFormatValidation = true,
        OutputFormat = OutputFormat.Hierarchical
    };
    private static readonly Dictionary<string, JsonSchema> Schemas =
        LoadSchemas();

    [Fact]
    public void ManifestFixturesMatchSchemaExpectations()
    {
        var manifest = JsonSerializer.Deserialize<FixtureManifest>(
                StrictUtf8.ReadAllText(
                    Path.Combine(FixturesDir, "manifest.json")),
                ProtocolCorpusStructureTests.Options)
            ?? throw new InvalidDataException(
                "Fixture manifest deserialized to null.");

        Assert.NotEmpty(manifest.Fixtures);

        foreach (var entry in manifest.Fixtures)
        {
            var fixturePath = Path.Combine(
                FixturesDir,
                entry.Path.Replace('/', Path.DirectorySeparatorChar));
            using var instance = JsonDocument.Parse(
                StrictUtf8.ReadAllText(fixturePath));

            var result = Evaluate(entry.Schema, instance.RootElement);
            var effectiveValid = result.IsValid &&
                (entry.Schema != "request.schema.json" ||
                    RequestTimeIsStrictlyOrdered(instance.RootElement));
            var expectedValid = entry.Expected == "valid";

            Assert.True(
                effectiveValid == expectedValid,
                $"path={entry.Path}{Environment.NewLine}" +
                $"schema={entry.Schema}{Environment.NewLine}" +
                $"reason={entry.Reason}{Environment.NewLine}" +
                $"expected={entry.Expected}{Environment.NewLine}" +
                $"evaluation={JsonSerializer.Serialize(result)}");
        }
    }

    internal static EvaluationResults Evaluate(
        string schemaName,
        JsonElement instance)
    {
        if (!Schemas.TryGetValue(schemaName, out var schema))
        {
            throw new InvalidOperationException(
                $"Unknown fixture schema: {schemaName}");
        }

        return schema.Evaluate(instance, EvaluationOptions);
    }

    internal static bool RequestTimeIsStrictlyOrdered(JsonElement instance)
    {
        if (!instance.TryGetProperty("timestamp", out var timestampElement) ||
            !instance.TryGetProperty("deadline", out var deadlineElement) ||
            !DateTimeOffset.TryParse(
                timestampElement.GetString(),
                CultureInfo.InvariantCulture,
                DateTimeStyles.AssumeUniversal |
                    DateTimeStyles.AdjustToUniversal,
                out var timestamp) ||
            !DateTimeOffset.TryParse(
                deadlineElement.GetString(),
                CultureInfo.InvariantCulture,
                DateTimeStyles.AssumeUniversal |
                    DateTimeStyles.AdjustToUniversal,
                out var deadline))
        {
            return false;
        }

        return timestamp < deadline;
    }

    internal static string FixturePath(string relativePath) =>
        Path.Combine(
            FixturesDir,
            relativePath.Replace('/', Path.DirectorySeparatorChar));

    private static Dictionary<string, JsonSchema> LoadSchemas()
    {
        var common = LoadSchema("common.schema.json");
        Registry.Register(common);
        Registry.Register(LoadSchema("world-query.schema.json"));
        Registry.Register(LoadSchema("extensions.schema.json"));
        Registry.Register(LoadSchema("quest.schema.json"));
        Registry.Register(LoadSchema("player-observation.schema.json"));
        Registry.Register(LoadSchema("recipe.schema.json"));
        Registry.Register(LoadSchema("request-envelope.schema.json"));

        return new Dictionary<string, JsonSchema>(StringComparer.Ordinal)
        {
            ["request.schema.json"] = LoadSchema("request.schema.json"),
            ["response.schema.json"] = LoadSchema("response.schema.json")
        };
    }

    private static JsonSchema LoadSchema(string fileName) =>
        JsonSchema.FromText(
            StrictUtf8.ReadAllText(Path.Combine(SchemaDir, fileName)),
            BuildOptions,
            null);

    private static string GetProtocolDir()
    {
        var directory = Path.GetDirectoryName(
            typeof(SchemaFixtureTests).Assembly.Location);

        while (directory is not null &&
            Path.GetFileName(directory) != "dotnet")
        {
            directory = Path.GetDirectoryName(directory);
        }

        return directory is not null
            ? Path.GetFullPath(
                Path.Combine(directory, "..", "..", "protocol"))
            : throw new InvalidOperationException(
                "Could not locate protocol directory.");
    }

    private sealed record FixtureManifest(
        IReadOnlyList<FixtureEntry> Fixtures);

    private sealed record FixtureEntry(
        string Path,
        string Schema,
        string Expected,
        string Reason);
}
