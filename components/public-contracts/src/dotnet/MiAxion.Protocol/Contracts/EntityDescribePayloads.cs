using System.Text.Json.Serialization;

namespace MiAxion.Protocol.Contracts;

public sealed record EntityReference
{
    [JsonConstructor]
    public EntityReference(string kind, string resourceId)
    {
        if (kind is not ("item" or "block"))
            throw new ArgumentException("Kind must be item or block.", nameof(kind));
        RecipeContractValidation.RequireResourceLocation(resourceId, nameof(resourceId));
        Kind = kind;
        ResourceId = resourceId;
    }

    public string Kind { get; }
    public string ResourceId { get; }

    internal string Key => $"{Kind}\0{ResourceId}";
}

public sealed record EntityDescribeRequest
{
    [JsonConstructor]
    public EntityDescribeRequest(IReadOnlyList<EntityReference> entities)
    {
        ArgumentNullException.ThrowIfNull(entities);
        if (entities.Count is < 1 or > 128 || entities.Any(static entity => entity is null))
            throw new ArgumentException("Entities must contain 1-128 non-null references.", nameof(entities));
        var seen = new HashSet<string>(StringComparer.Ordinal);
        foreach (var entity in entities)
        {
            if (!seen.Add(entity.Key))
                throw new ArgumentException("Entities must contain unique kind/resourceId pairs.", nameof(entities));
        }
        Entities = entities.ToArray();
    }

    public IReadOnlyList<EntityReference> Entities { get; }
}

public sealed record EntityDescription
{
    [JsonConstructor]
    public EntityDescription(
        string kind,
        string resourceId,
        string translationKey,
        string displayName)
    {
        var reference = new EntityReference(kind, resourceId);
        ArgumentNullException.ThrowIfNull(translationKey);
        ArgumentNullException.ThrowIfNull(displayName);
        EntityTextValidation.Require(translationKey, nameof(translationKey));
        EntityTextValidation.Require(displayName, nameof(displayName));
        if (translationKey.EnumerateRunes().Count() is < 1 or > 256 ||
            displayName.EnumerateRunes().Count() is < 1 or > 256)
            throw new ArgumentException("Entity names are outside their bounds.");
        Kind = reference.Kind;
        ResourceId = reference.ResourceId;
        TranslationKey = translationKey;
        DisplayName = displayName;
    }

    public string Kind { get; }
    public string ResourceId { get; }
    public string TranslationKey { get; }
    public string DisplayName { get; }

    internal string Key => $"{Kind}\0{ResourceId}";
}

public sealed record EntityDescribeResponse
{
    [JsonConstructor]
    public EntityDescribeResponse(
        string revision,
        IReadOnlyList<EntityDescription> descriptions,
        IReadOnlyList<EntityReference> missing)
    {
        ArgumentNullException.ThrowIfNull(revision);
        ArgumentNullException.ThrowIfNull(descriptions);
        ArgumentNullException.ThrowIfNull(missing);
        if (revision.Length is < 1 or > 64 || descriptions.Count > 128 || missing.Count > 128 ||
            descriptions.Count + missing.Count > 128 ||
            descriptions.Any(static value => value is null) || missing.Any(static value => value is null))
            throw new ArgumentException("Entity describe response is outside its bounds.");
        var seen = new HashSet<string>(StringComparer.Ordinal);
        foreach (var description in descriptions)
        {
            if (!seen.Add(description.Key))
                throw new ArgumentException("Descriptions must contain unique entities.");
        }
        foreach (var reference in missing)
        {
            if (!seen.Add(reference.Key))
                throw new ArgumentException("Descriptions and missing must not overlap.");
        }
        Revision = revision;
        Descriptions = descriptions.ToArray();
        Missing = missing.ToArray();
    }

    public string Revision { get; }
    public IReadOnlyList<EntityDescription> Descriptions { get; }
    public IReadOnlyList<EntityReference> Missing { get; }
}
