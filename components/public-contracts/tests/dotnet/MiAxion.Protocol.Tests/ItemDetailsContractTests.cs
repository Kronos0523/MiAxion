using System.Text.Json;
using System.Text.Json.Nodes;

namespace MiAxion.Protocol.Tests;

public sealed class ItemDetailsContractTests
{
    [Fact]
    public void VersionedConfigurationUsesClosedSettingsAndOnlyV2ResponseAcceptsDetails()
    {
        var envelope = JsonNode.Parse("""
            {"protocolVersion":"1.0","requestId":"11111111-1111-1111-1111-111111111111",
             "timestamp":"2026-10-01T00:00:00Z","deadline":"2026-10-01T00:00:05Z",
             "action":"terminology.v2.configure","sessionToken":null,"error":null,
             "payload":{"settings":{"enabled":true,"sources":["hotbar"],"capacity":64,"browserWindowSeconds":180,"retentionSeconds":1800}}}
            """)!;
        envelope["sessionToken"] = new string('A', 43);
        Assert.True(Valid("request.schema.json", envelope));
        envelope["payload"]!["settings"]!["extra"] = true;
        Assert.False(Valid("request.schema.json", envelope));
        envelope["sessionToken"] = null;
        envelope["payload"] = JsonNode.Parse("""
            {"producerGeneration":1,"revision":1,"language":"en_us",
             "captureSettings":{"enabled":true,"sources":["hotbar"],"capacity":64,"browserWindowSeconds":180,"retentionSeconds":1800},
             "entries":[],"browserGroups":[],"sourceAvailability":{},"truncated":false,
             "itemDetails":{"identity":{"kind":"item","resourceId":"minecraft:potion"},"displayName":"Potion","source":"hotbar",
              "observedAtUtc":"2026-10-01T00:00:00Z","tooltipStatus":"complete","tooltip":["Pack author text"],
              "effectsStatus":"complete","effects":[{"resourceId":"minecraft:speed","amplifier":1,"durationKind":"timed","durationTicks":200}]}}
            """);
        Assert.True(Valid("response.schema.json", envelope));
        envelope["action"] = "terminology.configure";
        Assert.False(Valid("response.schema.json", envelope));
        envelope["action"] = "terminology.v2.configure";
        envelope["payload"]!["itemDetails"]!["effects"]![0]!["durationKind"] = "infinite";
        Assert.False(Valid("response.schema.json", envelope));
    }

    private static bool Valid(string schema, JsonNode value)
    {
        using var document = JsonDocument.Parse(value.ToJsonString());
        return SchemaFixtureTests.Evaluate(schema, document.RootElement).IsValid;
    }
}
