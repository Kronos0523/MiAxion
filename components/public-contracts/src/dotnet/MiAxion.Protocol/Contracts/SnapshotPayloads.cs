using System.Text.Json.Serialization;

namespace MiAxion.Protocol.Contracts;

public sealed record InventorySnapshotRequest(bool IncludeEmpty = false);

public sealed record InventorySlot(
    int Slot,
    bool Empty,
    string? ItemId = null,
    int? Count = null);

public sealed record InventorySnapshotResponse(
    string Revision,
    IReadOnlyList<InventorySlot> Slots);

public sealed record PlayerSnapshotRequest;

public sealed record Position(
    double X,
    double Y,
    double Z);

public enum GameMode
{
    [JsonStringEnumMemberName("survival")]
    Survival,

    [JsonStringEnumMemberName("creative")]
    Creative,

    [JsonStringEnumMemberName("adventure")]
    Adventure,

    [JsonStringEnumMemberName("spectator")]
    Spectator
}

public sealed record PlayerSnapshotResponse(
    string DimensionId,
    Position Position,
    double Health,
    double Food,
    int ExperienceLevel,
    GameMode GameMode,
    [property: JsonIgnore(Condition = JsonIgnoreCondition.WhenWritingNull)]
    double? MaxHealth = null);
