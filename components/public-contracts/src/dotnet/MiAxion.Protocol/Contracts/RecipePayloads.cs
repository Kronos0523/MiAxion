using System.Text.Json;

namespace MiAxion.Protocol.Contracts;

public sealed record RecipeSelectionRequest
{
    public RecipeSelectionRequest(string mode, int maxResults)
        : this(mode, maxResults, null)
    {
    }

    [System.Text.Json.Serialization.JsonConstructor]
    public RecipeSelectionRequest(string mode, int maxResults, string? preferredRecipeId = null)
    {
        if (mode is not ("recommended-direct" or "recommended-process-families" or "planning-direct"))
            throw new ArgumentException("Unknown recipe selection mode.", nameof(mode));
        if (maxResults is < 1 or > 5)
            throw new ArgumentOutOfRangeException(nameof(maxResults));
        if (preferredRecipeId is not null)
        {
            if (mode is not ("recommended-direct" or "planning-direct"))
                throw new ArgumentException(
                    "A preferred recipe is only valid for direct selection.", nameof(preferredRecipeId));
            RecipeContractValidation.RequireResourceLocation(preferredRecipeId, nameof(preferredRecipeId));
        }
        Mode = mode;
        MaxResults = maxResults;
        PreferredRecipeId = preferredRecipeId;
    }

    public string Mode { get; }
    public int MaxResults { get; }

    [System.Text.Json.Serialization.JsonIgnore(
        Condition = System.Text.Json.Serialization.JsonIgnoreCondition.WhenWritingNull)]
    public string? PreferredRecipeId { get; }

    public static RecipeSelectionRequest RecommendedDirect() =>
        new("recommended-direct", 3);

    public static RecipeSelectionRequest RecommendedDirect(string preferredRecipeId) =>
        new("recommended-direct", 3, preferredRecipeId);

    public static RecipeSelectionRequest RecommendedProcessFamilies() =>
        new("recommended-process-families", 5);
}

public sealed record RecipeQueryRequest
{
    public RecipeQueryRequest(string itemId)
        : this(itemId, null, "item", null)
    {
    }

    [System.Text.Json.Serialization.JsonConstructor]
    public RecipeQueryRequest(
        string itemId,
        JsonElement? components = null,
        string? matchMode = null,
        RecipeSelectionRequest? selection = null,
        int? sourcePageSize = null,
        string? sourceCursor = null, string? resourceKind = null, string? ingredientTypeUid = null)
    {
        RecipeContractValidation.RequireResourceLocation(itemId, nameof(itemId));
        if (resourceKind is not null && resourceKind is not ("item" or "fluid" or "chemical" or "energy" or "custom")) throw new ArgumentException("Invalid resource kind.", nameof(resourceKind));
        if (ingredientTypeUid is not null && (string.IsNullOrWhiteSpace(ingredientTypeUid) || ingredientTypeUid.Length > 512)) throw new ArgumentException("Invalid ingredient type.", nameof(ingredientTypeUid));
        ResourceKind = resourceKind; IngredientTypeUid = ingredientTypeUid;
        ItemId = itemId;
        Components = RecipeContractValidation.CopyComponents(components);
        MatchMode = RecipeContractValidation.NormalizeMatchMode(matchMode);
        Selection = selection;
        if (sourcePageSize is { } pageSize)
            RecipeContractValidation.RequireSourcePageSize(pageSize);
        RecipeContractValidation.RequireSourceCursor(sourceCursor);
        SourcePageSize = sourcePageSize;
        SourceCursor = sourceCursor;
    }

    [System.Text.Json.Serialization.JsonIgnore(Condition = System.Text.Json.Serialization.JsonIgnoreCondition.WhenWritingNull)]
    public string? ResourceKind { get; }
    [System.Text.Json.Serialization.JsonIgnore(Condition = System.Text.Json.Serialization.JsonIgnoreCondition.WhenWritingNull)]
    public string? IngredientTypeUid { get; }
    public string ItemId { get; }
    [System.Text.Json.Serialization.JsonIgnore(
        Condition = System.Text.Json.Serialization.JsonIgnoreCondition.WhenWritingNull)]
    public JsonElement? Components { get; }
    public string MatchMode { get; }
    [System.Text.Json.Serialization.JsonIgnore(
        Condition = System.Text.Json.Serialization.JsonIgnoreCondition.WhenWritingNull)]
    public RecipeSelectionRequest? Selection { get; }

    [System.Text.Json.Serialization.JsonIgnore(
        Condition = System.Text.Json.Serialization.JsonIgnoreCondition.WhenWritingNull)]
    public int? SourcePageSize { get; }

    [System.Text.Json.Serialization.JsonIgnore(
        Condition = System.Text.Json.Serialization.JsonIgnoreCondition.WhenWritingNull)]
    public string? SourceCursor { get; }
}

public sealed record RecipeUsesRequest
{
    public RecipeUsesRequest(string itemId)
        : this(itemId, null, "item", null)
    {
    }

    [System.Text.Json.Serialization.JsonConstructor]
    public RecipeUsesRequest(
        string itemId,
        JsonElement? components = null,
        string? matchMode = null,
        RecipeSelectionRequest? selection = null,
        int? sourcePageSize = null,
        string? sourceCursor = null, string? resourceKind = null, string? ingredientTypeUid = null)
    {
        RecipeContractValidation.RequireResourceLocation(itemId, nameof(itemId));
        if (resourceKind is not null && resourceKind is not ("item" or "fluid" or "chemical" or "energy" or "custom")) throw new ArgumentException("Invalid resource kind.", nameof(resourceKind));
        if (ingredientTypeUid is not null && (string.IsNullOrWhiteSpace(ingredientTypeUid) || ingredientTypeUid.Length > 512)) throw new ArgumentException("Invalid ingredient type.", nameof(ingredientTypeUid));
        ResourceKind = resourceKind; IngredientTypeUid = ingredientTypeUid;
        ItemId = itemId;
        Components = RecipeContractValidation.CopyComponents(components);
        MatchMode = RecipeContractValidation.NormalizeMatchMode(matchMode);
        Selection = selection;
        if (sourcePageSize is { } pageSize)
            RecipeContractValidation.RequireSourcePageSize(pageSize);
        RecipeContractValidation.RequireSourceCursor(sourceCursor);
        SourcePageSize = sourcePageSize;
        SourceCursor = sourceCursor;
    }

    [System.Text.Json.Serialization.JsonIgnore(Condition = System.Text.Json.Serialization.JsonIgnoreCondition.WhenWritingNull)]
    public string? ResourceKind { get; }
    [System.Text.Json.Serialization.JsonIgnore(Condition = System.Text.Json.Serialization.JsonIgnoreCondition.WhenWritingNull)]
    public string? IngredientTypeUid { get; }
    public string ItemId { get; }
    [System.Text.Json.Serialization.JsonIgnore(
        Condition = System.Text.Json.Serialization.JsonIgnoreCondition.WhenWritingNull)]
    public JsonElement? Components { get; }
    public string MatchMode { get; }
    [System.Text.Json.Serialization.JsonIgnore(
        Condition = System.Text.Json.Serialization.JsonIgnoreCondition.WhenWritingNull)]
    public RecipeSelectionRequest? Selection { get; }

    [System.Text.Json.Serialization.JsonIgnore(
        Condition = System.Text.Json.Serialization.JsonIgnoreCondition.WhenWritingNull)]
    public int? SourcePageSize { get; }

    [System.Text.Json.Serialization.JsonIgnore(
        Condition = System.Text.Json.Serialization.JsonIgnoreCondition.WhenWritingNull)]
    public string? SourceCursor { get; }
}
