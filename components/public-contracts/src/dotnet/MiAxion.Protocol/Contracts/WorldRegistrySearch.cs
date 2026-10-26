using System.Text.Json.Serialization;
using System.Text.RegularExpressions;

namespace MiAxion.Protocol.Contracts;

public sealed record WorldRegistrySearchRequest
{
    [JsonConstructor]
    public WorldRegistrySearchRequest(WorldRegistryKind kind, string query, int pageSize = 32,
        string? @namespace = null, string? cursor = null)
    {
        if (!Enum.IsDefined(kind) || query is null || query.Length > 512 || query.Any(char.IsControl) || pageSize is < 1 or > 64)
            throw new ArgumentException("Invalid registry search.");
        if (@namespace is not null && !Regex.IsMatch(@namespace, "^[a-z0-9_.-]{1,128}$")) throw new ArgumentException("Invalid namespace.");
        if (cursor is not null && !Regex.IsMatch(cursor, "^[a-f0-9]{32}$")) throw new ArgumentException("Invalid cursor.");
        Kind = kind; Query = query; PageSize = pageSize; Namespace = @namespace; Cursor = cursor;
    }
    public WorldRegistryKind Kind { get; }
    public string Query { get; }
    public int PageSize { get; }
    public string? Namespace { get; }
    public string? Cursor { get; }
}

public sealed record WorldRegistrySearchResponse
{
    [JsonConstructor]
    public WorldRegistrySearchResponse(string revision, IReadOnlyList<WorldRegistryMatch> entries, int total, string? nextCursor = null)
    {
        Revision = WorldRegistryResolveResponse.RequireRevision(revision);
        Entries = WorldMachinesResponse.CopyUnique(entries, 64, static entry => entry.Key, nameof(entries));
        if (total < entries.Count || nextCursor is not null && !Regex.IsMatch(nextCursor, "^[a-f0-9]{32}$"))
            throw new ArgumentException("Invalid registry page.");
        Total = total; NextCursor = nextCursor;
    }
    public string Revision { get; }
    public IReadOnlyList<WorldRegistryMatch> Entries { get; }
    public int Total { get; }
    public string? NextCursor { get; }
}
