using System.Text.Json.Serialization;
using System.Text.RegularExpressions;

namespace MiAxion.Protocol.Contracts;

// Response shapes are separated by authority so a generator locator cannot imply a loaded-world observation.
public sealed record WorldQuerySummary
{
    [JsonConstructor]
    public WorldQuerySummary(
        WorldQueryAuthority authority,
        string dimensionId,
        long observedAtTick,
        string queryGeneration,
        int requested,
        int loaded,
        int examined,
        int matched,
        bool complete,
        bool truncated,
        IReadOnlyList<string> warnings)
    {
        RecipeContractValidation.RequireResourceLocation(dimensionId, nameof(dimensionId));
        ArgumentOutOfRangeException.ThrowIfNegative(observedAtTick);
        WorldQueryValidation.RequireOpaqueId(queryGeneration, nameof(queryGeneration));
        // Examined counts work already performed; response collections retain their own limits.
        if (requested is < 0 or > 1024 || loaded is < 0 or > 1024 ||
            examined < 0 || matched is < 0 or > 1024)
            throw new ArgumentOutOfRangeException(nameof(requested));
        ArgumentNullException.ThrowIfNull(warnings);
        if (warnings.Count > ProtocolConstants.WorldQueryWarningMaximum || warnings.Any(static warning => warning is null))
            throw new ArgumentException("Warnings must contain at most 16 values.", nameof(warnings));
        var seen = new HashSet<string>(StringComparer.Ordinal);
        foreach (var warning in warnings)
        {
            if (warning.Length is < 1 or > 64 ||
                !Regex.IsMatch(warning, "^[A-Z][A-Z0-9_]*$", RegexOptions.CultureInvariant) ||
                !seen.Add(warning))
                throw new ArgumentException("Warnings must be unique uppercase codes.", nameof(warnings));
        }

        Authority = authority;
        DimensionId = dimensionId;
        ObservedAtTick = observedAtTick;
        QueryGeneration = queryGeneration;
        Requested = requested;
        Loaded = loaded;
        Examined = examined;
        Matched = matched;
        Complete = complete;
        Truncated = truncated;
        Warnings = warnings.ToArray();
    }

    public WorldQueryAuthority Authority { get; }
    public string DimensionId { get; }
    public long ObservedAtTick { get; }
    public string QueryGeneration { get; }
    public int Requested { get; }
    public int Loaded { get; }
    public int Examined { get; }
    public int Matched { get; }
    public bool Complete { get; }
    public bool Truncated { get; }
    public IReadOnlyList<string> Warnings { get; }
}

public sealed record WorldBlockPosition
{
    [JsonConstructor]
    public WorldBlockPosition(int x, int y, int z)
    {
        if (x is < -30_000_000 or > 30_000_000 ||
            y is < -2048 or > 2048 ||
            z is < -30_000_000 or > 30_000_000)
            throw new ArgumentOutOfRangeException(nameof(x));
        X = x;
        Y = y;
        Z = z;
    }

    public int X { get; }
    public int Y { get; }
    public int Z { get; }

    internal string Key => $"{X}:{Y}:{Z}";
}

public sealed record WorldLocatedResponse
{
    [JsonConstructor]
    public WorldLocatedResponse(
        WorldQuerySummary summary,
        string targetId,
        string displayName,
        int x,
        int z,
        int? y,
        int distanceBlocks,
        WorldLocatorRange radius,
        int queryCount)
    {
        ArgumentNullException.ThrowIfNull(summary);
        if (summary.Authority != WorldQueryAuthority.GeneratorLocated)
            throw new ArgumentException("Locator responses require generator authority.", nameof(summary));
        RecipeContractValidation.RequireResourceLocation(targetId, nameof(targetId));
        RequireDisplayName(displayName);
        _ = new WorldBlockPosition(x, y ?? 0, z);
        if (distanceBlocks < 0 || queryCount is < 1 or > 1024)
            throw new ArgumentOutOfRangeException(nameof(distanceBlocks));
        Summary = summary;
        TargetId = targetId;
        DisplayName = displayName;
        X = x;
        Z = z;
        Y = y;
        DistanceBlocks = distanceBlocks;
        Radius = radius;
        QueryCount = queryCount;
    }

    public WorldQuerySummary Summary { get; }
    public string TargetId { get; }
    public string DisplayName { get; }
    public int X { get; }
    public int Z { get; }
    public int? Y { get; }
    public int DistanceBlocks { get; }
    public WorldLocatorRange Radius { get; }
    public int QueryCount { get; }

    private static void RequireDisplayName(string value)
    {
        ArgumentNullException.ThrowIfNull(value);
        EntityTextValidation.Require(value, nameof(value));
        if (value.EnumerateRunes().Count() is < 1 or > 256)
            throw new ArgumentException("Display name is outside its bounds.", nameof(value));
    }
}

public sealed record WorldMachineObservation
{
    [JsonConstructor]
    public WorldMachineObservation(
        string blockId,
        string displayName,
        WorldBlockPosition position,
        int distanceBlocks,
        string adapterId,
        WorldMachineCandidateDomain candidateDomain,
        WorldMachineState state,
        IReadOnlyList<WorldMachineFeature>? features = null)
    {
        RecipeContractValidation.RequireResourceLocation(blockId, nameof(blockId));
        RequireDisplayName(displayName);
        ArgumentNullException.ThrowIfNull(position);
        WorldQueryValidation.RequireOpaqueId(adapterId, nameof(adapterId));
        ArgumentOutOfRangeException.ThrowIfNegative(distanceBlocks);
        features ??= Array.Empty<WorldMachineFeature>();
        if (features.Count > ProtocolConstants.WorldQueryMachineFeatureMaximum ||
            features.Any(static feature => feature is null))
            throw new ArgumentException("Machine features exceed their bounds.", nameof(features));
        var orderedFeatures = features.OrderBy(static feature => feature.Id).ToArray();
        if (orderedFeatures.Select(static feature => feature.Id)
            .Distinct(StringComparer.Ordinal).Count() != orderedFeatures.Length)
            throw new ArgumentException("Machine feature IDs must be unique.", nameof(features));
        BlockId = blockId;
        DisplayName = displayName;
        Position = position;
        DistanceBlocks = distanceBlocks;
        AdapterId = adapterId;
        CandidateDomain = candidateDomain;
        State = state;
        Features = orderedFeatures.Length == 0 ? null : orderedFeatures;
    }

    public string BlockId { get; }
    public string DisplayName { get; }
    public WorldBlockPosition Position { get; }
    public int DistanceBlocks { get; }
    public string AdapterId { get; }
    public WorldMachineCandidateDomain CandidateDomain { get; }
    public WorldMachineState State { get; }
    [JsonIgnore(Condition = JsonIgnoreCondition.WhenWritingNull)]
    public IReadOnlyList<WorldMachineFeature>? Features { get; }

    internal string Key => $"{BlockId}\0{Position.Key}";

    private static void RequireDisplayName(string value)
    {
        ArgumentNullException.ThrowIfNull(value);
        EntityTextValidation.Require(value, nameof(value));
        if (value.EnumerateRunes().Count() is < 1 or > 256)
            throw new ArgumentException("Display name is outside its bounds.", nameof(value));
    }
}

public sealed record WorldMachineFeature
{
    [JsonConstructor]
    public WorldMachineFeature(
        string id,
        string unit,
        long value,
        long? capacity = null,
        string? resourceId = null)
    {
        WorldQueryValidation.RequireOpaqueId(id, nameof(id));
        WorldQueryValidation.RequireOpaqueId(unit, nameof(unit));
        if (unit.Length > 32)
            throw new ArgumentException(
                "Machine feature unit is outside its bounds.", nameof(unit));
        if (value is < 0 or > ProtocolConstants.MaximumSafeJsonInteger)
            throw new ArgumentOutOfRangeException(nameof(value));
        if (capacity is < 0 or > ProtocolConstants.MaximumSafeJsonInteger ||
            (capacity is not null && capacity < value))
            throw new ArgumentOutOfRangeException(nameof(capacity));
        if (resourceId is not null)
            RecipeContractValidation.RequireResourceLocation(resourceId, nameof(resourceId));
        Id = id;
        Unit = unit;
        Value = value;
        Capacity = capacity;
        ResourceId = resourceId;
    }

    public string Id { get; }
    public string Unit { get; }
    public long Value { get; }
    [JsonIgnore(Condition = JsonIgnoreCondition.WhenWritingNull)]
    public long? Capacity { get; }
    [JsonIgnore(Condition = JsonIgnoreCondition.WhenWritingNull)]
    public string? ResourceId { get; }
}

public sealed record WorldContainerItem
{
    [JsonConstructor]
    public WorldContainerItem(
        int slot,
        string itemId,
        int count,
        string? componentIdentity = null)
    {
        if (slot is < 0 or > 255)
            throw new ArgumentOutOfRangeException(nameof(slot));
        RecipeContractValidation.RequireResourceLocation(itemId, nameof(itemId));
        ArgumentOutOfRangeException.ThrowIfLessThan(count, 1);
        if (componentIdentity is not null)
        {
            EntityTextValidation.Require(componentIdentity, nameof(componentIdentity));
            if (componentIdentity.EnumerateRunes().Count() > 512)
                throw new ArgumentException("Component identity is outside its bounds.", nameof(componentIdentity));
        }
        Slot = slot;
        ItemId = itemId;
        Count = count;
        ComponentIdentity = componentIdentity;
    }

    public int Slot { get; }
    public string ItemId { get; }
    public int Count { get; }

    [JsonIgnore(Condition = JsonIgnoreCondition.WhenWritingNull)]
    public string? ComponentIdentity { get; }
}

public sealed record WorldContainerSide
{
    private static readonly string[] SideOrder = ["down", "up", "north", "south", "west", "east"];

    [JsonConstructor]
    public WorldContainerSide(string side, IReadOnlyList<WorldContainerItem> stacks)
    {
        if (Array.IndexOf(SideOrder, side) < 0)
            throw new ArgumentException("Unknown container side.", nameof(side));
        ArgumentNullException.ThrowIfNull(stacks);
        if (stacks.Count > 256 || stacks.Any(static stack => stack is null))
            throw new ArgumentException("Container side exceeds its bounds.", nameof(stacks));
        var ordered = stacks.OrderBy(static stack => stack.Slot).ToArray();
        if (ordered.Select(static stack => stack.Slot).Distinct().Count() != ordered.Length)
            throw new ArgumentException("Container side contains duplicate slots.", nameof(stacks));
        Side = side;
        Stacks = ordered;
    }

    public string Side { get; }
    public IReadOnlyList<WorldContainerItem> Stacks { get; }

    internal static int Order(string side) => Array.IndexOf(SideOrder, side);
}

public sealed record WorldContainerObservation
{
    [JsonConstructor]
    public WorldContainerObservation(
        string blockId,
        string displayName,
        WorldBlockPosition position,
        int distanceBlocks,
        WorldContainerView view,
        IReadOnlyList<WorldContainerItem>? stacks = null,
        IReadOnlyList<WorldContainerSide>? sides = null)
    {
        RecipeContractValidation.RequireResourceLocation(blockId, nameof(blockId));
        ArgumentNullException.ThrowIfNull(displayName);
        EntityTextValidation.Require(displayName, nameof(displayName));
        if (displayName.EnumerateRunes().Count() is < 1 or > 256)
            throw new ArgumentException("Display name is outside its bounds.", nameof(displayName));
        ArgumentNullException.ThrowIfNull(position);
        ArgumentOutOfRangeException.ThrowIfNegative(distanceBlocks);
        BlockId = blockId;
        DisplayName = displayName;
        Position = position;
        DistanceBlocks = distanceBlocks;
        View = view;
        // Nearby container responses are metadata-only. Keep the legacy
        // members for wire compatibility with older clients, but never retain
        // slot/item/component data in the current contract.
        Stacks = null;
        Sides = null;
    }

    public string BlockId { get; }
    public string DisplayName { get; }
    public WorldBlockPosition Position { get; }
    public int DistanceBlocks { get; }
    public WorldContainerView View { get; }

    [JsonIgnore(Condition = JsonIgnoreCondition.WhenWritingNull)]
    public IReadOnlyList<WorldContainerItem>? Stacks { get; }

    [JsonIgnore(Condition = JsonIgnoreCondition.WhenWritingNull)]
    public IReadOnlyList<WorldContainerSide>? Sides { get; }

    internal string Key => $"{BlockId}\0{Position.Key}";
}

public sealed record WorldTargetObservation
{
    [JsonConstructor]
    public WorldTargetObservation(
        string targetId,
        string displayName,
        WorldBlockPosition position,
        int distanceBlocks,
        WorldQueryTargetKind kind,
        int priority)
    {
        RecipeContractValidation.RequireResourceLocation(targetId, nameof(targetId));
        ArgumentNullException.ThrowIfNull(displayName);
        EntityTextValidation.Require(displayName, nameof(displayName));
        if (displayName.EnumerateRunes().Count() is < 1 or > 256)
            throw new ArgumentException("Display name is outside its bounds.", nameof(displayName));
        ArgumentNullException.ThrowIfNull(position);
        if (distanceBlocks < 0 || priority is < 0 or > 1000)
            throw new ArgumentOutOfRangeException(nameof(distanceBlocks));
        TargetId = targetId;
        DisplayName = displayName;
        Position = position;
        DistanceBlocks = distanceBlocks;
        Kind = kind;
        Priority = priority;
    }

    public string TargetId { get; }
    public string DisplayName { get; }
    public WorldBlockPosition Position { get; }
    public int DistanceBlocks { get; }
    public WorldQueryTargetKind Kind { get; }
    public int Priority { get; }

    internal string Key => $"{TargetId}\0{Position.Key}";
}

public sealed record WorldRegistryMatch
{
    [JsonConstructor]
    public WorldRegistryMatch(
        WorldRegistryKind kind,
        string resourceId,
        string translationKey,
        string displayName)
    {
        RecipeContractValidation.RequireResourceLocation(resourceId, nameof(resourceId));
        RequireText(translationKey, nameof(translationKey));
        RequireText(displayName, nameof(displayName));
        Kind = kind;
        ResourceId = resourceId;
        TranslationKey = translationKey;
        DisplayName = displayName;
    }

    public WorldRegistryKind Kind { get; }
    public string ResourceId { get; }
    public string TranslationKey { get; }
    public string DisplayName { get; }

    internal string Key => $"{Kind}\0{ResourceId}";

    private static void RequireText(string value, string parameterName)
    {
        ArgumentNullException.ThrowIfNull(value);
        EntityTextValidation.Require(value, parameterName);
        if (value.EnumerateRunes().Count() is < 1 or > 256)
            throw new ArgumentException("Registry text is outside its bounds.", parameterName);
    }
}

public sealed record WorldMachinesResponse
{
    [JsonConstructor]
    public WorldMachinesResponse(
        WorldQuerySummary summary,
        WorldChunkRange chunkRange,
        WorldVerticalRange verticalRange,
        IReadOnlyList<WorldMachineObservation> machines,
        WorldScanWindow? window = null,
        string? nextCursor = null)
    {
        RequireNearbySummary(summary);
        RequireContinuation(summary, nextCursor);
        NextCursor = nextCursor;
        Machines = CopyUnique(
            machines,
            ProtocolConstants.WorldQueryResultMaximum,
            static machine => machine.Key,
            nameof(machines));
        Window = window;
        Summary = summary;
        ChunkRange = chunkRange;
        VerticalRange = verticalRange;
    }

    public WorldQuerySummary Summary { get; }
    public WorldChunkRange ChunkRange { get; }
    public WorldVerticalRange VerticalRange { get; }
    public IReadOnlyList<WorldMachineObservation> Machines { get; }
    [JsonIgnore(Condition = JsonIgnoreCondition.WhenWritingNull)]
    public string? NextCursor { get; }
    [JsonIgnore(Condition = JsonIgnoreCondition.WhenWritingNull)]
    public WorldScanWindow? Window { get; }

    internal static void RequireNearbySummary(WorldQuerySummary summary)
    {
        ArgumentNullException.ThrowIfNull(summary);
        if (summary.Authority != WorldQueryAuthority.LoadedObserved)
            throw new ArgumentException("Nearby responses require loaded authority.", nameof(summary));
    }

    internal static void RequireContinuation(WorldQuerySummary summary, string? nextCursor)
    {
        WorldNearbyScanRequest.RequirePageCursor(nextCursor, allowStart: false);
        if (nextCursor is not null && (summary.Complete || !summary.Truncated))
            throw new ArgumentException("A continuation requires an incomplete result page.", nameof(nextCursor));
    }

    internal static IReadOnlyList<T> CopyUnique<T>(
        IReadOnlyList<T> values,
        int maximum,
        Func<T, string> key,
        string parameterName)
    {
        ArgumentNullException.ThrowIfNull(values, parameterName);
        if (values.Count > maximum || values.Any(static value => value is null))
            throw new ArgumentException("Result collection is outside its bounds.", parameterName);
        var copy = values.ToArray();
        var seen = new HashSet<string>(StringComparer.Ordinal);
        foreach (var value in copy)
        {
            if (!seen.Add(key(value)))
                throw new ArgumentException("Result collection contains duplicate identities.", parameterName);
        }
        return copy;
    }
}

public sealed record WorldContainersResponse
{
    [JsonConstructor]
    public WorldContainersResponse(
        WorldQuerySummary summary,
        WorldChunkRange chunkRange,
        WorldVerticalRange verticalRange,
        IReadOnlyList<WorldContainerObservation> containers,
        WorldScanWindow? window = null,
        string? nextCursor = null)
    {
        WorldMachinesResponse.RequireNearbySummary(summary);
        WorldMachinesResponse.RequireContinuation(summary, nextCursor);
        NextCursor = nextCursor;
        Containers = WorldMachinesResponse.CopyUnique(
            containers,
            ProtocolConstants.WorldQueryResultMaximum,
            static container => container.Key,
            nameof(containers));
        Window = window;
        Summary = summary;
        ChunkRange = chunkRange;
        VerticalRange = verticalRange;
    }

    public WorldQuerySummary Summary { get; }
    public WorldChunkRange ChunkRange { get; }
    public WorldVerticalRange VerticalRange { get; }
    public IReadOnlyList<WorldContainerObservation> Containers { get; }
    [JsonIgnore(Condition = JsonIgnoreCondition.WhenWritingNull)]
    public string? NextCursor { get; }
    [JsonIgnore(Condition = JsonIgnoreCondition.WhenWritingNull)]
    public WorldScanWindow? Window { get; }
}

public sealed record WorldStructureTargetsResponse
{
    [JsonConstructor]
    public WorldStructureTargetsResponse(
        WorldQuerySummary summary,
        string structureId,
        string coverage,
        IReadOnlyList<WorldTargetObservation> targets,
        string? nextCursor = null)
    {
        WorldMachinesResponse.RequireNearbySummary(summary);
        WorldMachinesResponse.RequireContinuation(summary, nextCursor);
        RecipeContractValidation.RequireResourceLocation(structureId, nameof(structureId));
        if (coverage != "structure_covered_chunks")
            throw new ArgumentException("Unknown structure coverage.", nameof(coverage));
        Targets = WorldMachinesResponse.CopyUnique(
            targets,
            ProtocolConstants.WorldQueryResultMaximum,
            static target => target.Key,
            nameof(targets));
        Summary = summary;
        StructureId = structureId;
        Coverage = coverage;
        NextCursor = nextCursor;
    }

    public WorldQuerySummary Summary { get; }
    public string StructureId { get; }
    public string Coverage { get; }
    public IReadOnlyList<WorldTargetObservation> Targets { get; }
    [JsonIgnore(Condition = JsonIgnoreCondition.WhenWritingNull)]
    public string? NextCursor { get; }
}

public sealed record WorldRegistryResolveResponse
{
    [JsonConstructor]
    public WorldRegistryResolveResponse(
        string revision,
        IReadOnlyList<WorldRegistryMatch> matches,
        bool truncated, bool suggestionsOnly = false)
    {
        SuggestionsOnly = suggestionsOnly;
        Revision = RequireRevision(revision);
        Matches = WorldMachinesResponse.CopyUnique(
            matches,
            ProtocolConstants.WorldQueryNameCandidateMaximum,
            static match => match.Key,
            nameof(matches));
        Truncated = truncated;
    }

    public string Revision { get; }
    public IReadOnlyList<WorldRegistryMatch> Matches { get; }
    public bool Truncated { get; }
    public bool SuggestionsOnly { get; }

    internal static string RequireRevision(string value)
    {
        WorldQueryValidation.RequireOpaqueId(value, nameof(value));
        return value;
    }
}

internal static class WorldQueryValidation
{
    private static readonly Regex OpaqueIdPattern = new(
        "^[A-Za-z0-9_.:-]+$",
        RegexOptions.CultureInvariant | RegexOptions.NonBacktracking);

    internal static void RequireOpaqueId(string value, string parameterName)
    {
        QuestContractValidation.RequireText(value, 1, 64, parameterName);
        if (!OpaqueIdPattern.IsMatch(value))
            throw new ArgumentException("Value must be a safe opaque ID.", parameterName);
    }
}

public sealed record WorldRegistryDescribeResponse
{
    [JsonConstructor]
    public WorldRegistryDescribeResponse(
        string revision,
        IReadOnlyList<WorldRegistryMatch> descriptions,
        IReadOnlyList<WorldRegistryReference> missing)
    {
        Revision = WorldRegistryResolveResponse.RequireRevision(revision);
        Descriptions = WorldMachinesResponse.CopyUnique(
            descriptions,
            ProtocolConstants.WorldQueryResultMaximum,
            static description => description.Key,
            nameof(descriptions));
        Missing = WorldMachinesResponse.CopyUnique(
            missing,
            ProtocolConstants.WorldQueryResultMaximum,
            static reference => reference.Key,
            nameof(missing));
        if (Descriptions.Count + Missing.Count > 128 ||
            Descriptions.Select(static value => value.Key).Intersect(
                Missing.Select(static value => value.Key), StringComparer.Ordinal).Any())
            throw new ArgumentException("Descriptions and missing must not overlap.");
    }

    public string Revision { get; }
    public IReadOnlyList<WorldRegistryMatch> Descriptions { get; }
    public IReadOnlyList<WorldRegistryReference> Missing { get; }
}
