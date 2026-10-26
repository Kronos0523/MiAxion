namespace MiAxion.Protocol.Contracts;

/// <summary>Metadata-only directory request. Provider choice is a separate, future contract.</summary>
public sealed record RecipeProvidersListRequest;

public sealed record RecipeProvidersListResponse(
    IReadOnlyList<RecipeProviderDescriptor> Providers);

public sealed record RecipeProviderDescriptor(
    string AddonId,
    string AddonVersion,
    string TargetModId,
    RecipeProviderReadiness Query,
    RecipeProviderReadiness Uses,
    long? SourceGeneration = null);

public sealed record RecipeProviderReadiness(string Status, string Reason);

/// <summary>Separate source-bound wire shape; does not extend the 1.7 recipe frames.</summary>
public sealed record RecipeSourceRequest
{
    [System.Text.Json.Serialization.JsonConstructor]
    public RecipeSourceRequest(string addonId, long sourceGeneration, string itemId,
        System.Text.Json.JsonElement? components = null, string? matchMode = null,
        RecipeSelectionRequest? selection = null, int? sourcePageSize = null, string? sourceCursor = null, string? resourceKind = null, string? ingredientTypeUid = null)
    {
        if (addonId is not { Length: >= 2 and <= 64 } ||
            !System.Text.RegularExpressions.Regex.IsMatch(addonId, "^[a-z][a-z0-9_]{1,63}$",
                System.Text.RegularExpressions.RegexOptions.CultureInvariant))
            throw new ArgumentException("Invalid recipe addon ID.", nameof(addonId));
        ArgumentOutOfRangeException.ThrowIfNegative(sourceGeneration);
        var query = new RecipeQueryRequest(itemId, components, matchMode, selection, sourcePageSize, sourceCursor, resourceKind, ingredientTypeUid);
        AddonId = addonId;
        SourceGeneration = sourceGeneration;
        ResourceKind = query.ResourceKind; IngredientTypeUid = query.IngredientTypeUid;
        ItemId = query.ItemId;
        Components = query.Components;
        MatchMode = query.MatchMode;
        Selection = query.Selection;
        SourcePageSize = query.SourcePageSize;
        SourceCursor = query.SourceCursor;
    }

    public string AddonId { get; }
    public long SourceGeneration { get; }
    [System.Text.Json.Serialization.JsonIgnore(Condition = System.Text.Json.Serialization.JsonIgnoreCondition.WhenWritingNull)]
    public string? ResourceKind { get; }
    [System.Text.Json.Serialization.JsonIgnore(Condition = System.Text.Json.Serialization.JsonIgnoreCondition.WhenWritingNull)]
    public string? IngredientTypeUid { get; }
    public string ItemId { get; }
    [System.Text.Json.Serialization.JsonIgnore(Condition = System.Text.Json.Serialization.JsonIgnoreCondition.WhenWritingNull)]
    public System.Text.Json.JsonElement? Components { get; }
    public string MatchMode { get; }
    [System.Text.Json.Serialization.JsonIgnore(Condition = System.Text.Json.Serialization.JsonIgnoreCondition.WhenWritingNull)]
    public RecipeSelectionRequest? Selection { get; }
    [System.Text.Json.Serialization.JsonIgnore(Condition = System.Text.Json.Serialization.JsonIgnoreCondition.WhenWritingNull)]
    public int? SourcePageSize { get; }
    [System.Text.Json.Serialization.JsonIgnore(Condition = System.Text.Json.Serialization.JsonIgnoreCondition.WhenWritingNull)]
    public string? SourceCursor { get; }
}
