using System.Text.Json.Serialization;

namespace MiAxion.Protocol.Contracts;

[JsonPolymorphic(TypeDiscriminatorPropertyName = "kind")]
[JsonDerivedType(typeof(KeyActionTarget), "keyAction")]
[JsonDerivedType(typeof(ArsPageTarget), "arsPage")]
[JsonDerivedType(typeof(IronSpellTarget), "ironSpell")]
public abstract record GameActionTarget;
public sealed record KeyActionTarget(KeyActionId Id) : GameActionTarget;
public sealed record ArsPageTarget(int PageOneBased) : GameActionTarget;
public sealed record IronSpellTarget(string NamespacedSpellId) : GameActionTarget;

// All scope fields are assertions, not authority: the eventual service must verify its own lease.
// No source hand/book identity or transient Iron globalIndex is persisted in a target.
public sealed record GameActionScope(
    string InstanceDirectory, long BridgeSessionGeneration, long WorldEpoch, Guid LocalPlayerUuid,
    string Mode, Guid ProfileId, long ProfileRevision, long MappingRevision, long GrantRevision);
public sealed record GameActionContextRequest(string Mode, Guid? ProfileId, long ProfileRevision, long MappingRevision);
public sealed record GameActionContextResponse(long GrantRevision);
public sealed record GameActionCatalogRequest(
    [property: JsonIgnore(Condition = JsonIgnoreCondition.WhenWritingNull)] string? CatalogRevision = null,
    [property: JsonIgnore(Condition = JsonIgnoreCondition.WhenWritingNull)] string? Continuation = null);
public sealed record GameActionCatalogEntry(
    GameActionTarget Target, string DisplayName, bool Available, string? UnavailableReason,
    [property: JsonIgnore(Condition = JsonIgnoreCondition.WhenWritingNull)] string? CategoryId = null,
    [property: JsonIgnore(Condition = JsonIgnoreCondition.WhenWritingNull)] string? CategoryDisplayName = null);
public sealed record GameActionCatalogResponse(
    GameActionScope Scope, string CatalogRevision, IReadOnlyList<GameActionCatalogEntry> Entries,
    string? NextContinuation);
public sealed record GameActionGrantRequest(GameActionScope Scope, IReadOnlyList<GameActionTarget> Targets);
public sealed record GameActionGrantResponse(Guid GrantId, GameActionScope Scope, DateTimeOffset ExpiresAt);
public sealed record GameActionRevokeRequest(Guid GrantId, GameActionScope Scope);
public sealed record GameActionRevokeResponse(bool Revoked);
// The existing envelope deadline is the absolute execution deadline, never a renewable payload timer.
public sealed record GameActionExecuteRequest(
    Guid GrantId, GameActionScope Scope, Guid CaptureId, Guid ExecutionId,
    GameActionTarget Target, DateTimeOffset ReleasedAt);
public sealed record GameActionStatusRequest(Guid GrantId, Guid ExecutionId);
public sealed record GameActionOutcome(string State, string? Reason);
public sealed record GameActionExecutionResponse(Guid ExecutionId, GameActionOutcome Outcome);
