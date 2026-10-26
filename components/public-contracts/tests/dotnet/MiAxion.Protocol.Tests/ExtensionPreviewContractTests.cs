using System.Text.Json;
using System.Text.Json.Nodes;
using Json.Schema;
using MiAxion.Protocol.Contracts;
using MiAxion.Protocol.Serialization;

namespace MiAxion.Protocol.Tests;

/// <summary>Shared list/call fixtures reused by the production codec.</summary>
public sealed class ExtensionPreviewContractTests
{
    private static readonly string Root = Path.Combine(Path.GetDirectoryName(
        SchemaFixtureTests.FixturePath("manifest.json"))!, "0ae");

    [Fact]
    public void SharedListAndCallFixturesValidateAndEchoExactCorrelation()
    {
        var registry = new SchemaRegistry { Fetch = (_, _) => null };
        var schema = JsonSchema.FromText(File.ReadAllText(Path.Combine(Root, "extensions.schema.json")),
            new BuildOptions { SchemaRegistry = registry }, null);
        using var corpus = JsonDocument.Parse(File.ReadAllText(Path.Combine(Root, "exchanges.json")));
        var cases = corpus.RootElement.GetProperty("exchanges").EnumerateArray().ToArray();
        Assert.Equal(5, cases.Length);
        foreach (var exchange in cases)
        {
            var request = exchange.GetProperty("request");
            var response = exchange.GetProperty("response");
            Assert.True(schema.Evaluate(request).IsValid, exchange.GetProperty("name").GetString());
            Assert.True(schema.Evaluate(response).IsValid, exchange.GetProperty("name").GetString());
            foreach (var field in new[] { "protocolVersion", "requestId", "deadline", "action" })
                Assert.Equal(request.GetProperty(field).GetString(), response.GetProperty(field).GetString());
            var decoded = response.Deserialize<ResponseEnvelope>(ProtocolJson.Options)!;
            Assert.Equal(request.GetProperty("deadline").GetDateTimeOffset(), decoded.Deadline);
            Assert.Equal(JsonValueKind.Null, response.GetProperty("sessionToken").ValueKind);
            Assert.True(decoded.Action.TryGetKnown(out _));
            if (decoded.Error is null)
            {
                if (decoded.Action == new ProtocolAction("extensions.list"))
                    Assert.NotNull(decoded.Payload!.Value.Deserialize<ExtensionListResponse>(ProtocolJson.Options));
                else Assert.NotNull(decoded.Payload!.Value.Deserialize<ExtensionCallResponse>(ProtocolJson.Options));
            }
            Assert.True(request.GetRawText().Length < 65536);
        }
        var page1 = cases[0].GetProperty("response").GetProperty("payload");
        var page2 = cases[1].GetProperty("response").GetProperty("payload");
        Assert.Equal(page1.GetProperty("catalogRevision").GetString(), page2.GetProperty("catalogRevision").GetString());
        Assert.Equal(page1.GetProperty("nextCursor").GetString(), cases[1].GetProperty("request").GetProperty("payload").GetProperty("cursor").GetString());
        Assert.Equal(JsonValueKind.Null, page2.GetProperty("nextCursor").ValueKind);
    }

    [Theory]
    [InlineData("unknown-field")]
    [InlineData("wrong-version")]
    [InlineData("unknown-status")]
    [InlineData("failure-with-data")]
    public void UnsupportedOrContradictoryEnvelopesAreRejected(string mutation)
    {
        var schema = JsonSchema.FromText(File.ReadAllText(Path.Combine(Root, "extensions.schema.json")),
            new BuildOptions { SchemaRegistry = new SchemaRegistry { Fetch = (_, _) => null } }, null);
        var corpus = JsonNode.Parse(File.ReadAllText(Path.Combine(Root, "exchanges.json")))!;
        var response = corpus["exchanges"]![2]!["response"]!.DeepClone();
        switch (mutation)
        {
            case "unknown-field": response["unknown"] = true; break;
            case "wrong-version": response["protocolVersion"] = "2.0"; break;
            case "unknown-status": response["payload"]!["status"] = "invented"; break;
            case "failure-with-data": response["payload"]!["status"] = "EXECUTION_FAILED"; break;
        }
        using var document = JsonDocument.Parse(response.ToJsonString());
        Assert.False(schema.Evaluate(document.RootElement).IsValid);
    }
}
