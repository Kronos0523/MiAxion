using System.Text.Json;
using System.Text.Json.Nodes;
using Json.Schema;
using MiAxion.Protocol.Runtime;

namespace MiAxion.Protocol.Tests;

public sealed class DiscoverySchemaTests
{
    private static readonly JsonSchema Schema = JsonSchema.FromText(
        StrictUtf8.Decode(
            DiscoverySchemaAuthority.LoadUtf8().Span,
            DiscoverySchemaAuthority.ResourceName));
    private static readonly EvaluationOptions Options = new()
    {
        RequireFormatValidation = true,
        OutputFormat = OutputFormat.Hierarchical
    };

    [Fact]
    public void CanonicalLoopbackDiscoveryRecordIsValid()
    {
        AssertValid(ValidRecord(), expected: true);
    }

    [Theory]
    [InlineData("ws://localhost:32123/miaxion/bridge/v1")]
    [InlineData("ws://user@127.0.0.1:32123/miaxion/bridge/v1")]
    [InlineData("ws://127.0.0.1:32123/miaxion/bridge/v1?query=1")]
    [InlineData("ws://127.0.0.1:32123/miaxion/bridge/v1#fragment")]
    [InlineData("ws://127.0.0.1:0/miaxion/bridge/v1")]
    [InlineData("ws://127.0.0.1:65536/miaxion/bridge/v1")]
    public void NonCanonicalEndpointIsRejected(string endpoint)
    {
        var record = ValidRecord();
        record["endpoint"] = endpoint;

        AssertValid(record, expected: false);
    }

    [Theory]
    [InlineData("2026-07-30T01:02:03Z")]
    [InlineData("2026-07-30T09:02:03.000+08:00")]
    public void CreatedAtRequiresUtcMilliseconds(string createdAt)
    {
        var record = ValidRecord();
        record["createdAt"] = createdAt;

        AssertValid(record, expected: false);
    }

    [Fact]
    public void MissingAndUnknownFieldsAreRejected()
    {
        var missing = ValidRecord();
        missing.Remove("instanceId");
        var unknown = ValidRecord();
        unknown["surprise"] = true;

        AssertValid(missing, expected: false);
        AssertValid(unknown, expected: false);
    }

    private static JsonObject ValidRecord() => new()
    {
        ["schemaVersion"] = 1,
        ["instanceId"] = "b8b88ff4-36c0-4bdc-a5c1-8a976dc7d2cf",
        ["processId"] = 4242,
        ["processStartEpochMs"] = 1785373200000,
        ["createdAt"] = "2026-07-30T01:00:00.000Z",
        ["endpoint"] =
            "ws://127.0.0.1:32123/miaxion/bridge/v1",
        ["subprotocol"] = "miaxion.bridge.v1",
        ["protocolVersion"] = "1.0",
        ["modVersion"] = "0.1.0",
        ["minecraftVersion"] = "1.21.1"
    };

    private static void AssertValid(JsonObject instance, bool expected)
    {
        using var document = JsonDocument.Parse(instance.ToJsonString());
        var result = Schema.Evaluate(document.RootElement, Options);

        Assert.True(
            result.IsValid == expected,
            JsonSerializer.Serialize(result));
    }
}
