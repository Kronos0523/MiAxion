using System.Text;
using System.Text.Json;
using System.Text.Json.Nodes;
using MiAxion.Protocol.Runtime;

namespace MiAxion.Protocol.Tests;

public sealed class RuntimeLimitsConsistencyTests
{
    private static readonly ProtocolRuntimeLimits Expected = new()
    {
        SchemaVersion = 1,
        HttpUpgradeHeaderBytes = 8192,
        HandshakeTimeoutMs = 5000,
        UnpairedFirstRequestTimeoutMs = 5000,
        ClientToServerMessageBytes = 65536,
        ServerToDesktopMessageBytes = 1048576,
        RecipeResponseMessageBytes = 67108864,
        PairingDeadlineMs = 60000,
        ActionDeadlineMs = 10000,
        RecipeActionDeadlineMs = 30000,
        QueryDeadlineMaximumMs = 1800000,
        AuthenticatedIdleTimeoutMs = 120000,
        ConnectionInFlight = 8,
        RecipeInFlight = 2,
        PairingPending = 1,
        JsonDepth = 32,
        RawJsonStringUtf16Units = 32768,
        SemanticJsonStringScalars = 16384,
        CanonicalInboundBytes = 65536,
        JsonNumberAsciiBytes = 64,
        HudTextScalars = 280,
        HudQueueMessages = 4,
        OutboundQueueMessages = 16,
        OutboundQueueBytes = 2097152,
        RecipeOutboundQueueBytes = 134217728,
        OutboundUnwritableTimeoutMs = 5000,
        DesktopHeartbeatMs = 30000,
        OrdinaryRatePerMinute = 600,
        OrdinaryBurst = 128,
        HealthRatePerMinute = 120,
        HealthBurst = 32,
        HudRatePerMinute = 60,
        HudBurst = 16,
        PairRatePerMinute = 3,
        PairBurst = 3,
        CancelRatePerMinute = 120,
        CancelBurst = 32,
        WorldQueryResponseMessageBytes = 4194304,
        WorldQueryLocatorDeadlineMs = 30000,
        WorldQueryNearbyDeadlineMs = 10000,
        WorldQueryLocatorInFlight = 1,
        WorldQueryNearbyInFlight = 1,
        WorldQueryLocatorRatePerMinute = 30,
        WorldQueryLocatorBurst = 8,
        WorldQueryNearbyRatePerMinute = 120,
        WorldQueryNearbyBurst = 32,
        WorldQueryChunkMaximum = 25,
        WorldQueryVerticalBlocksMaximum = 101,
        WorldQueryBlockEntityMaximum = 256,
        WorldQueryContainerSlotMaximum = 256,
        WorldQueryContainerStackMaximum = 1024,
        WorldQueryMachineMaximum = 128,
        WorldQueryMachineFeatureMaximum = 32,
        WorldQueryTargetMaximum = 128,
        WorldQueryStructurePieceMaximum = 256,
        WorldQueryNameCandidateMaximum = 8,
        WorldQueryStringScalarsMaximum = 256,
        WorldQueryTickBudgetMs = 2,
        WorldQueryWorkUnitsPerTick = 8
    };

    [Fact]
    public void SourceEmbeddedResourceAndPublicModelAreIdentical()
    {
        var sourceBytes = File.ReadAllBytes(RuntimeLimitsPath());
        var source = ProtocolRuntimeLimitsLoader.Parse(sourceBytes);
        var embedded = ProtocolRuntimeLimitsLoader.Load();

        Assert.Equal(Expected, source);
        Assert.Equal(Expected, embedded);
        Assert.Equal(
            59,
            typeof(ProtocolRuntimeLimits).GetProperties().Length);
    }

    [Fact]
    public void LoaderRejectsMissingUnknownAndDuplicateProperties()
    {
        var source = File.ReadAllText(
            RuntimeLimitsPath(),
            new UTF8Encoding(false, true));
        var missing = JsonNode.Parse(source)!.AsObject();
        missing.Remove("hudBurst");
        var unknown = JsonNode.Parse(source)!.AsObject();
        unknown["surprise"] = 1;
        var duplicate = source.Replace(
            "\"schemaVersion\": 1,",
            "\"schemaVersion\": 1,\n  \"schemaVersion\": 1,",
            StringComparison.Ordinal);

        Assert.Throws<JsonException>(
            () => ProtocolRuntimeLimitsLoader.Parse(
                Encoding.UTF8.GetBytes(missing.ToJsonString())));
        Assert.Throws<JsonException>(
            () => ProtocolRuntimeLimitsLoader.Parse(
                Encoding.UTF8.GetBytes(unknown.ToJsonString())));
        Assert.Throws<JsonException>(
            () => ProtocolRuntimeLimitsLoader.Parse(
                Encoding.UTF8.GetBytes(duplicate)));
    }

    [Fact]
    public void LoaderRejectsEncodingTrailingAndNumericCoercion()
    {
        var source = File.ReadAllBytes(RuntimeLimitsPath());
        var text = Encoding.UTF8.GetString(source);
        var bom = new byte[source.Length + 3];
        bom[0] = 0xef;
        bom[1] = 0xbb;
        bom[2] = 0xbf;
        source.CopyTo(bom, 3);
        byte[] WithHudBurst<T>(T value)
        {
            var malformed = JsonNode.Parse(source)!.AsObject();
            malformed["hudBurst"] = JsonValue.Create(value);
            return Encoding.UTF8.GetBytes(malformed.ToJsonString());
        }

        Assert.ThrowsAny<JsonException>(
            () => ProtocolRuntimeLimitsLoader.Parse([0xff]));
        Assert.ThrowsAny<JsonException>(
            () => ProtocolRuntimeLimitsLoader.Parse(bom));
        Assert.ThrowsAny<JsonException>(
            () => ProtocolRuntimeLimitsLoader.Parse(
                Encoding.UTF8.GetBytes(text + "{}")));
        Assert.ThrowsAny<JsonException>(
            () => ProtocolRuntimeLimitsLoader.Parse(
                WithHudBurst(3.5)));
        Assert.ThrowsAny<JsonException>(
            () => ProtocolRuntimeLimitsLoader.Parse(
                WithHudBurst("16")));
    }

    private static string RuntimeLimitsPath()
    {
        var directory = new DirectoryInfo(AppContext.BaseDirectory);
        while (directory is not null &&
            !File.Exists(Path.Combine(directory.FullName, "protocol", "runtime-limits.json")))
        {
            directory = directory.Parent;
        }

        return directory is not null
            ? Path.Combine(
                directory.FullName,
                "protocol",
                "runtime-limits.json")
            : throw new DirectoryNotFoundException(
                "Could not locate the repository root.");
    }
}
