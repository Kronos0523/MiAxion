using System.Globalization;
using System.Text.Json;
using MiAxion.Protocol.Contracts;
using MiAxion.Protocol.Serialization;

namespace MiAxion.Protocol.Tests;

public sealed class DtoRoundTripTests
{
    [Fact]
    public void RecipeSlotPreservesMoreThanSixtyFourBoundedAlternatives()
    {
        var alternatives = Enumerable.Range(0, 65)
            .Select(index => new RecipeAlternativeDto($"test:item_{index:D3}", 1))
            .ToArray();

        var slot = new RecipeSlotDto(alternatives);

        Assert.Equal(65, slot.Alternatives.Count);
        Assert.Throws<ArgumentOutOfRangeException>(() => new RecipeSlotDto(
            Enumerable.Repeat(
                alternatives[0],
                ProtocolConstants.RecipeSlotAlternativesMaximum + 1).ToArray()));
    }

    private static readonly Guid RequestId =
        Guid.Parse("b8b88ff4-36c0-4bdc-a5c1-8a976dc7d2cf");
    private static readonly Guid TargetRequestId =
        Guid.Parse("9c84c889-4766-412d-8c5a-235dd7d52cb4");
    private static readonly DateTimeOffset Timestamp =
        DateTimeOffset.Parse(
            "2026-07-26T05:30:00Z",
            CultureInfo.InvariantCulture);
    private static readonly DateTimeOffset Deadline =
        DateTimeOffset.Parse(
            "2026-07-26T05:30:03Z",
            CultureInfo.InvariantCulture);
    private const string Token =
        "QmFzZTY0dXJsLXNlc3Npb24tdG9rZW4tdjEtMzItYnl0ZXM";
    private const string UnicodeHudText =
        "中文语音助手🙂 §a绿色 \"引号\" \\反斜杠\n换行";

    [Theory]
    [InlineData("recommended-direct")]
    [InlineData("planning-direct")]
    public void RecipeSelectionV12CarriesTargetComponentsAndCompletionMetadata(string mode)
    {
        using var components = JsonDocument.Parse(
            "{\"minecraft:custom_data\":{\"tier\":2}}");
        var request = new RecipeQueryRequest(
            "minecraft:stick",
            components.RootElement.Clone(),
            "subset-components",
            new RecipeSelectionRequest(mode, 3));
        var requestJson = ProtocolJson.Serialize(request);
        var restoredRequest = ProtocolJson.Deserialize<RecipeQueryRequest>(requestJson);

        Assert.Equal("subset-components", restoredRequest.MatchMode);
        Assert.Equal(3, restoredRequest.Selection!.MaxResults);
        Assert.Equal(mode, restoredRequest.Selection.Mode);
        Assert.NotNull(restoredRequest.Components);

        var response = new RecipeResponse(
            [], false, true, true, 300, mode, "revision");
        var restoredResponse = ProtocolJson.Deserialize<RecipeResponse>(
            ProtocolJson.Serialize(response));

        Assert.True(restoredResponse.SourceComplete);
        Assert.True(restoredResponse.SelectionComplete);
        Assert.Equal(300, restoredResponse.CandidatesEvaluated);
        Assert.Equal(mode, restoredResponse.SelectionMode);
        Assert.Equal("revision", restoredResponse.InventoryRevision);

        var processSelection = ProtocolJson.Deserialize<RecipeQueryRequest>(
            ProtocolJson.Serialize(new RecipeQueryRequest(
                "minecraft:iron_ingot",
                selection: RecipeSelectionRequest.RecommendedProcessFamilies())));
        Assert.Equal("recommended-process-families", processSelection.Selection!.Mode);
        Assert.Equal(5, processSelection.Selection.MaxResults);

        var preferredSelection = ProtocolJson.Deserialize<RecipeQueryRequest>(
            ProtocolJson.Serialize(new RecipeQueryRequest(
                "railcraft:standard_rail",
                selection: RecipeSelectionRequest.RecommendedDirect(
                    "railcraft:rolling/steel_rail"))));
        Assert.Equal("railcraft:rolling/steel_rail", preferredSelection.Selection!.PreferredRecipeId);
    }

    [Fact]
    public void HealthPingRoundTripPreservesEffectiveOrdinaryRateMetadataAndOmitsItWhenAbsent()
    {
        var configured = new HealthPingResponse(
            "fixed-nonce", true, "1.0", 240, 64, "neoforge-config");
        var restored = ProtocolJson.Deserialize<HealthPingResponse>(
            ProtocolJson.Serialize(configured));

        Assert.Equal(240, restored.OrdinaryRequestsPerMinute);
        Assert.Equal(64, restored.OrdinaryRequestBurst);
        Assert.Equal("neoforge-config", restored.OrdinaryRequestRateSource);

        using var legacy = JsonDocument.Parse(
            ProtocolJson.Serialize(new HealthPingResponse("fixed-nonce", true, "1.0")));
        Assert.False(legacy.RootElement.TryGetProperty("ordinaryRequestsPerMinute", out _));
        Assert.False(legacy.RootElement.TryGetProperty("ordinaryRequestBurst", out _));
        Assert.False(legacy.RootElement.TryGetProperty("ordinaryRequestRateSource", out _));
    }

    [Fact]
    public void PlayerSnapshotRoundTripPreservesObservedMaximumHealth()
    {
        var response = new PlayerSnapshotResponse(
            "minecraft:overworld", new Position(0, 64, 0), 55.49, 20, 0, GameMode.Survival, 55);

        var restored = ProtocolJson.Deserialize<PlayerSnapshotResponse>(ProtocolJson.Serialize(response));

        Assert.Equal(55, restored.MaxHealth);
    }

    [Fact]
    public void PlayerSnapshotOmitsUnknownMaximumHealthForLegacyPeers()
    {
        var response = new PlayerSnapshotResponse(
            "minecraft:overworld", new Position(0, 64, 0), 20, 20, 0, GameMode.Survival);

        using var json = JsonDocument.Parse(ProtocolJson.Serialize(response));

        Assert.False(json.RootElement.TryGetProperty("maxHealth", out _));
    }

    public static IEnumerable<object[]> RoundTripCases()
    {
        var planksSlot = new RecipeSlotDto(
            [
                new RecipeAlternativeDto(
                    "minecraft:oak_planks",
                    2,
                    JsonDocument.Parse(
                        "{\"minecraft:custom_data\":{\"fixture\":true}}")
                        .RootElement.Clone()),
                new RecipeAlternativeDto(
                    "minecraft:spruce_planks",
                    3)
            ]);
        var repeatedStickSlot = new RecipeSlotDto(
            [new RecipeAlternativeDto("minecraft:stick", 1)]);
        var catalystSlot = new RecipeSlotDto(
            [new RecipeAlternativeDto("minecraft:crafting_table", 1)]);
        var outputSlot = new RecipeSlotDto(
            [new RecipeAlternativeDto("minecraft:diamond_pickaxe", 1)]);
        var recipe = new RecipeDto(
            "minecraft:diamond_pickaxe",
            "minecraft:crafting_shaped",
            "minecraft",
            RecipeIdentityDto.Registered(),
            [planksSlot, repeatedStickSlot, repeatedStickSlot],
            [catalystSlot],
            [outputSlot],
            new RecipeProcessDto("minecraft:crafting_table"));
        var recipeResponse = new RecipeResponse([recipe], true);
        var inventorySlot =
            new InventorySlot(1, false, "minecraft:diamond", 3);
        var position = new Position(0, 64, 0);
        using var requestPayload = JsonDocument.Parse(
            """{"nonce":"fixed-nonce"}""");
        using var responsePayload = JsonDocument.Parse(
            """{"nonce":"fixed-nonce","gameReady":false,"protocolVersion":"1.0"}""");

        object[] values =
        [
            new SessionPairRequest("MiAxion Desktop", "1.0.0"),
            new SessionPairApproved(Token, "Bearer"),
            new SessionRevokeRequest(SessionRevokeReason.UserRequested),
            new SessionRevokeResponse(true),
            new RequestCancelRequest(TargetRequestId),
            new RequestCancelResponse(
                TargetRequestId,
                true,
                CancelState.Cancelled),
            new CapabilitiesListRequest(),
            new CapabilityDescriptor("health.ping", "1.0", false),
            new CapabilitiesListResponse(
                [new CapabilityDescriptor("health.ping", "1.0", false)]),
            new RecipeQueryRequest("minecraft:diamond"),
            new RecipeUsesRequest("minecraft:diamond"),
            planksSlot.Alternatives[0],
            planksSlot,
            recipe,
            recipeResponse,
            new InventorySnapshotRequest(true),
            inventorySlot,
            new InventorySnapshotResponse("inventory-revision-1", [inventorySlot]),
            new PlayerSnapshotRequest(),
            position,
            new PlayerSnapshotResponse(
                "minecraft:overworld",
                position,
                20,
                20,
                0,
                GameMode.Survival),
            new EntityResolveRequest("动力臂怎么用", ["create"]),
            new EntityResolveMatch(
                "item",
                "create:deployer",
                "block.create.deployer",
                "动力臂"),
            new EntityResolveResponse(
                "r1",
                [new EntityResolveMatch(
                    "item",
                    "create:deployer",
                    "block.create.deployer",
                    "动力臂")],
                false),
            new EntityReference("item", "create:deployer"),
            new EntityDescribeRequest(
                [
                    new EntityReference("item", "create:deployer"),
                    new EntityReference("block", "minecraft:crafting_table")
                ]),
            new EntityDescription(
                "item",
                "create:deployer",
                "block.create.deployer",
                "动力臂"),
            new EntityDescribeResponse(
                "r1",
                [new EntityDescription(
                    "item",
                    "create:deployer",
                    "block.create.deployer",
                    "动力臂")],
                [new EntityReference("block", "minecraft:crafting_table")]),
            new HudShowRequest(UnicodeHudText, HudSeverity.Info, 3000),
            new HudShowResponse(true),
            new HealthPingRequest("fixed-nonce"),
            new HealthPingResponse("fixed-nonce", false, "1.0"),
            new ProtocolError(
                ProtocolErrorCode.InvalidRequest,
                "request was invalid",
                false,
                null),
            new RequestEnvelope(
                ProtocolConstants.Version,
                RequestId,
                Timestamp,
                Deadline,
                new ProtocolAction("health.ping"),
                Token,
                requestPayload.RootElement.Clone(),
                null),
            new ResponseEnvelope(
                ProtocolConstants.Version,
                RequestId,
                Timestamp,
                Deadline,
                new ProtocolAction("health.ping"),
                null,
                responsePayload.RootElement.Clone(),
                null)
        ];

        foreach (var value in values)
        {
            yield return [value, value.GetType()];
        }
    }

    [Fact]
    public void RecipeDtosPreserveRolesSlotMultiplicityAndAlternativeCounts()
    {
        var firstRequired = new RecipeSlotDto(
            [
                new RecipeAlternativeDto("minecraft:oak_planks", 2),
                new RecipeAlternativeDto("minecraft:spruce_planks", 3)
            ]);
        var secondRequired = new RecipeSlotDto(
            [new RecipeAlternativeDto("minecraft:stick", 1)]);
        var catalyst = new RecipeSlotDto(
            [new RecipeAlternativeDto("minecraft:crafting_table", 1)]);
        var recipe = new RecipeDto(
            "minecraft:test",
            "minecraft:crafting",
            "minecraft",
            [firstRequired, secondRequired, secondRequired],
            [catalyst],
            [new RecipeSlotDto(
                [new RecipeAlternativeDto("minecraft:result", 1)])]);
        var response = new RecipeResponse([recipe], true);

        var restored = ProtocolJson.Deserialize<RecipeResponse>(
            ProtocolJson.Serialize(response));

        Assert.True(restored.Truncated);
        Assert.Equal(3, restored.Recipes[0].Inputs.Count);
        Assert.Equal(2, restored.Recipes[0].Inputs[0].Alternatives.Count);
        Assert.Equal(
            [2, 3],
            restored.Recipes[0].Inputs[0].Alternatives
                .Select(alternative => alternative.Count));
        Assert.Single(restored.Recipes[0].Catalysts);
        Assert.Equal(
            "minecraft:crafting_table",
            restored.Recipes[0].Catalysts[0].Alternatives[0].ItemId);
        Assert.Equal(
            "registered",
            restored.Recipes[0].Identity.Kind);
    }

    [Fact]
    public void RecipeComponentsAreDeepCopiedAndOptional()
    {
        using var document = JsonDocument.Parse(
            "{\"minecraft:custom_data\":{\"value\":1}}");
        var alternative = new RecipeAlternativeDto(
            "minecraft:stick",
            1,
            document.RootElement);

        document.RootElement.GetProperty("minecraft:custom_data")
            .GetProperty("value");
        Assert.Equal(
            "{\"minecraft:custom_data\":{\"value\":1}}",
            alternative.Components!.Value.GetRawText());
        Assert.Null(new RecipeAlternativeDto("minecraft:stick", 1).Components);
    }

    [Fact]
    public void RecipeIdentityRequiresRegisteredOrDerivedShape()
    {
        Assert.Throws<ArgumentException>(
            () => new RecipeIdentityDto("other"));
        Assert.Throws<ArgumentException>(
            () => new RecipeIdentityDto(
                "derived",
                "sha256-canonical-v1",
                "not-a-sha256"));
    }

    [Fact]
    public void RecipeDtoCollectionsAreDefensivelyCopiedAndReadOnly()
    {
        var alternatives = new List<RecipeAlternativeDto>
        {
            new("minecraft:stick", 1)
        };
        var slot = new RecipeSlotDto(alternatives);
        var inputs = new List<RecipeSlotDto> { slot };
        var catalysts = new List<RecipeSlotDto> { slot };
        var outputs = new List<RecipeSlotDto> { slot };
        var recipe = new RecipeDto(
            "minecraft:test",
            "minecraft:crafting",
            "minecraft",
            inputs,
            catalysts,
            outputs);
        var recipes = new List<RecipeDto> { recipe };
        var response = new RecipeResponse(recipes, false);

        alternatives.Clear();
        inputs.Clear();
        catalysts.Clear();
        outputs.Clear();
        recipes.Clear();

        Assert.Single(slot.Alternatives);
        Assert.Single(recipe.Inputs);
        Assert.Single(recipe.Catalysts);
        Assert.Single(recipe.Outputs);
        Assert.Single(response.Recipes);
        Assert.Throws<NotSupportedException>(
            () => ((IList<RecipeAlternativeDto>)slot.Alternatives).Clear());
        Assert.Throws<NotSupportedException>(
            () => ((IList<RecipeSlotDto>)recipe.Inputs).Clear());
        Assert.Throws<NotSupportedException>(
            () => ((IList<RecipeDto>)response.Recipes).Clear());
    }

    [Theory]
    [MemberData(nameof(RoundTripCases))]
    public void EveryContractFamilyRoundTrips(object original, Type type)
    {
        var json = JsonSerializer.Serialize(
            original,
            type,
            ProtocolJson.Options);
        var deserialized = JsonSerializer.Deserialize(
            json,
            type,
            ProtocolJson.Options);
        Assert.NotNull(deserialized);

        var roundTripJson = JsonSerializer.Serialize(
            deserialized,
            type,
            ProtocolJson.Options);
        using var expected = JsonDocument.Parse(json);
        using var actual = JsonDocument.Parse(roundTripJson);

        Assert.True(
            JsonElement.DeepEquals(
                expected.RootElement,
                actual.RootElement),
            $"Round-trip mismatch for {type.Name}.");
    }

    [Fact]
    public void SharedUnicodeHudFixtureRoundTripsAndRevalidates()
    {
        var fixtureJson = StrictUtf8.ReadAllText(
            SchemaFixtureTests.FixturePath("valid/hud-request.json"));
        var envelope =
            ProtocolJson.Deserialize<RequestEnvelope>(fixtureJson);
        var payload = envelope.Payload.Deserialize<HudShowRequest>(
            ProtocolJson.Options);

        Assert.NotNull(payload);
        Assert.Equal(UnicodeHudText, payload.Text);

        using var original = JsonDocument.Parse(fixtureJson);
        using var serialized = JsonDocument.Parse(
            ProtocolJson.Serialize(envelope));

        Assert.True(
            EnvelopesAreSemanticallyEqual(
                original.RootElement,
                serialized.RootElement));

        var result = SchemaFixtureTests.Evaluate(
            "request.schema.json",
            serialized.RootElement);
        Assert.True(
            result.IsValid &&
                SchemaFixtureTests.RequestTimeIsStrictlyOrdered(
                    serialized.RootElement),
            JsonSerializer.Serialize(result));
    }

    private static bool EnvelopesAreSemanticallyEqual(
        JsonElement expected,
        JsonElement actual)
    {
        foreach (var property in expected.EnumerateObject())
        {
            if (!actual.TryGetProperty(property.Name, out var actualValue))
            {
                return false;
            }

            if (property.Name is "timestamp" or "deadline")
            {
                if (DateTimeOffset.Parse(
                        property.Value.GetString()!,
                        CultureInfo.InvariantCulture) !=
                    DateTimeOffset.Parse(
                        actualValue.GetString()!,
                        CultureInfo.InvariantCulture))
                {
                    return false;
                }
            }
            else if (!JsonElement.DeepEquals(property.Value, actualValue))
            {
                return false;
            }
        }

        return expected.EnumerateObject().Count() ==
            actual.EnumerateObject().Count();
    }
}
