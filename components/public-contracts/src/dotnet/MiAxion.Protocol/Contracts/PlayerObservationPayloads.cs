using System.Text.Json.Serialization;

namespace MiAxion.Protocol.Contracts;

public static class PlayerObservationPayloads
{
    public sealed record Request([property: JsonIgnore(Condition = JsonIgnoreCondition.WhenWritingNull)] string? DetailsSlot = null);
    public sealed record Enchant(
        string Id,
        string? DisplayName,
        int Level,
        bool Stored = false);
    public sealed record Item(
        string ItemId,
        string? DisplayName,
        int Count,
        int? Damage,
        int? MaxDamage,
        string DurabilityStatus,
        IReadOnlyList<Enchant> Enchantments,
        string DetailsStatus);
    public sealed record Slot(
        string SlotId,
        string Source,
        string Area,
        int Index,
        int? LegacySlot,
        int? ContainerSlot,
        string Status,
        Item? Item,
        string? FailureCode);
    public sealed record Equipment(
        string Location,
        string? SlotId,
        string Status);
    public sealed record Details(
        string SlotId,
        string Status,
        IReadOnlyList<string> TooltipLines);
    public sealed record BlockPosition(
        int X,
        int Y,
        int Z);
    public sealed record Position(
        double X,
        double Y,
        double Z);
    public sealed record Effect(
        string Id,
        string? DisplayName,
        int Level,
        int? DurationTicks,
        bool Infinite);
    public sealed record StateProperty(
        string Name,
        string Value);
    public sealed record ScreenText(
        string Source,
        string ElementId,
        string Kind,
        string Text,
        int? X,
        int? Y,
        bool? Visible,
        bool? Active,
        IReadOnlyList<string> TooltipLines);
    public sealed record InventoryView(
        string CapturedAt,
        string Status,
        IReadOnlyList<string> Sources,
        int? SelectedSlot,
        IReadOnlyList<Slot> Slots,
        IReadOnlyList<Equipment> Equipment,
        Details? Details);
    public sealed record PlayerStatus(
        string CapturedAt,
        string Status,
        PlayerSnapshotResponse Player,
        IReadOnlyList<Effect> Effects,
        double? Armor,
        double? Toughness,
        double? ExperienceProgress,
        double? Saturation,
        int? Air,
        int? MaxAir,
        IReadOnlyList<string> UnknownFields);
    public sealed record WorldContext(
        string CapturedAt,
        string Status,
        string DimensionId,
        Position Position,
        BlockPosition BlockPosition,
        BlockPosition ChunkPosition,
        BlockPosition RelativePosition,
        string Direction,
        double Yaw,
        double Pitch,
        string? BiomeId,
        int? SkyLight,
        int? BlockLight,
        int? CombinedLight,
        long GameTime,
        long DayTime,
        bool Daylight,
        string Weather,
        IReadOnlyList<string> UnknownFields);
    public sealed record ScreenSnapshot(
        string CapturedAt,
        string Status,
        string? Title,
        string? ScreenType,
        int? MenuId,
        IReadOnlyList<Slot> Slots,
        Item? Carried,
        IReadOnlyList<ScreenText> Elements,
        Details? Details,
        IReadOnlyList<string> CoverageNotes);
    public sealed record TargetSnapshot(
        string CapturedAt,
        string Status,
        string? Kind,
        string? RegistryId,
        string? DisplayName,
        BlockPosition? BlockPosition,
        int? EntityId,
        IReadOnlyList<StateProperty> Properties);
}
