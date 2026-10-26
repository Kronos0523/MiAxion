using System.Text.Json.Serialization;

namespace MiAxion.Protocol.Contracts;

// Z1 keeps world-query identity and bounds platform-neutral; no Minecraft or Bridge types cross this boundary.
public enum WorldQueryAuthority
{
    [JsonStringEnumMemberName("generator_located")]
    GeneratorLocated,

    [JsonStringEnumMemberName("loaded_observed")]
    LoadedObserved
}

public enum WorldLocatorRange
{
    [JsonStringEnumMemberName("near")]
    Near,

    [JsonStringEnumMemberName("standard")]
    Standard,

    [JsonStringEnumMemberName("far")]
    Far
}

public enum WorldChunkRange
{
    [JsonStringEnumMemberName("1x1")]
    OneByOne,

    [JsonStringEnumMemberName("3x3")]
    ThreeByThree,

    [JsonStringEnumMemberName("5x5")]
    FiveByFive
}

public enum WorldVerticalRange
{
    [JsonStringEnumMemberName("10")]
    Ten,

    [JsonStringEnumMemberName("50")]
    Fifty
}

public enum WorldRegistryKind
{
    [JsonStringEnumMemberName("structure")]
    Structure,

    [JsonStringEnumMemberName("biome")]
    Biome
}

public enum WorldQueryTargetKind
{
    [JsonStringEnumMemberName("metal_storage_block")]
    MetalStorageBlock,

    [JsonStringEnumMemberName("loot_container")]
    LootContainer,

    [JsonStringEnumMemberName("user_block")]
    UserBlock
}

public enum WorldQueryProgressLane
{
    [JsonStringEnumMemberName("locator")]
    Locator,

    [JsonStringEnumMemberName("nearby")]
    Nearby
}

public sealed record WorldQueryProgressRequest
{
    [JsonConstructor]
    public WorldQueryProgressRequest(WorldQueryProgressLane lane) => Lane = lane;

    public WorldQueryProgressLane Lane { get; }
}

public enum WorldMachineState
{
    [JsonStringEnumMemberName("running")]
    Running,

    [JsonStringEnumMemberName("idle")]
    Idle,

    [JsonStringEnumMemberName("blocked")]
    Blocked,

    [JsonStringEnumMemberName("offline")]
    Offline,

    [JsonStringEnumMemberName("unknown")]
    Unknown
}

public enum WorldMachineCandidateDomain
{
    [JsonStringEnumMemberName("block_entity_only")]
    BlockEntityOnly
}

public enum WorldContainerView
{
    [JsonStringEnumMemberName("full")]
    Full,

    [JsonStringEnumMemberName("automation_unsided")]
    AutomationUnsided,

    [JsonStringEnumMemberName("automation_sided")]
    AutomationSided,

    [JsonStringEnumMemberName("loot_ungenerated")]
    LootUngenerated,

    [JsonStringEnumMemberName("locked")]
    Locked,

    [JsonStringEnumMemberName("unavailable")]
    Unavailable
}

public sealed record WorldLocateRequest
{
    [JsonConstructor]
    public WorldLocateRequest(string targetId, WorldLocatorRange range)
    {
        RecipeContractValidation.RequireResourceLocation(targetId, nameof(targetId));
        TargetId = targetId;
        Range = range;
    }

    public string TargetId { get; }
    public WorldLocatorRange Range { get; }
}

public sealed record WorldNearbyScanRequest
{
    [JsonConstructor]
    public WorldNearbyScanRequest(
        WorldChunkRange chunkRange = WorldChunkRange.ThreeByThree,
        WorldVerticalRange verticalRange = WorldVerticalRange.Ten,
        WorldScanWindow? window = null,
        string? pageCursor = null)
    {
        ChunkRange = chunkRange;
        VerticalRange = verticalRange;
        Window = window;
        RequirePageCursor(pageCursor, allowStart: true);
        PageCursor = pageCursor;
    }

    public WorldChunkRange ChunkRange { get; }
    public WorldVerticalRange VerticalRange { get; }
    [JsonIgnore(Condition = JsonIgnoreCondition.WhenWritingNull)]
    public WorldScanWindow? Window { get; }
    // Null preserves the legacy request. Empty explicitly starts paged reading.
    [JsonIgnore(Condition = JsonIgnoreCondition.WhenWritingNull)]
    public string? PageCursor { get; }

    internal static void RequirePageCursor(string? value, bool allowStart)
    {
        if (value is null || (allowStart && value.Length == 0)) return;
        if (value.Length != 32 || value.Any(static c => c is not (>= '0' and <= '9') and not (>= 'a' and <= 'f')))
            throw new ArgumentException("Invalid world page cursor.", nameof(value));
    }
}

public sealed record WorldStructureTargetsRequest
{
    [JsonConstructor]
    public WorldStructureTargetsRequest(
        string? structureId = null,
        string? blockId = null,
        string? pageCursor = null)
    {
        if (structureId is not null)
            RecipeContractValidation.RequireResourceLocation(structureId, nameof(structureId));
        if (blockId is not null)
            RecipeContractValidation.RequireResourceLocation(blockId, nameof(blockId));
        WorldNearbyScanRequest.RequirePageCursor(pageCursor, allowStart: true);
        StructureId = structureId;
        BlockId = blockId;
        PageCursor = pageCursor;
    }

    [JsonIgnore(Condition = JsonIgnoreCondition.WhenWritingNull)]
    public string? StructureId { get; }

    [JsonIgnore(Condition = JsonIgnoreCondition.WhenWritingNull)]
    public string? BlockId { get; }

    // Null preserves the legacy one-shot request; an empty string explicitly
    // starts a structure target continuation-capable query.
    [JsonIgnore(Condition = JsonIgnoreCondition.WhenWritingNull)]
    public string? PageCursor { get; }
}

public sealed record WorldRegistryResolveRequest
{
    [JsonConstructor]
    public WorldRegistryResolveRequest(WorldRegistryKind kind, string query)
    {
        ArgumentNullException.ThrowIfNull(query);
        EntityTextValidation.Require(query, nameof(query));
        if (query.EnumerateRunes().Count() is < 1 or > 512)
            throw new ArgumentException("Query must contain 1-512 Unicode scalars.", nameof(query));
        Kind = kind;
        Query = query;
    }

    public WorldRegistryKind Kind { get; }
    public string Query { get; }
}

public sealed record WorldRegistryReference
{
    [JsonConstructor]
    public WorldRegistryReference(WorldRegistryKind kind, string resourceId)
    {
        RecipeContractValidation.RequireResourceLocation(resourceId, nameof(resourceId));
        Kind = kind;
        ResourceId = resourceId;
    }

    public WorldRegistryKind Kind { get; }
    public string ResourceId { get; }

    internal string Key => $"{Kind}\0{ResourceId}";
}

public sealed record WorldRegistryDescribeRequest
{
    [JsonConstructor]
    public WorldRegistryDescribeRequest(IReadOnlyList<WorldRegistryReference> entries)
    {
        ArgumentNullException.ThrowIfNull(entries);
        if (entries is not { Count: >= 1 and <= 128 } || entries.Any(static entry => entry is null))
            throw new ArgumentException("Entries must contain 1-128 non-null references.", nameof(entries));
        var seen = new HashSet<string>(StringComparer.Ordinal);
        foreach (var entry in entries)
        {
            if (!seen.Add(entry.Key))
                throw new ArgumentException("Entries must be unique kind/resourceId pairs.", nameof(entries));
        }
        Entries = entries.ToArray();
    }

    public IReadOnlyList<WorldRegistryReference> Entries { get; }
}
