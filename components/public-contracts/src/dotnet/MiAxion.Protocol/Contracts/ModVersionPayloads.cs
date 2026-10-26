using System.Text.Json.Serialization;
using System.Text.RegularExpressions;

namespace MiAxion.Protocol.Contracts;

/// <summary>Requests versions only for exact Mod IDs selected by local knowledge metadata.</summary>
public sealed record ModVersionsResolveRequest
{
    public const int MaximumModIds = 32;
    private static readonly Regex ModIdPattern = new(
        "^[a-z0-9_.-]+$", RegexOptions.CultureInvariant | RegexOptions.NonBacktracking);

    [JsonConstructor]
    public ModVersionsResolveRequest(IReadOnlyList<string> modIds)
    {
        ArgumentNullException.ThrowIfNull(modIds);
        if (modIds.Count is < 1 or > MaximumModIds || modIds.Any(static id => id is null))
            throw new ArgumentException("Mod IDs must contain 1-32 exact identifiers.", nameof(modIds));
        var seen = new HashSet<string>(StringComparer.Ordinal);
        foreach (var id in modIds)
        {
            if (id.Length is < 1 or > 64 || !ModIdPattern.IsMatch(id) || !seen.Add(id))
                throw new ArgumentException("Mod IDs must be unique lowercase identifiers.", nameof(modIds));
        }
        ModIds = modIds.ToArray();
    }

    public IReadOnlyList<string> ModIds { get; }
}

/// <summary>Contains versions for requested IDs that are loaded; omitted IDs are not loaded.</summary>
public sealed record ModVersionsResolveResponse
{
    [JsonConstructor]
    public ModVersionsResolveResponse(IReadOnlyDictionary<string, string> versions)
    {
        ArgumentNullException.ThrowIfNull(versions);
        if (versions.Count > ModVersionsResolveRequest.MaximumModIds ||
            versions.Any(static pair => pair.Key is null || pair.Value is null))
            throw new ArgumentException("Resolved Mod versions exceed the closed contract.", nameof(versions));

        var copy = new Dictionary<string, string>(StringComparer.Ordinal);
        foreach (var (modId, version) in versions)
        {
            if (modId.Length is < 1 or > 64 || !Regex.IsMatch(modId, "^[a-z0-9_.-]+$", RegexOptions.CultureInvariant) ||
                version.Length is < 1 or > 128 || version.EnumerateRunes().Count() > 128)
                throw new ArgumentException("Resolved Mod versions are outside the closed contract.", nameof(versions));
            EntityTextValidation.Require(version, nameof(versions));
            if (!copy.TryAdd(modId, version))
                throw new ArgumentException("Resolved Mod versions contain duplicate IDs.", nameof(versions));
        }
        Versions = copy;
    }

    public IReadOnlyDictionary<string, string> Versions { get; }
}
