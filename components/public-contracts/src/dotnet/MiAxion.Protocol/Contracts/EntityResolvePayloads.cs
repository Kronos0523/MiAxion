using System.Text.Json.Serialization;
using System.Text.RegularExpressions;

namespace MiAxion.Protocol.Contracts;

public sealed record EntityResolveRequest
{
    private static readonly Regex NamespacePattern = new(
        "^[a-z0-9_.-]+$", RegexOptions.CultureInvariant | RegexOptions.NonBacktracking);

    public EntityResolveRequest(string query, IReadOnlyList<string> namespaces)
        : this(query, namespaces, null)
    {
    }

    [JsonConstructor]
    public EntityResolveRequest(
        string query,
        IReadOnlyList<string> namespaces,
        string? kind = null,
        string? continuation = null, string? scope = null)
    {
        ArgumentNullException.ThrowIfNull(query);
        ArgumentNullException.ThrowIfNull(namespaces);
        if (query.EnumerateRunes().Count() is < 1 or > 512)
            throw new ArgumentException("Query must contain 1-512 Unicode scalars.", nameof(query));
        if (kind is not (null or "item" or "block" or "fluid" or "any") && !(scope == "browser" && kind == "custom"))
            throw new ArgumentException("Kind must be item, block, fluid, any, or omitted.", nameof(kind));
        if (namespaces.Count > 128 || namespaces.Any(static value => value is null))
            throw new ArgumentException("Namespaces must contain at most 128 values.", nameof(namespaces));
        var seen = new HashSet<string>(StringComparer.Ordinal);
        foreach (var value in namespaces)
        {
            if (value.Length is < 1 or > 64 || !NamespacePattern.IsMatch(value) || !seen.Add(value))
                throw new ArgumentException("Namespaces must be unique lowercase identifiers.", nameof(namespaces));
        }

        if (scope is not (null or "registry" or "browser")) throw new ArgumentException("Invalid directory scope.");
        Scope = scope;
        Query = query;
        Namespaces = namespaces.ToArray();
        Kind = kind;
        EntityContinuationValidation.Require(continuation);
        Continuation = continuation;
    }

    [JsonIgnore(Condition = JsonIgnoreCondition.WhenWritingNull)]
    public string? Scope { get; }
    public string Query { get; }
    public IReadOnlyList<string> Namespaces { get; }

    [JsonIgnore(Condition = JsonIgnoreCondition.WhenWritingNull)]
    public string? Kind { get; }

    [JsonIgnore(Condition = JsonIgnoreCondition.WhenWritingNull)]
    public string? Continuation { get; }

    [JsonIgnore]
    public string EffectiveKind => Kind ?? "any";
}

public sealed record EntityResolveMatch
{
    [JsonConstructor]
    public EntityResolveMatch(string kind, string resourceId, string translationKey, string displayName, string? ingredientTypeUid = null, System.Text.Json.JsonElement? components = null)
    {
        ArgumentNullException.ThrowIfNull(kind);
        ArgumentNullException.ThrowIfNull(resourceId);
        ArgumentNullException.ThrowIfNull(translationKey);
        ArgumentNullException.ThrowIfNull(displayName);
        if (kind is not ("item" or "block" or "fluid" or "custom"))
            throw new ArgumentException("Kind must be item, block or fluid.", nameof(kind));
        RecipeContractValidation.RequireResourceLocation(resourceId, nameof(resourceId));
        EntityTextValidation.Require(translationKey, nameof(translationKey));
        EntityTextValidation.Require(displayName, nameof(displayName));
        if (translationKey.EnumerateRunes().Count() is < 1 or > 256 || displayName.EnumerateRunes().Count() is < 1 or > 256)
            throw new ArgumentException("Entity names are outside their bounds.");
        if (kind == "custom" && string.IsNullOrWhiteSpace(ingredientTypeUid)) throw new ArgumentException("Missing native ingredient type.");
        IngredientTypeUid = ingredientTypeUid;
        Components = components?.Clone();
        Kind = kind;
        ResourceId = resourceId;
        TranslationKey = translationKey;
        DisplayName = displayName;
    }

    public string Kind { get; }
    [JsonIgnore(Condition = JsonIgnoreCondition.WhenWritingNull)] public string? IngredientTypeUid { get; }
    [JsonIgnore(Condition = JsonIgnoreCondition.WhenWritingNull)] public System.Text.Json.JsonElement? Components { get; }
    public string ResourceId { get; }
    public string TranslationKey { get; }
    public string DisplayName { get; }
}

public sealed record EntityResolveResponse
{
    [JsonConstructor]
    public EntityResolveResponse(string revision, IReadOnlyList<EntityResolveMatch> matches, bool truncated, string? nextContinuation = null)
    {
        ArgumentNullException.ThrowIfNull(revision);
        ArgumentNullException.ThrowIfNull(matches);
        if (revision is { Length: 0 or > 64 } || matches.Count > 8 || matches.Any(static value => value is null))
            throw new ArgumentException("Entity resolve response is outside its bounds.");
        Revision = revision;
        Matches = matches.ToArray();
        Truncated = truncated;
        EntityContinuationValidation.Require(nextContinuation);
        if (nextContinuation is not null && (matches.Count != 8 || !truncated))
            throw new ArgumentException("An entity continuation requires a full incomplete page.", nameof(nextContinuation));
        NextContinuation = nextContinuation;
    }

    public string Revision { get; }
    public IReadOnlyList<EntityResolveMatch> Matches { get; }
    public bool Truncated { get; }

    [JsonIgnore(Condition = JsonIgnoreCondition.WhenWritingNull)]
    public string? NextContinuation { get; }
}

internal static class EntityContinuationValidation
{
    internal static void Require(string? value)
    {
        if (value is not null && (value.Length is < 1 or > 256 ||
            value.Any(static c => !char.IsAsciiLetterOrDigit(c) && c is not ('_' or '-'))))
            throw new ArgumentException("Entity continuation is invalid.", nameof(value));
    }
}
