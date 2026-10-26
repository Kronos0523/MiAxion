using System.Globalization;
using System.Text.Json;
using MiAxion.Protocol.Contracts;
using MiAxion.Protocol.Serialization;

namespace MiAxion.Protocol.Tests;

public sealed class SerializationTests
{
    private const string RequestId = "b8b88ff4-36c0-4bdc-a5c1-8a976dc7d2cf";
    private const string Timestamp = "2026-07-26T05:30:00Z";
    private const string Deadline = "2026-07-26T05:30:03Z";

    public static TheoryData<string> ForbiddenRequestJson => new()
    {
        CreateRequestJson(
            action: "health.ping",
            payload: """{"nonce":"fixed-nonce"}""",
            extra: ""","unexpected":true"""),
        CreateRequestJson(
            action: "health.ping",
            payload: """{"nonce":"fixed-nonce"}""",
            omitRequestId: true),
        CreateRequestJson(
            action: "health.ping",
            payload: """{"nonce":"fixed-nonce"}""",
            requestId: "null"),
        CreateRequestJson(
            action: "health.ping",
            payload: """{"nonce":"fixed-nonce"}""",
            actionPropertyName: "Action"),
        CreateRequestJson(
            action: "missingdot",
            payload: """{"nonce":"fixed-nonce"}"""),
        CreateRequestJson(
            action: "future..snapshot",
            payload: """{"nonce":"fixed-nonce"}"""),
        CreateRequestJson(
            action: "HUD.SHOW",
            payload: """{"text":"case","severity":"info","durationMs":3000}"""),
        CreateRequestJson(
            action: "HudShow",
            payload: """{"text":"alias","severity":"info","durationMs":3000}"""),
        CreateRequestJson(
            action: "10",
            payload: """{"nonce":"fixed-nonce"}""",
            actionIsJsonLiteral: true)
    };

    public static TheoryData<ProtocolAction, string> ActionWireNames => new()
    {
        { new("session.pair.request"), "session.pair.request" },
        { new("session.pair.approved"), "session.pair.approved" },
        { new("session.revoke"), "session.revoke" },
        { new("request.cancel"), "request.cancel" },
        { new("capabilities.list"), "capabilities.list" },
        { new("extensions.list"), "extensions.list" },
        { new("extensions.call"), "extensions.call" },
        { new("recipe.query"), "recipe.query" },
        { new("recipe.uses"), "recipe.uses" },
        { new("recipe.providers.list"), "recipe.providers.list" },
        { new("recipe.query.source"), "recipe.query.source" },
        { new("recipe.uses.source"), "recipe.uses.source" },
        { new("quests.query"), "quests.query" },
        { new("quests.detail"), "quests.detail" },
        { new("inventory.snapshot"), "inventory.snapshot" },
        { new("terminology.configure"), "terminology.configure" },
        { new("terminology.snapshot"), "terminology.snapshot" },
        { new("terminology.clear"), "terminology.clear" },
        { new("terminology.changed"), "terminology.changed" },
        { new("player.snapshot"), "player.snapshot" },
        { new("entities.resolve"), "entities.resolve" },
        { new("entities.describe"), "entities.describe" },
        { new("mods.versions.resolve"), "mods.versions.resolve" },
        { new("world.structure.locate"), "world.structure.locate" },
        { new("world.biome.locate"), "world.biome.locate" },
        { new("world.machines.scan"), "world.machines.scan" },
        { new("world.containers.scan"), "world.containers.scan" },
        { new("world.structure.targets"), "world.structure.targets" },
        { new("world.registry.resolve"), "world.registry.resolve" },
        { new("world.registry.describe"), "world.registry.describe" },
        { new("world.query.progress"), "world.query.progress" },
        { new("hud.show"), "hud.show" },
        { new("health.ping"), "health.ping" }
    };

    public static TheoryData<
        ProtocolAction,
        KnownProtocolAction> KnownActionValues => new()
    {
        {
            new("session.pair.request"),
            KnownProtocolAction.SessionPairRequest
        },
        {
            new("session.pair.approved"),
            KnownProtocolAction.SessionPairApproved
        },
        { new("session.revoke"), KnownProtocolAction.SessionRevoke },
        { new("request.cancel"), KnownProtocolAction.RequestCancel },
        {
            new("capabilities.list"),
            KnownProtocolAction.CapabilitiesList
        },
        { new("extensions.list"), KnownProtocolAction.ExtensionsList },
        { new("extensions.call"), KnownProtocolAction.ExtensionsCall },
        { new("recipe.query"), KnownProtocolAction.RecipeQuery },
        { new("recipe.uses"), KnownProtocolAction.RecipeUses },
        { new("recipe.providers.list"), KnownProtocolAction.RecipeProvidersList },
        { new("recipe.query.source"), KnownProtocolAction.RecipeQuerySource },
        { new("recipe.uses.source"), KnownProtocolAction.RecipeUsesSource },
        { new("quests.query"), KnownProtocolAction.QuestsQuery },
        { new("quests.detail"), KnownProtocolAction.QuestsDetail },
        {
            new("inventory.snapshot"),
            KnownProtocolAction.InventorySnapshot
        },
        { new("terminology.configure"), KnownProtocolAction.TerminologyConfigure },
        { new("terminology.snapshot"), KnownProtocolAction.TerminologySnapshot },
        { new("terminology.clear"), KnownProtocolAction.TerminologyClear },
        { new("terminology.changed"), KnownProtocolAction.TerminologyChanged },
        {
            new("player.snapshot"),
            KnownProtocolAction.PlayerSnapshot
        },
        { new("entities.resolve"), KnownProtocolAction.EntityResolve },
        { new("entities.describe"), KnownProtocolAction.EntityDescribe },
        { new("mods.versions.resolve"), KnownProtocolAction.ModVersionsResolve },
        { new("world.structure.locate"), KnownProtocolAction.WorldStructureLocate },
        { new("world.biome.locate"), KnownProtocolAction.WorldBiomeLocate },
        { new("world.machines.scan"), KnownProtocolAction.WorldMachinesScan },
        { new("world.containers.scan"), KnownProtocolAction.WorldContainersScan },
        { new("world.structure.targets"), KnownProtocolAction.WorldStructureTargets },
        { new("world.registry.resolve"), KnownProtocolAction.WorldRegistryResolve },
        { new("world.registry.describe"), KnownProtocolAction.WorldRegistryDescribe },
        { new("world.query.progress"), KnownProtocolAction.WorldQueryProgress },
        { new("hud.show"), KnownProtocolAction.HudShow },
        { new("health.ping"), KnownProtocolAction.HealthPing }
    };

    public static TheoryData<ProtocolErrorCode, string> ErrorWireNames => new()
    {
        { ProtocolErrorCode.InvalidRequest, "INVALID_REQUEST" },
        { ProtocolErrorCode.Unauthorized, "UNAUTHORIZED" },
        { ProtocolErrorCode.UnsupportedVersion, "UNSUPPORTED_VERSION" },
        { ProtocolErrorCode.CapabilityUnavailable, "CAPABILITY_UNAVAILABLE" },
        { ProtocolErrorCode.GameNotReady, "GAME_NOT_READY" },
        { ProtocolErrorCode.NotFound, "NOT_FOUND" },
        { ProtocolErrorCode.Timeout, "TIMEOUT" },
        { ProtocolErrorCode.RateLimited, "RATE_LIMITED" },
        { ProtocolErrorCode.DetailsHidden, "DETAILS_HIDDEN" },
        { ProtocolErrorCode.ResultTooLarge, "RESULT_TOO_LARGE" },
        { ProtocolErrorCode.InternalError, "INTERNAL_ERROR" }
    };

    [Fact]
    public void PublicContractInventoryIsExact()
    {
        string[] expected =
        [
            nameof(ActionConfigurationEntry),
            nameof(ActionConfigurationField),
            nameof(ActionConfigurationGroup),
            nameof(ActionConfigurationPage),
            nameof(ActionConfigurationRequest),
            nameof(ActionConfigurationResponse),
            nameof(PlayerObservationPayloads),
            nameof(PlayerObservationPayloads.Request),
            nameof(PlayerObservationPayloads.Enchant),
            nameof(PlayerObservationPayloads.Item),
            nameof(PlayerObservationPayloads.Slot),
            nameof(PlayerObservationPayloads.Equipment),
            nameof(PlayerObservationPayloads.Details),
            nameof(PlayerObservationPayloads.BlockPosition),
            nameof(PlayerObservationPayloads.Position),
            nameof(PlayerObservationPayloads.Effect),
            nameof(PlayerObservationPayloads.StateProperty),
            nameof(PlayerObservationPayloads.ScreenText),
            nameof(PlayerObservationPayloads.InventoryView),
            nameof(PlayerObservationPayloads.PlayerStatus),
            nameof(PlayerObservationPayloads.WorldContext),
            nameof(PlayerObservationPayloads.ScreenSnapshot),
            nameof(PlayerObservationPayloads.TargetSnapshot),
            nameof(RecipeResourceDto),
            nameof(WorldRegistrySearchRequest),
            nameof(WorldRegistrySearchResponse),
            nameof(CancelState),
            nameof(CapabilitiesListRequest),
            nameof(CapabilitiesListResponse),
            nameof(CapabilityDescriptor),
            nameof(ExtensionListRequest),
            nameof(ExtensionCallRequest),
            nameof(ExtensionListResponse),
            nameof(ExtensionCallResponse),
            nameof(ExtensionDescriptorDto),
            nameof(ExtensionDependencyDto),
            nameof(EntityDescription),
            nameof(EntityDescribeRequest),
            nameof(EntityDescribeResponse),
            nameof(EntityReference),
            nameof(EntityResolveMatch),
            nameof(EntityResolveRequest),
            nameof(EntityResolveResponse),
            nameof(WorldBlockPosition),
            nameof(WorldChunkRange),
            nameof(WorldContainerObservation),
            nameof(WorldContainerSide),
            nameof(WorldContainerItem),
            nameof(WorldContainersResponse),
            nameof(WorldContainerView),
            nameof(WorldLocatedResponse),
            nameof(WorldLocateRequest),
            nameof(WorldLocatorRange),
            nameof(WorldMachineCandidateDomain),
            nameof(WorldMachineFeature),
            nameof(WorldMachineState),
            nameof(WorldMachineObservation),
            nameof(WorldMachinesResponse),
            nameof(WorldNearbyScanRequest),
            nameof(WorldScanWindow),
            nameof(WorldQueryAuthority),
            nameof(WorldQuerySummary),
            nameof(WorldQueryTargetKind),
            nameof(WorldRegistryDescribeRequest),
            nameof(WorldRegistryDescribeResponse),
            nameof(WorldRegistryKind),
            nameof(WorldRegistryMatch),
            nameof(WorldRegistryReference),
            nameof(WorldRegistryResolveRequest),
            nameof(WorldRegistryResolveResponse),
            nameof(WorldStructureTargetsResponse),
            nameof(WorldStructureTargetsRequest),
            nameof(WorldTargetObservation),
            nameof(WorldVerticalRange),
            nameof(WorldQueryProgressLane),
            nameof(WorldQueryProgressRequest),
            nameof(WorldQueryProgressResponse),
            nameof(GameMode),
            nameof(HealthPingRequest),
            nameof(HealthPingResponse),
            nameof(HudSeverity),
            nameof(HudShowRequest),
            nameof(HudShowResponse),
            nameof(InventorySlot),
            nameof(InventorySnapshotRequest),
            nameof(InventorySnapshotResponse),
            nameof(KnownProtocolAction),
            nameof(KeyActionId),
            nameof(KeyValue),
            nameof(UnboundKeyValue),
            nameof(BoundKeyValue),
            nameof(KeyContextMetadata),
            nameof(KeybindingSelection),
            nameof(KeybindingCatalogRequest),
            nameof(KeybindingCatalogEntry),
            nameof(KeybindingCatalogResponse),
            nameof(KeybindingPreviewRequest),
            nameof(KeybindingPreviewEntry),
            nameof(KeybindingPreviewResponse),
            nameof(KeybindingApplyRequest),
            nameof(KeybindingRollbackRequest),
            nameof(KeybindingChange),
            nameof(KeybindingSkip),
            nameof(KeybindingReceipt),
            nameof(KeybindingApplyResponse),
            nameof(KeybindingRollbackResponse),
            nameof(GameActionTarget),
            nameof(KeyActionTarget),
            nameof(ArsPageTarget),
            nameof(IronSpellTarget),
            nameof(GameActionScope),
            nameof(GameActionContextRequest),
            nameof(GameActionContextResponse),
            nameof(GameActionCatalogRequest),
            nameof(GameActionCatalogEntry),
            nameof(GameActionCatalogResponse),
            nameof(GameActionGrantRequest),
            nameof(GameActionGrantResponse),
            nameof(GameActionRevokeRequest),
            nameof(GameActionRevokeResponse),
            nameof(GameActionExecuteRequest),
            nameof(GameActionStatusRequest),
            nameof(GameActionOutcome),
            nameof(GameActionExecutionResponse),
            nameof(ModVersionsResolveRequest),
            nameof(ModVersionsResolveResponse),
            nameof(TerminologyConfigureRequest),
            nameof(TerminologySnapshotRequest),
            nameof(TerminologyClearRequest),
            nameof(TerminologyCaptureSettings),
            nameof(TerminologyIdentity),
            nameof(TerminologyEntry),
            nameof(TerminologyBrowserGroup),
            nameof(ObservedPotionEffectPayload),
            nameof(ObservedItemDetailsPayload),
            nameof(TerminologySnapshotPayload),
            nameof(TerminologyChangedEnvelope),
            nameof(PlayerSnapshotRequest),
            nameof(PlayerSnapshotResponse),
            nameof(Position),
            nameof(ProtocolAction),
            nameof(ProtocolConstants),
            nameof(ProtocolError),
            nameof(ProtocolErrorCode),
            nameof(QuestDependency),
            nameof(QuestDescriptionVisibility),
            nameof(QuestDetailRequest),
            nameof(QuestDetailResponse),
            nameof(QuestObjective),
            nameof(QuestQueryEntry),
            nameof(QuestQueryRequest),
            nameof(QuestQueryResponse),
            nameof(QuestQueryScope),
            nameof(QuestReward),
            nameof(QuestRewardClaimState),
            nameof(QuestState),
            nameof(RecipeAlternativeDto),
            nameof(RecipeClassificationDto),
            nameof(RecipeDto),
            nameof(RecipeIdentityDto),
            nameof(RecipeProcessDto),
            nameof(RecipeProviderDescriptor),
            nameof(RecipeProviderReadiness),
            nameof(RecipeProvidersListRequest),
            nameof(RecipeProvidersListResponse),
            nameof(RecipeQueryRequest),
            nameof(RecipeResponse),
            nameof(RecipeSelectionRequest),
            nameof(RecipeSourceRequest),
            nameof(RecipeSlotDto),
            nameof(RecipeUsesRequest),
            nameof(RequestCancelRequest),
            nameof(RequestCancelResponse),
            nameof(RequestEnvelope),
            nameof(ResponseEnvelope),
            nameof(SessionPairApproved),
            nameof(SessionPairRequest),
            nameof(SessionRevokeReason),
            nameof(SessionRevokeRequest),
            nameof(SessionRevokeResponse)
        ];
        var actual = typeof(ProtocolAction).Assembly
            .GetExportedTypes()
            .Where(type =>
                type.Namespace == "MiAxion.Protocol.Contracts")
            .Select(type => type.Name)
            .Order(StringComparer.Ordinal)
            .ToArray();

        Assert.Equal(
            expected.Order(StringComparer.Ordinal),
            actual);
        Assert.Equal(
            [
                nameof(ProtocolConstants.Version),
                nameof(ProtocolConstants.RecipeCapabilityVersion),
                nameof(ProtocolConstants.RecipeSourceCapabilityVersion),
                nameof(ProtocolConstants.RecipeSourcePageDefault),
                nameof(ProtocolConstants.RecipeSourcePageMaximum),
                nameof(ProtocolConstants.RecipeSourceCursorMaximum),
                nameof(ProtocolConstants.QuestCapabilityVersion),
                nameof(ProtocolConstants.EntityResolveCapabilityVersion),
                nameof(ProtocolConstants.EntityDescribeCapabilityVersion),
                nameof(ProtocolConstants.ModVersionsResolveCapabilityVersion),
                nameof(ProtocolConstants.WorldNearbyCapabilityVersion),
                nameof(ProtocolConstants.WorldStructureTargetsCapabilityVersion),
                nameof(ProtocolConstants.QuestQueryResultMaximum),
                nameof(ProtocolConstants.QuestDetailCollectionMaximum),
                nameof(ProtocolConstants.QuestDescriptionLineScalarMaximum),
                nameof(ProtocolConstants.QuestTitleScalarMaximum),
                nameof(ProtocolConstants.QuestProgressIntegerMaximum),
                nameof(ProtocolConstants.RecipeSlotAlternativesMaximum),
                nameof(ProtocolConstants.RecipeInputSlotsMaximum),
                nameof(ProtocolConstants.RecipeCatalystSlotsMaximum),
                nameof(ProtocolConstants.RecipeOutputSlotsMaximum),
                nameof(ProtocolConstants.WorldQueryResultMaximum),
                nameof(ProtocolConstants.WorldQueryMachineFeatureMaximum),
                nameof(ProtocolConstants.WorldQueryContainerStackMaximum),
                nameof(ProtocolConstants.WorldQueryNameCandidateMaximum),
                nameof(ProtocolConstants.WorldQueryWarningMaximum),
                nameof(ProtocolConstants.WorldQueryStructurePieceMaximum),
                nameof(ProtocolConstants.WorldQueryProgressMaximum),
                nameof(ProtocolConstants.MaximumSafeJsonInteger),
                nameof(ProtocolConstants.DiscoverySchemaVersion),
                nameof(ProtocolConstants.DiscoveryRecordMaximumBytes),
                nameof(ProtocolConstants.DiscoveryV2RecordMaximumBytes),
                nameof(ProtocolConstants.DiscoveryProcessStartToleranceMilliseconds),
                nameof(ProtocolConstants.DiscoveryRecordExtension),
                nameof(ProtocolConstants.DiscoveryEndpointScheme),
                nameof(ProtocolConstants.DiscoveryEndpointPath),
                nameof(ProtocolConstants.DiscoverySubprotocol),
                nameof(ProtocolConstants.DiscoveryMinecraftVersion)
            ],
            typeof(ProtocolConstants)
                .GetFields(
                    System.Reflection.BindingFlags.Public |
                    System.Reflection.BindingFlags.Static)
                .Select(field => field.Name));
    }

    [Fact]
    public void SerializerOptionsAreImmutable()
    {
        Assert.True(ProtocolJson.Options.IsReadOnly);
        Assert.Throws<InvalidOperationException>(
            () => ProtocolJson.Options.PropertyNameCaseInsensitive = true);
    }

    [Theory]
    [MemberData(nameof(ForbiddenRequestJson))]
    public void RequestEnvelopeRejectsForbiddenJson(string json) =>
        Assert.Throws<JsonException>(
            () => ProtocolJson.Deserialize<RequestEnvelope>(json));

    [Fact]
    public void RequestEnvelopeAcceptsAndPreservesUnknownWellFormedAction()
    {
        var request = ProtocolJson.Deserialize<RequestEnvelope>(
            CreateRequestJson(
                action: "future.read.snapshot",
                payload: """{"provider":"next"}"""));

        Assert.Equal("future.read.snapshot", request.Action.ToString());
        Assert.Equal(
            """{"provider":"next"}""",
            request.Payload.GetRawText());
    }

    [Theory]
    [InlineData("""{"severity":"notice"}""")]
    [InlineData("""{"severity":"INFO"}""")]
    [InlineData("""{"severity":"Info"}""")]
    [InlineData("""{"severity":0}""")]
    public void TypedEnumRejectsUnknownWrongCaseAndNumericValues(string json) =>
        Assert.Throws<JsonException>(
            () => ProtocolJson.Deserialize<EnumHolder>(json));

    [Theory]
    [InlineData("\"invalid_request\"")]
    [InlineData("\"InvalidRequest\"")]
    [InlineData("0")]
    public void ErrorCodeRejectsNonWireValues(string json) =>
        Assert.Throws<JsonException>(
            () => ProtocolJson.Deserialize<ProtocolErrorCode>(json));

    [Fact]
    public void PayloadRejectsMissingRequiredConstructorField() =>
        Assert.Throws<JsonException>(
            () => ProtocolJson.Deserialize<HudShowRequest>(
                """{"severity":"info","durationMs":3000}"""));

    [Fact]
    public void PayloadRejectsNullForNonNullableReference() =>
        Assert.Throws<JsonException>(
            () => ProtocolJson.Deserialize<CapabilitiesListResponse>(
                """{"capabilities":null}"""));

    [Fact]
    public void ResponseEnvelopeRequiresExplicitSessionTokenMember() =>
        Assert.Throws<JsonException>(
            () => ProtocolJson.Deserialize<ResponseEnvelope>(
                $$"""
                {
                  "protocolVersion":"1.0",
                  "requestId":"{{RequestId}}",
                  "timestamp":"{{Timestamp}}",
                  "deadline":"{{Deadline}}",
                  "action":"health.ping",
                  "payload":{"nonce":"fixed-nonce","gameReady":false,"protocolVersion":"1.0"},
                  "error":null
                }
                """));

    [Fact]
    public void RequestEnvelopeRejectsNonIncreasingDeadline()
    {
        using var payload = JsonDocument.Parse(
            """{"nonce":"fixed-nonce"}""");
        var timestamp = DateTimeOffset.Parse(
            Timestamp,
            CultureInfo.InvariantCulture);

        Assert.Throws<ArgumentException>(
            () => new RequestEnvelope(
                ProtocolConstants.Version,
                Guid.Parse(RequestId),
                timestamp,
                timestamp,
                new ProtocolAction("health.ping"),
                "QmFzZTY0dXJsLXNlc3Npb24tdG9rZW4tdjEtMzItYnl0ZXM",
                payload.RootElement.Clone(),
                null));
    }

    [Fact]
    public void InventorySnapshotRequestDefaultsIncludeEmptyToFalse()
    {
        var request = ProtocolJson.Deserialize<InventorySnapshotRequest>("{}");

        Assert.False(request.IncludeEmpty);
    }

    [Fact]
    public void SourceBoundRecipeRequestIsSeparateFromLegacyRecipeShape()
    {
        var selected = new RecipeSourceRequest("miaxion_rei", 9, "minecraft:stick");
        using var selectedJson = JsonDocument.Parse(ProtocolJson.Serialize(selected));
        Assert.Equal("miaxion_rei", selectedJson.RootElement.GetProperty("addonId").GetString());
        Assert.Equal(9, selectedJson.RootElement.GetProperty("sourceGeneration").GetInt64());
        Assert.Equal("minecraft:stick", selectedJson.RootElement.GetProperty("itemId").GetString());
        using var legacyJson = JsonDocument.Parse(ProtocolJson.Serialize(new RecipeQueryRequest("minecraft:stick")));
        Assert.False(legacyJson.RootElement.TryGetProperty("addonId", out _));
        Assert.False(legacyJson.RootElement.TryGetProperty("sourceGeneration", out _));
        Assert.Throws<ArgumentOutOfRangeException>(() => new RecipeSourceRequest("miaxion_rei", -1, "minecraft:stick"));
        Assert.Throws<ArgumentException>(() => new RecipeSourceRequest("Bad ID", 9, "minecraft:stick"));
    }

    [Fact]
    public void ResponseEnvelopeSerializesExplicitNullSessionToken()
    {
        using var payload = JsonDocument.Parse(
            """{"nonce":"fixed-nonce","gameReady":false,"protocolVersion":"1.0"}""");
        var response = new ResponseEnvelope(
            ProtocolConstants.Version,
            Guid.Parse(RequestId),
            DateTimeOffset.Parse(Timestamp, CultureInfo.InvariantCulture),
            DateTimeOffset.Parse(Deadline, CultureInfo.InvariantCulture),
            new ProtocolAction("health.ping"),
            null,
            payload.RootElement.Clone(),
            null);

        using var json = JsonDocument.Parse(ProtocolJson.Serialize(response));

        Assert.True(json.RootElement.TryGetProperty("sessionToken", out var token));
        Assert.Equal(JsonValueKind.Null, token.ValueKind);
    }

    [Theory]
    [MemberData(nameof(ActionWireNames))]
    public void ProtocolActionsUseExactWireNames(
        ProtocolAction action,
        string expectedWireName)
    {
        var json = ProtocolJson.Serialize(action);

        Assert.Equal($"\"{expectedWireName}\"", json);
        Assert.Equal(action, ProtocolJson.Deserialize<ProtocolAction>(json));
    }

    [Theory]
    [MemberData(nameof(KnownActionValues))]
    public void KnownActionCatalogMapsExactWireValues(
        ProtocolAction action,
        KnownProtocolAction expected)
    {
        Assert.True(action.TryGetKnown(out var actual));
        Assert.Equal(expected, actual);
    }

    [Fact]
    public void UnknownActionIsNotMisclassifiedAsKnown()
    {
        var action = new ProtocolAction("future.read.snapshot");

        Assert.False(action.TryGetKnown(out _));
    }

    [Theory]
    [MemberData(nameof(ErrorWireNames))]
    public void ProtocolErrorCodesUseExactWireNames(
        ProtocolErrorCode code,
        string expectedWireName)
    {
        var json = ProtocolJson.Serialize(code);

        Assert.Equal($"\"{expectedWireName}\"", json);
        Assert.Equal(code, ProtocolJson.Deserialize<ProtocolErrorCode>(json));
    }

    private static string CreateRequestJson(
        string action,
        string payload,
        string requestId = $"\"{RequestId}\"",
        string actionPropertyName = "action",
        bool actionIsJsonLiteral = false,
        bool omitRequestId = false,
        string extra = "")
    {
        var requestIdProperty = omitRequestId
            ? string.Empty
            : $"\"requestId\":{requestId},";
        var actionValue = actionIsJsonLiteral ? action : $"\"{action}\"";

        return $$"""
            {
              "protocolVersion":"1.0",
              {{requestIdProperty}}
              "timestamp":"{{Timestamp}}",
              "deadline":"{{Deadline}}",
              "{{actionPropertyName}}":{{actionValue}},
              "sessionToken":"QmFzZTY0dXJsLXNlc3Npb24tdG9rZW4tdjEtMzItYnl0ZXM",
              "payload":{{payload}},
              "error":null{{extra}}
            }
            """;
    }

    private sealed record EnumHolder(HudSeverity Severity);
}
