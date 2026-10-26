using MiAxion.Protocol.Contracts;
using MiAxion.Protocol.Serialization;

namespace MiAxion.Protocol.Tests;

public sealed class EntityDescribeContractTests
{
    [Fact]
    public void BrowserDirectoryRoundTripsNativeVariantsWithoutChangingRegistryRequests()
    {
        var request = new EntityResolveRequest("气体", [], "custom", null, "browser");
        var restored = ProtocolJson.Deserialize<EntityResolveRequest>(ProtocolJson.Serialize(request));
        Assert.Equal("browser", restored.Scope);
        Assert.Equal("custom", restored.Kind);
        using var variant = System.Text.Json.JsonDocument.Parse("{\"variant\":\"native-a\"}");
        var response = new EntityResolveResponse("r1",
            [new("custom", "fixture:gas", "fixture:gas", "气体", "fixture:chemical", variant.RootElement)], false);
        var match = ProtocolJson.Deserialize<EntityResolveResponse>(ProtocolJson.Serialize(response)).Matches[0];
        Assert.Equal("fixture:chemical", match.IngredientTypeUid);
        Assert.Equal("native-a", match.Components!.Value.GetProperty("variant").GetString());
        Assert.DoesNotContain("scope", ProtocolJson.Serialize(new EntityResolveRequest("气体", [], "fluid")));
        Assert.Throws<ArgumentException>(() => new EntityResolveRequest("气体", [], "custom"));
    }

    [Fact]
    public void FluidResolveIsAnExplicitExtensionAndDoesNotExpandLegacyDescribe()
    {
        var request = new EntityResolveRequest("熔融黄铜", [], "fluid");
        Assert.Equal("fluid", ProtocolJson.Deserialize<EntityResolveRequest>(ProtocolJson.Serialize(request)).Kind);
        var response = new EntityResolveResponse("r1", [new("fluid", "fixture:brass", "fluid.brass", "熔融黄铜")], false);
        Assert.Equal("fluid", ProtocolJson.Deserialize<EntityResolveResponse>(ProtocolJson.Serialize(response)).Matches[0].Kind);
        Assert.Throws<ArgumentException>(() => new EntityReference("fluid", "fixture:brass"));
    }

    [Fact]
    public void EntityPagesRoundTripWithoutChangingLegacyShape()
    {
        var request = new EntityResolveRequest("Shared", [], "item", "page_2-A");
        Assert.Equal("page_2-A", ProtocolJson.Deserialize<EntityResolveRequest>(ProtocolJson.Serialize(request)).Continuation);
        var matches = Enumerable.Range(0, 8).Select(i => new EntityResolveMatch("item", $"mod:item_{i}", "item.shared", "Shared")).ToArray();
        var response = new EntityResolveResponse("r1", matches, true, "page_2-A");
        Assert.Equal("page_2-A", ProtocolJson.Deserialize<EntityResolveResponse>(ProtocolJson.Serialize(response)).NextContinuation);
        Assert.DoesNotContain("continuation", ProtocolJson.Serialize(new EntityResolveRequest("Shared", [])), StringComparison.OrdinalIgnoreCase);
        Assert.DoesNotContain("continuation", ProtocolJson.Serialize(new EntityResolveResponse("r1", [], false)), StringComparison.OrdinalIgnoreCase);
        Assert.Throws<ArgumentException>(() => new EntityResolveResponse("r1", matches, false, "page_2-A"));
        Assert.Throws<ArgumentException>(() => new EntityResolveResponse("r1", matches[..1], true, "page_2-A"));
        foreach (var invalid in new[] { "", "bad token", "token=", "中文", new string('a', 257) })
            Assert.Throws<ArgumentException>(() => new EntityResolveRequest("Shared", [], "item", invalid));
    }

    [Fact]
    public void ResolveKindIsOptionalWithoutChangingLegacyWireShape()
    {
        var legacy = new EntityResolveRequest("机械压力机", ["create"]);
        var legacyJson = ProtocolJson.Serialize(legacy);
        Assert.DoesNotContain("kind", legacyJson, StringComparison.Ordinal);
        Assert.Equal("any", legacy.EffectiveKind);

        var item = new EntityResolveRequest("机械压力机", [], "item");
        var restored = ProtocolJson.Deserialize<EntityResolveRequest>(
            ProtocolJson.Serialize(item));
        Assert.Equal("item", restored.Kind);
        Assert.Equal("item", restored.EffectiveKind);
    }

    [Fact]
    public void DescribeRequestRejectsAnyAndDuplicateReferences()
    {
        Assert.Throws<ArgumentException>(() =>
            new EntityReference("any", "create:mechanical_press"));

        var reference = new EntityReference("item", "create:mechanical_press");
        Assert.Throws<ArgumentException>(() =>
            new EntityDescribeRequest([reference, reference]));
    }

    [Fact]
    public void DescribeResponseRejectsOverlappingDescriptionAndMissing()
    {
        var reference = new EntityReference("item", "create:mechanical_press");
        var description = new EntityDescription(
            reference.Kind,
            reference.ResourceId,
            "block.create.mechanical_press",
            "Mechanical Press");

        Assert.Throws<ArgumentException>(() =>
            new EntityDescribeResponse("r1", [description], [reference]));
    }

    [Theory]
    [InlineData("line\nfeed")]
    [InlineData("\u202Ereversed")]
    public void EntityNamesRejectControlsBidiAndInvalidUtf16(string value)
    {
        Assert.Throws<ArgumentException>(() =>
            new EntityDescription(
                "item",
                "create:mechanical_press",
                "block.create.mechanical_press",
                value));
    }

    [Fact]
    public void EntityNamesRejectUnpairedUtf16Surrogate()
    {
        var invalid = new string('\uD800', 1);
        Assert.Throws<ArgumentException>(() =>
            new EntityDescription(
                "item",
                "create:mechanical_press",
                "block.create.mechanical_press",
                invalid));
    }

    [Fact]
    public void EntityNamesKeepEmojiVariationSelectorAndJoiner()
    {
        var description = new EntityDescription(
            "item",
            "create:mechanical_press",
            "block.create.mechanical_press",
            "🙂️‍🔧");

        Assert.Equal("🙂️‍🔧", description.DisplayName);
    }
}
