namespace MiAxion.Protocol.Contracts;

/// <summary>Paired-companion terminology transport. These contracts do not authorize a game read or Provider disclosure.</summary>
public sealed record TerminologyConfigureRequest(TerminologyCaptureSettings Settings);
public sealed record TerminologySnapshotRequest;
public sealed record TerminologyClearRequest;
public sealed record TerminologyCaptureSettings(bool Enabled, IReadOnlyList<string> Sources, int Capacity,
    int BrowserWindowSeconds, int RetentionSeconds);
/// <summary>IngredientTypeUid is JEI's original type identifier (often a Java class name), never an instance UID.</summary>
public sealed record TerminologyIdentity(string Kind, string ResourceId, string? IngredientTypeUid = null);
public sealed record TerminologyEntry(TerminologyIdentity? Identity, string DisplayName,
    IReadOnlyList<string> Sources, DateTimeOffset ObservedAtUtc);
public sealed record TerminologyBrowserGroup(long GroupId, IReadOnlyList<TerminologyEntry> Entries,
    long RemainingRetentionMilliseconds);
public sealed record ObservedPotionEffectPayload(string ResourceId, int Amplifier, string DurationKind, int? DurationTicks);
public sealed record ObservedItemDetailsPayload(TerminologyIdentity Identity, string DisplayName, string Source,
    DateTimeOffset ObservedAtUtc, string TooltipStatus, IReadOnlyList<string> Tooltip,
    string EffectsStatus, IReadOnlyList<ObservedPotionEffectPayload> Effects);
public sealed record TerminologySnapshotPayload(long ProducerGeneration, long Revision, string Language,
    TerminologyCaptureSettings CaptureSettings, IReadOnlyList<TerminologyEntry> Entries,
    IReadOnlyList<TerminologyBrowserGroup> BrowserGroups,
    IReadOnlyDictionary<string, string> SourceAvailability, bool Truncated,
    [property: System.Text.Json.Serialization.JsonIgnore(Condition = System.Text.Json.Serialization.JsonIgnoreCondition.WhenWritingNull)]
    ObservedItemDetailsPayload? ItemDetails = null);
/// <summary>Not a response envelope: no request ID exists, so it must be routed before ordinary response correlation.</summary>
public sealed record TerminologyChangedEnvelope(string ProtocolVersion, ProtocolAction Action,
    TerminologySnapshotPayload Payload);
