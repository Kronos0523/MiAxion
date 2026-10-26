using System.Text.Json.Serialization;

namespace MiAxion.Protocol.Contracts;

// Wire contracts only. Schema validation precedes binding; these do not authorize writes.
public sealed record KeyActionId(string ProviderId, string RawActionId, string CompatibilityFamily);

[JsonPolymorphic(TypeDiscriminatorPropertyName = "kind")]
[JsonDerivedType(typeof(UnboundKeyValue), "unbound")]
[JsonDerivedType(typeof(BoundKeyValue), "bound")]
public abstract record KeyValue;
public sealed record UnboundKeyValue : KeyValue;
public sealed record BoundKeyValue(string InputType, int Code, string Modifier) : KeyValue;

// Compatibility evidence is read-only: apply/rollback never write native conflict contexts.
public sealed record KeyContextMetadata(string NativeConflictContext, string CompatibilityEvidence);
public sealed record KeybindingSelection(KeyActionId Id, KeyValue Value, KeyContextMetadata Context);

public sealed record KeybindingCatalogRequest(
    [property: JsonIgnore(Condition = JsonIgnoreCondition.WhenWritingNull)] string? CatalogRevision = null,
    [property: JsonIgnore(Condition = JsonIgnoreCondition.WhenWritingNull)] string? Continuation = null);
public sealed record KeybindingCatalogEntry(
    string RawActionId, KeyActionId? Id, string DisplayName, KeyValue? CurrentValue,
    KeyContextMetadata? Context, bool Available, string? UnavailableReason);
public sealed record KeybindingCatalogResponse(
    string InstanceDirectory, string CatalogRevision,
    IReadOnlyList<KeybindingCatalogEntry> Entries, string? NextContinuation);
public sealed record KeybindingPreviewRequest(
    string InstanceDirectory, string CatalogRevision, Guid ProfileId, long ProfileRevision,
    IReadOnlyList<KeybindingSelection> Bindings);
public sealed record KeybindingPreviewEntry(
    KeyActionId Id, KeyValue? CurrentValue, KeyValue TargetValue, string Status, string? Reason);
public sealed record KeybindingPreviewResponse(
    Guid PreviewToken, string InstanceDirectory, string CatalogRevision,
    IReadOnlyList<KeybindingPreviewEntry> Entries);
// The opaque server-held preview freezes the selected identities, contexts and old/new values.
public sealed record KeybindingApplyRequest(string InstanceDirectory, string CatalogRevision, Guid PreviewToken);
public sealed record KeybindingRollbackRequest(string InstanceDirectory, Guid ReceiptId);
public sealed record KeybindingChange(KeyActionId Id, KeyValue OldValue, KeyValue NewValue);
public sealed record KeybindingSkip(KeyActionId Id, string Reason);
public sealed record KeybindingReceipt(
    Guid ReceiptId, string InstanceDirectory, string FinalCatalogRevision,
    IReadOnlyList<KeybindingChange> Changed, IReadOnlyList<KeybindingSkip> Skipped,
    string Status, DateTimeOffset CompletedAt);
public sealed record KeybindingApplyResponse(KeybindingReceipt Receipt);
public sealed record KeybindingRollbackResponse(KeybindingReceipt Receipt);
