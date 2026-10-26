using System.Text.Json;
using System.Text.Json.Nodes;
using MiAxion.Protocol.Contracts;
using MiAxion.Protocol.Serialization;

namespace MiAxion.Protocol.Tests;

public sealed class CastingProtocolTests
{
    private static readonly Guid Id = Guid.Parse("11111111-1111-1111-1111-111111111111");
    private static readonly DateTimeOffset Now = new(2026, 10, 2, 0, 0, 0, TimeSpan.Zero);
    private static readonly KeyActionId KeyId = new("minecraft", "key.togglePerspective", "neoforge-1.21.1");
    private static readonly KeyContextMetadata Context = new("universal", "native-owner");
    private static readonly GameActionScope Scope = new("/instances/test", 1, 1, Id, "casting", Id, 1, 1, 1);

    public static IEnumerable<object[]> Contracts()
    {
        var receipt = new KeybindingReceipt(Id, "/instances/test", "revision-2",
            [new(KeyId, new BoundKeyValue("keysym", 74, "none"), new UnboundKeyValue())], [], "applied", Now);
        yield return ["keybindings.catalog", new KeybindingCatalogRequest(),
            new KeybindingCatalogResponse("/instances/test", "revision-1",
                [new("key.togglePerspective", KeyId, "Perspective", new UnboundKeyValue(), Context, true, null),
                 new("unknown.action", null, "Unknown", null, null, false, "owner_unknown")], "next_page")];
        yield return ["keybindings.preview", new KeybindingPreviewRequest("/instances/test", "revision-1", Id, 1,
            [new(KeyId, new BoundKeyValue("mouse", 1, "shift"), Context)]),
            new KeybindingPreviewResponse(Id, "/instances/test", "revision-1",
                [new(KeyId, new UnboundKeyValue(), new BoundKeyValue("mouse", 1, "shift"), "change", null)])];
        yield return ["keybindings.apply", new KeybindingApplyRequest("/instances/test", "revision-1", Id),
            new KeybindingApplyResponse(receipt)];
        yield return ["keybindings.rollback", new KeybindingRollbackRequest("/instances/test", Id),
            new KeybindingRollbackResponse(receipt)];
        yield return ["actions.configuration", new ActionConfigurationRequest(1),
            new ActionConfigurationResponse(1, "/instances/test", [new("miaxion_iron", "0.1.2", "irons_spellbooks",
                new(1, "spells", "铁魔法", "ironSpell", ["casting"], ["actions.catalog", "voice.aliases.v1"],
                    [new("commands", "口令", [new("actions", "actionList", "法术", true, true, "runtime"),
                        new("aliases", "aliasList", "语音口令", true, false, "instanceProfileMode")])]), null)])];
        yield return ["actions.context", new GameActionContextRequest("casting", Id, 1, 2), new GameActionContextResponse(3)];
        yield return ["actions.context", new GameActionContextRequest("conversation", null, 0, 0), new GameActionContextResponse(4)];
        yield return ["actions.catalog", new GameActionCatalogRequest("revision-1", "next_page"),
            new GameActionCatalogResponse(Scope, "revision-1",
                [new(new KeyActionTarget(KeyId), "Perspective", true, null, "key.categories.misc", "杂项"),
                 new(new ArsPageTarget(3), "Page 3", false, "source_unavailable"),
                 new(new IronSpellTarget("irons_spellbooks:fireball"), "Fireball", true, null)], null)];
        yield return ["actions.grant", new GameActionGrantRequest(Scope,
            [new KeyActionTarget(KeyId), new ArsPageTarget(3), new IronSpellTarget("irons_spellbooks:fireball")]),
            new GameActionGrantResponse(Id, Scope, Now.AddMinutes(5))];
        yield return ["actions.revoke", new GameActionRevokeRequest(Id, Scope), new GameActionRevokeResponse(true)];
        yield return ["actions.execute", new GameActionExecuteRequest(Id, Scope, Id, Id, new ArsPageTarget(3), Now),
            new GameActionExecutionResponse(Id, new("dispatched", null))];
        yield return ["actions.status", new GameActionStatusRequest(Id, Id),
            new GameActionExecutionResponse(Id, new("rejected", "expired"))];
    }

    [Theory]
    [MemberData(nameof(Contracts))]
    public void TypedContractsSerializeIntoValidClosedEnvelopes(string action, object request, object response)
    {
        Assert.True(new ProtocolAction(action).TryGetKnown(out _));
        foreach (var (payload, isRequest) in new[] { (request, true), (response, false) })
        {
            var envelope = Envelope(action, payload, isRequest);
            AssertValid(envelope, isRequest);
            var restored = JsonSerializer.Deserialize(envelope["payload"]!.ToJsonString(), payload.GetType(), ProtocolJson.Options);
            Assert.True(JsonNode.DeepEquals(envelope["payload"], JsonSerializer.SerializeToNode(restored, ProtocolJson.Options)));
            // Every nested object is closed, including identities, contexts, targets and receipts.
            foreach (var nested in Objects(envelope["payload"]!))
            {
                nested["injected"] = true;
                AssertInvalid(envelope, isRequest);
                nested.Remove("injected");
            }
            if (!isRequest || !action.EndsWith(".catalog", StringComparison.Ordinal))
            {
                foreach (var name in ((JsonObject)envelope["payload"]!).Select(p => p.Key).ToArray())
                {
                    var missing = envelope.DeepClone(); ((JsonObject)missing["payload"]!).Remove(name);
                    AssertInvalid(missing, isRequest);
                }
            }
            envelope["payload"]!["extra"] = true;
            AssertInvalid(envelope, isRequest);
            Assert.Throws<JsonException>(() => JsonSerializer.Deserialize(envelope["payload"]!.ToJsonString(), payload.GetType(), ProtocolJson.Options));
            envelope = Envelope(action, payload, isRequest);
            envelope["unexpectedEnvelopeField"] = true;
            AssertInvalid(envelope, isRequest);
            envelope = Envelope(action, payload, isRequest);
            envelope["action"] = "actions.future";
            Assert.False(new ProtocolAction("actions.future").TryGetKnown(out _));
            // Unknown request envelopes remain parseable for the existing unavailable response path.
            if (isRequest) AssertValid(envelope, true); else AssertInvalid(envelope, false);
            envelope = Envelope(action, payload, isRequest);
            envelope["payload"] = null;
            AssertInvalid(envelope, isRequest);
            if (isRequest)
            {
                envelope = Envelope(action, payload, true);
                envelope["sessionToken"] = null;
                AssertInvalid(envelope, true);
            }
        }
    }

    [Fact]
    public void ScopeTargetsKeyValuesAndPagingRejectMalformedNestedFields()
    {
        var execute = Envelope("actions.execute", new GameActionExecuteRequest(Id, Scope, Id, Id, new ArsPageTarget(3), Now), true);
        foreach (var target in new[] { "{\"kind\":\"arsPage\",\"pageOneBased\":0}",
            "{\"kind\":\"arsPage\",\"pageOneBased\":3,\"hand\":\"main\"}",
            "{\"kind\":\"ironSpell\",\"namespacedSpellId\":\"bad id\"}",
            "{\"kind\":\"ironSpell\",\"namespacedSpellId\":\"mod:spell\",\"globalIndex\":1}",
            "{\"kind\":\"macro\",\"steps\":[]}" })
        {
            var invalid = execute.DeepClone(); invalid["payload"]!["target"] = JsonNode.Parse(target);
            AssertInvalid(invalid, true);
        }
        foreach (var property in ((JsonObject)execute["payload"]!["scope"]!).Select(p => p.Key).ToArray())
        {
            var invalid = execute.DeepClone(); ((JsonObject)invalid["payload"]!["scope"]!).Remove(property);
            AssertInvalid(invalid, true);
        }
        foreach (var revision in new JsonNode?[] { JsonValue.Create(-1), JsonValue.Create(9007199254740992L), JsonValue.Create("1"), null })
        {
            var invalid = execute.DeepClone(); invalid["payload"]!["scope"]!["grantRevision"] = revision;
            AssertInvalid(invalid, true);
        }
        var preview = Envelope("keybindings.preview", new KeybindingPreviewRequest("/instances/test", "r", Id, 1,
            [new(KeyId, new UnboundKeyValue(), Context)]), true);
        foreach (var value in new[] { "{\"kind\":\"unbound\",\"code\":0}",
            "{\"kind\":\"bound\",\"inputType\":\"keysym\",\"code\":-1,\"modifier\":\"none\"}",
            "{\"kind\":\"bound\",\"inputType\":\"mouse\",\"code\":1,\"modifier\":\"super\"}",
            "{\"kind\":\"bound\",\"inputType\":\"keysym\",\"code\":74,\"modifier\":\"none\",\"nativeConflictContext\":\"overwrite\"}" })
        {
            preview["payload"]!["bindings"]![0]!["value"] = JsonNode.Parse(value);
            AssertInvalid(preview, true);
        }
        AssertInvalid(Envelope("actions.catalog", new GameActionCatalogRequest(null, "cursor"), true), true);
        AssertInvalid(Envelope("keybindings.catalog", new KeybindingCatalogRequest("r", new string('a', 257)), true), true);
        AssertInvalid(Envelope("actions.grant", new GameActionGrantRequest(Scope, []), true), true);
        AssertInvalid(Envelope("actions.grant", new GameActionGrantRequest(Scope, [new ArsPageTarget(1), new ArsPageTarget(1)]), true), true);
        AssertInvalid(Envelope("actions.grant", new GameActionGrantRequest(Scope,
            Enumerable.Range(1, 129).Select(n => (GameActionTarget)new ArsPageTarget(n)).ToArray()), true), true);
    }

    [Fact]
    public void BridgeCandidateAndExistingTargetsKeepTheirExactIdentity()
    {
        GameActionTarget[] targets =
        [
            new KeyActionTarget(new("miaxion_bridge", "key.ftbquests.quests", "minecraft-1.21.1-neoforge")),
            new KeyActionTarget(new("minecraft", "key.togglePerspective", "minecraft-1.21.1-neoforge")),
            new KeyActionTarget(new("minecraft", "key.smoothCamera", "minecraft-1.21.1-neoforge")),
            new ArsPageTarget(3), new IronSpellTarget("irons_spellbooks:fireball")
        ];
        foreach (var target in targets)
        {
            var request = new GameActionExecuteRequest(Id, Scope, Id, Id, target, Now);
            var envelope = Envelope("actions.execute", request, true);
            AssertValid(envelope, true);
            Assert.Equal(request, ProtocolJson.Deserialize<GameActionExecuteRequest>(envelope["payload"]!.ToJsonString()));
            var catalog = new GameActionCatalogResponse(Scope, "r1", [new(target, "Candidate", false, "m1_unverified")], null);
            var response = Envelope("actions.catalog", catalog, false);
            AssertValid(response, false);
            var restored = ProtocolJson.Deserialize<GameActionCatalogResponse>(response["payload"]!.ToJsonString())!;
            Assert.Equal(target, restored.Entries[0].Target);
            Assert.False(restored.Entries[0].Available);
            Assert.Equal("m1_unverified", restored.Entries[0].UnavailableReason);
        }
    }

    [Fact]
    public void OutcomesAreObservationsAndUnknownActionsCannotReturnSuccess()
    {
        foreach (var state in new[] { "dispatched", "started", "unknown" })
            AssertValid(Envelope("actions.status", new GameActionExecutionResponse(Id, new(state, null)), false), false);
        foreach (var outcome in new[] { new GameActionOutcome("success", null), new("rejected", null), new("dispatched", "success") })
            AssertInvalid(Envelope("actions.status", new GameActionExecutionResponse(Id, outcome), false), false);
        var error = Envelope("actions.execute", new GameActionExecutionResponse(Id, new("unknown", null)), false);
        error["payload"] = null;
        error["error"] = JsonNode.Parse("{\"code\":\"UNAUTHORIZED\",\"message\":\"No action grant\",\"retryable\":false,\"details\":null}");
        AssertValid(error, false);
    }

    private static JsonNode Envelope(string action, object payload, bool request)
    {
        var element = JsonSerializer.SerializeToElement(payload, payload.GetType(), ProtocolJson.Options);
        return JsonNode.Parse(request
            ? ProtocolJson.Serialize(new RequestEnvelope("1.0", Id, Now, Now.AddSeconds(5), new(action), new string('A', 43), element, null))
            : ProtocolJson.Serialize(new ResponseEnvelope("1.0", Id, Now, Now.AddSeconds(5), new(action), null, element, null)))!;
    }

    private static void AssertValid(JsonNode envelope, bool request) => Assert.True(Valid(envelope, request), envelope.ToJsonString());
    private static IEnumerable<JsonObject> Objects(JsonNode node)
    {
        if (node is JsonObject obj)
        {
            yield return obj;
            foreach (var child in obj.Select(p => p.Value).Where(v => v is not null).ToArray())
                foreach (var nested in Objects(child!)) yield return nested;
        }
        else if (node is JsonArray array)
            foreach (var child in array.Where(v => v is not null))
                foreach (var nested in Objects(child!)) yield return nested;
    }
    private static void AssertInvalid(JsonNode envelope, bool request) => Assert.False(Valid(envelope, request), envelope.ToJsonString());
    private static bool Valid(JsonNode envelope, bool request)
    {
        using var document = JsonDocument.Parse(envelope.ToJsonString());
        return SchemaFixtureTests.Evaluate(request ? "request.schema.json" : "response.schema.json", document.RootElement).IsValid;
    }
}
