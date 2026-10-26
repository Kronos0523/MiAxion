using System.Text.Json;
using System.Text.Json.Serialization;
using System.Text.RegularExpressions;

namespace MiAxion.Protocol.Contracts;

public sealed record RecipeAlternativeDto
{
    public RecipeAlternativeDto(string? itemId = null, int count = 0, JsonElement? components = null, RecipeResourceDto? resource = null)
    {
        if (resource is null) {
            RecipeContractValidation.RequireResourceLocation(itemId!, nameof(itemId));
            ArgumentOutOfRangeException.ThrowIfNegativeOrZero(count);
        } else if (itemId is not null || count != 0) throw new ArgumentException("Typed resources cannot masquerade as items.", nameof(resource));
        Resource = resource;

        ItemId = itemId;
        Count = count;
        Components = RecipeContractValidation.CopyComponents(components);
    }

    [JsonIgnore(Condition = JsonIgnoreCondition.WhenWritingNull)]
    public string? ItemId { get; }

    [JsonIgnore(Condition = JsonIgnoreCondition.WhenWritingDefault)]
    public int Count { get; }
    [JsonIgnore(Condition = JsonIgnoreCondition.WhenWritingNull)]
    public RecipeResourceDto? Resource { get; }

    [JsonIgnore(Condition = JsonIgnoreCondition.WhenWritingNull)]
    public JsonElement? Components { get; }
}

public sealed record RecipeIdentityDto
{
    public RecipeIdentityDto(string kind, string? algorithm = null, string? contentSha256 = null)
    {
        if (kind is not ("registered" or "derived"))
            throw new ArgumentException("Identity kind must be registered or derived.", nameof(kind));
        if (kind == "registered" && (algorithm is not null || contentSha256 is not null))
            throw new ArgumentException("Registered identity cannot contain derived fields.", nameof(kind));
        if (kind == "derived" && (algorithm != "sha256-canonical-v1" ||
            contentSha256 is null || !Regex.IsMatch(contentSha256, "^[0-9a-f]{64}$")))
            throw new ArgumentException("Derived identity must contain a lowercase SHA-256.", nameof(contentSha256));
        Kind = kind;
        Algorithm = algorithm;
        ContentSha256 = contentSha256;
    }

    public string Kind { get; }

    [JsonIgnore(Condition = JsonIgnoreCondition.WhenWritingNull)]
    public string? Algorithm { get; }

    [JsonIgnore(Condition = JsonIgnoreCondition.WhenWritingNull)]
    public string? ContentSha256 { get; }

    public static RecipeIdentityDto Registered() => new("registered");
    public static RecipeIdentityDto Derived(string hash) => new("derived", "sha256-canonical-v1", hash);
}

public sealed record RecipeSlotDto
{
    public RecipeSlotDto(
        IReadOnlyList<RecipeAlternativeDto> alternatives)
    {
        Alternatives = RecipeContractValidation.CopyWithinBounds(
            alternatives,
            minCount: 1,
            maxCount: ProtocolConstants.RecipeSlotAlternativesMaximum,
            nameof(alternatives));
    }

    public IReadOnlyList<RecipeAlternativeDto> Alternatives { get; }
}

public sealed record RecipeProcessDto
{
    public RecipeProcessDto(
        string machineFamily,
        int? durationTicks = null,
        string? energyUnit = null,
        long? energyAmount = null)
    {
        RecipeContractValidation.RequireResourceLocation(machineFamily, nameof(machineFamily));
        if (durationTicks is <= 0)
            throw new ArgumentOutOfRangeException(nameof(durationTicks));
        if ((energyUnit is null) != (energyAmount is null))
            throw new ArgumentException("Energy unit and amount must appear together.", nameof(energyUnit));
        if (energyUnit is not null && (string.IsNullOrWhiteSpace(energyUnit) || energyUnit.Length > 64 || energyUnit.Any(char.IsControl)))
            throw new ArgumentException("Unsupported energy unit.", nameof(energyUnit));
        if (energyAmount is <= 0 or > ProtocolConstants.MaximumSafeJsonInteger)
            throw new ArgumentOutOfRangeException(nameof(energyAmount));

        MachineFamily = machineFamily;
        DurationTicks = durationTicks;
        EnergyUnit = energyUnit;
        EnergyAmount = energyAmount;
    }

    public string MachineFamily { get; }

    [JsonIgnore(Condition = JsonIgnoreCondition.WhenWritingNull)]
    public int? DurationTicks { get; }

    [JsonIgnore(Condition = JsonIgnoreCondition.WhenWritingNull)]
    public string? EnergyUnit { get; }

    [JsonIgnore(Condition = JsonIgnoreCondition.WhenWritingNull)]
    public long? EnergyAmount { get; }
}

public sealed record RecipeClassificationDto
{
    public RecipeClassificationDto(
        string category,
        string planningEligibility,
        string? visibleCategoryName = null)
    {
        if (category is not ("crafting" or "machine_processing" or "drops" or "loot" or "conversion" or "other_unclassified"))
            throw new ArgumentException("Unsupported recipe category.", nameof(category));
        if (planningEligibility is not ("deterministic" or "acquisition_only" or "insufficient_data"))
            throw new ArgumentException("Unsupported recipe planning eligibility.", nameof(planningEligibility));
        if ((category is "drops" or "loot" or "other_unclassified") && planningEligibility == "deterministic")
            throw new ArgumentException("Acquisition and unclassified categories cannot be deterministic.", nameof(planningEligibility));
        if (visibleCategoryName is { Length: 0 or > 256 } ||
            visibleCategoryName?.Any(static character => char.IsControl(character)) == true)
            throw new ArgumentException("Visible category name is invalid.", nameof(visibleCategoryName));
        Category = category;
        PlanningEligibility = planningEligibility;
        VisibleCategoryName = visibleCategoryName;
    }

    public string Category { get; }
    public string PlanningEligibility { get; }
    [JsonIgnore(Condition = JsonIgnoreCondition.WhenWritingNull)]
    public string? VisibleCategoryName { get; }
}

public sealed record RecipeDto
{
    public RecipeDto(
        string recipeId,
        string type,
        string sourceModId,
        IReadOnlyList<RecipeSlotDto> inputs,
        IReadOnlyList<RecipeSlotDto> catalysts,
        IReadOnlyList<RecipeSlotDto> outputs)
        : this(recipeId, type, sourceModId, RecipeIdentityDto.Registered(), inputs, catalysts, outputs, null)
    {
    }

    [JsonConstructor]
    public RecipeDto(
        string recipeId,
        string type,
        string sourceModId,
        RecipeIdentityDto identity,
        IReadOnlyList<RecipeSlotDto> inputs,
        IReadOnlyList<RecipeSlotDto> catalysts,
        IReadOnlyList<RecipeSlotDto> outputs,
        RecipeProcessDto? process = null,
        RecipeClassificationDto? classification = null)
    {
        RecipeContractValidation.RequireResourceLocation(
            recipeId,
            nameof(recipeId));
        RecipeContractValidation.RequireResourceLocation(
            type,
            nameof(type));
        RecipeContractValidation.RequireNamespace(
            sourceModId,
            nameof(sourceModId));
        Identity = identity ?? throw new ArgumentNullException(nameof(identity));
        Process = process;
        Classification = classification;

        RecipeId = recipeId;
        Type = type;
        SourceModId = sourceModId;
        Inputs = RecipeContractValidation.CopyWithinBounds(
            inputs,
            minCount: 0,
            maxCount: ProtocolConstants.RecipeInputSlotsMaximum,
            nameof(inputs));
        Catalysts = RecipeContractValidation.CopyWithinBounds(
            catalysts,
            minCount: 0,
            maxCount: ProtocolConstants.RecipeCatalystSlotsMaximum,
            nameof(catalysts));
        Outputs = RecipeContractValidation.CopyWithinBounds(
            outputs,
            minCount: 0,
            maxCount: ProtocolConstants.RecipeOutputSlotsMaximum,
            nameof(outputs));
    }

    public string RecipeId { get; }

    public string Type { get; }

    public string SourceModId { get; }

    public RecipeIdentityDto Identity { get; }

    public IReadOnlyList<RecipeSlotDto> Inputs { get; }

    public IReadOnlyList<RecipeSlotDto> Catalysts { get; }

    public IReadOnlyList<RecipeSlotDto> Outputs { get; }

    [JsonIgnore(Condition = JsonIgnoreCondition.WhenWritingNull)]
    public RecipeProcessDto? Process { get; }

    [JsonIgnore(Condition = JsonIgnoreCondition.WhenWritingNull)]
    public RecipeClassificationDto? Classification { get; }
}

public sealed record RecipeResponse
{
    public RecipeResponse(
        IReadOnlyList<RecipeDto> recipes,
        bool truncated)
        : this(recipes, truncated, null, null, null, null, null, null, null, null)
    {
    }

    [JsonConstructor]
    public RecipeResponse(
        IReadOnlyList<RecipeDto> recipes,
        bool truncated,
        bool? sourceComplete = null,
        bool? selectionComplete = null,
        int? candidatesEvaluated = null,
        string? selectionMode = null,
        string? inventoryRevision = null,
        IReadOnlyList<string>? selectionOrder = null,
        string? nextSourceCursor = null,
        int? sourcePageSize = null, IReadOnlyList<string>? resourceWarnings = null)
    {
        if (resourceWarnings is not null && (resourceWarnings.Count > 32 || resourceWarnings.Any(v => v is null || v.Length is < 1 or > 64 || !v.All(static c => c is >= 'A' and <= 'Z' or >= '0' and <= '9' or '_')))) throw new ArgumentException("Invalid bounded recipe diagnostic codes.", nameof(resourceWarnings));
        ResourceWarnings = resourceWarnings?.ToArray();
        Recipes = RecipeContractValidation.CopyWithinBounds(
            recipes,
            minCount: 0,
            maxCount: 128,
            nameof(recipes));
        Truncated = truncated;
        if (candidatesEvaluated is < 0)
            throw new ArgumentOutOfRangeException(nameof(candidatesEvaluated));
        if (selectionComplete is not null && sourceComplete is null)
            throw new ArgumentException(
                "selectionComplete requires sourceComplete.", nameof(selectionComplete));
        if (selectionMode is not null &&
            selectionMode is not ("recommended-direct" or "recommended-process-families" or "planning-direct"))
            throw new ArgumentException(
                "Unknown recipe selection mode.", nameof(selectionMode));
        if (inventoryRevision is { Length: 0 or > 64 })
            throw new ArgumentException(
                "Inventory revision must contain between 1 and 64 characters.", nameof(inventoryRevision));
        SourceComplete = sourceComplete;
        SelectionComplete = selectionComplete;
        CandidatesEvaluated = candidatesEvaluated;
        SelectionMode = selectionMode;
        InventoryRevision = inventoryRevision;
        SelectionOrder = selectionOrder is null
            ? null
            : RecipeContractValidation.CopyWithinBounds(
                selectionOrder, 0, 5, nameof(selectionOrder));
        if (SelectionOrder is not null)
        {
            foreach (var recipeId in SelectionOrder)
                RecipeContractValidation.RequireResourceLocation(recipeId, nameof(selectionOrder));
            if (SelectionOrder.Distinct(StringComparer.Ordinal).Count() != SelectionOrder.Count)
                throw new ArgumentException(
                    "Selection order cannot contain duplicate recipe IDs.", nameof(selectionOrder));
        }
        RecipeContractValidation.RequireSourceCursor(nextSourceCursor);
        if (nextSourceCursor is not null && !truncated)
            throw new ArgumentException(
                "A source continuation requires a truncated response.", nameof(nextSourceCursor));
        if (nextSourceCursor is not null && sourcePageSize is null)
            throw new ArgumentException(
                "A source continuation requires the originating source page size.",
                nameof(sourcePageSize));
        if (sourcePageSize is { } pageSize)
            RecipeContractValidation.RequireSourcePageSize(pageSize);
        NextSourceCursor = nextSourceCursor;
        SourcePageSize = sourcePageSize;
    }

    [JsonIgnore(Condition = JsonIgnoreCondition.WhenWritingNull)]
    public IReadOnlyList<string>? ResourceWarnings { get; }
    public IReadOnlyList<RecipeDto> Recipes { get; }

    public bool Truncated { get; }

    [JsonIgnore(Condition = JsonIgnoreCondition.WhenWritingNull)]
    public bool? SourceComplete { get; }

    [JsonIgnore(Condition = JsonIgnoreCondition.WhenWritingNull)]
    public bool? SelectionComplete { get; }

    [JsonIgnore(Condition = JsonIgnoreCondition.WhenWritingNull)]
    public int? CandidatesEvaluated { get; }

    [JsonIgnore(Condition = JsonIgnoreCondition.WhenWritingNull)]
    public string? SelectionMode { get; }

    [JsonIgnore(Condition = JsonIgnoreCondition.WhenWritingNull)]
    public string? InventoryRevision { get; }

    [JsonIgnore(Condition = JsonIgnoreCondition.WhenWritingNull)]
    public IReadOnlyList<string>? SelectionOrder { get; }

    [JsonIgnore(Condition = JsonIgnoreCondition.WhenWritingNull)]
    public string? NextSourceCursor { get; }

    [JsonIgnore(Condition = JsonIgnoreCondition.WhenWritingNull)]
    public int? SourcePageSize { get; }
}

internal static class RecipeContractValidation
{
    private const int ResourceLocationMaxLength = 128;
    private const int NamespaceMaxLength = 64;

    private static readonly Regex ResourceLocationPattern = new(
        "^[a-z0-9_.-]+:[a-z0-9_./-]+$",
        RegexOptions.CultureInvariant | RegexOptions.NonBacktracking);
    private static readonly Regex NamespacePattern = new(
        "^[a-z0-9_.-]+$",
        RegexOptions.CultureInvariant | RegexOptions.NonBacktracking);

    internal static JsonElement? CopyComponents(JsonElement? components)
    {
        if (components is null) return null;
        if (components.Value.ValueKind != JsonValueKind.Object)
            throw new ArgumentException("Components must be an object.", nameof(components));
        foreach (var property in components.Value.EnumerateObject())
        {
            if (property.Name.Length > ResourceLocationMaxLength ||
                !ResourceLocationPattern.IsMatch(property.Name))
                throw new ArgumentException("Component keys must be resource locations.", nameof(components));
        }
        using var document = JsonDocument.Parse(components.Value.GetRawText());
        return document.RootElement.Clone();
    }

    internal static string NormalizeMatchMode(string? value)
    {
        var mode = value ?? "item";
        if (mode is not ("item" or "subset-components" or "exact-components"))
            throw new ArgumentException("Unknown recipe component match mode.", nameof(value));
        return mode;
    }

    internal static void RequireSourcePageSize(int value)
    {
        if (value is < 1 or > ProtocolConstants.RecipeSourcePageMaximum)
            throw new ArgumentOutOfRangeException(nameof(value));
    }

    internal static void RequireSourceCursor(string? value)
    {
        if (value is not null && (value.Length is < 1 or > ProtocolConstants.RecipeSourceCursorMaximum ||
            !value.All(static c => c is >= 'a' and <= 'z' or >= '0' and <= '9')))
            throw new ArgumentException("Source cursor is invalid.", nameof(value));
    }

    internal static IReadOnlyList<T> CopyWithinBounds<T>(
        IReadOnlyList<T> values,
        int minCount,
        int maxCount,
        string parameterName)
    {
        ArgumentNullException.ThrowIfNull(values, parameterName);

        if (values.Count < minCount || values.Count > maxCount)
        {
            throw new ArgumentOutOfRangeException(
                parameterName,
                values.Count,
                $"Collection count must be between {minCount} and {maxCount}.");
        }

        var copy = values.ToArray();

        if (copy.Any(item => item is null))
        {
            throw new ArgumentException(
                "Collection elements cannot be null.",
                parameterName);
        }

        return Array.AsReadOnly(copy);
    }

    internal static void RequireResourceLocation(
        string value,
        string parameterName)
    {
        ArgumentNullException.ThrowIfNull(value, parameterName);

        if (value.Length > ResourceLocationMaxLength ||
            !ResourceLocationPattern.IsMatch(value))
        {
            throw new ArgumentException(
                "Value must be a valid resource location.",
                parameterName);
        }
    }

    internal static void RequireNamespace(
        string value,
        string parameterName)
    {
        ArgumentNullException.ThrowIfNull(value, parameterName);

        if (value.Length > NamespaceMaxLength ||
            !NamespacePattern.IsMatch(value))
        {
            throw new ArgumentException(
                "Value must be a valid resource namespace.",
                parameterName);
        }
    }
}
