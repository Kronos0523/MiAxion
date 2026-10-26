using System.Collections.Immutable;
using System.Text;
using System.Text.Json;
using System.Text.Json.Serialization;
using System.Text.RegularExpressions;

namespace MiAxion.Protocol.Contracts;

internal static class ExtensionPayloadValidation
{
    internal static void Text(string value, int maximum)
    {
        if (string.IsNullOrWhiteSpace(value) || value.Length > maximum || value.Any(char.IsControl))
            throw new ArgumentException("Invalid extension metadata.");
        Unicode(value);
    }
    internal static void Version(string value)
    {
        Text(value, 32);
        if (!Regex.IsMatch(value, "^[0-9]+\\.[0-9]+$", RegexOptions.CultureInvariant)) throw new ArgumentException("Invalid extension version.");
    }
    internal static void Id(string value)
    {
        Text(value, 128);
        if (!Regex.IsMatch(value, "^[a-z0-9_.-]+:[a-z0-9_./-]+$", RegexOptions.CultureInvariant)) throw new ArgumentException("Invalid extension identifier.");
    }
    internal static JsonElement Object(JsonElement value)
    {
        if (value.ValueKind != JsonValueKind.Object || Encoding.UTF8.GetByteCount(value.GetRawText()) > 16384)
            throw new ArgumentException("Invalid extension data size or shape.");
        Check(value, 0);
        return value.Clone();
    }
    private static void Check(JsonElement value, int depth)
    {
        if (depth > 24) throw new ArgumentException("Extension data is too deep.");
        if (value.ValueKind == JsonValueKind.Object)
        {
            var names = new HashSet<string>(StringComparer.Ordinal);
            foreach (var item in value.EnumerateObject())
            {
                string name;
                try { name = item.Name; Unicode(name); }
                catch (Exception exception) when (exception is ArgumentException or InvalidOperationException)
                { throw new ArgumentException("Invalid extension data field."); }
                if (!names.Add(name)) throw new ArgumentException("Duplicate extension data field.");
                Check(item.Value, depth + 1);
            }
        }
        else if (value.ValueKind == JsonValueKind.Array)
            foreach (var item in value.EnumerateArray()) Check(item, depth + 1);
        else if (value.ValueKind == JsonValueKind.String)
        {
            try { _ = new UTF8Encoding(false, true).GetByteCount(value.GetString()!); }
            catch (Exception exception) when (exception is ArgumentException or InvalidOperationException)
            { throw new ArgumentException("Invalid extension text."); }
        }
    }

    private static void Unicode(string value)
    {
        try { _ = new UTF8Encoding(false, true).GetByteCount(value); }
        catch (EncoderFallbackException) { throw new ArgumentException("Invalid extension text."); }
    }
}

public sealed record ExtensionListRequest
{
    [JsonConstructor]
    public ExtensionListRequest(string? cursor, string? catalogRevision)
    {
        if ((cursor is null) != (catalogRevision is null)) throw new ArgumentException("Cursor requires its catalog revision.");
        if (cursor is not null) ExtensionPayloadValidation.Text(cursor, 128);
        if (catalogRevision is not null) ExtensionPayloadValidation.Text(catalogRevision, 128);
        Cursor = cursor;
        CatalogRevision = catalogRevision;
    }
    public string? Cursor { get; }
    public string? CatalogRevision { get; }
}

public sealed record ExtensionCallRequest
{
    [JsonConstructor]
    public ExtensionCallRequest(string capabilityId, string capabilityVersion, string catalogRevision, JsonElement arguments)
    {
        ExtensionPayloadValidation.Id(capabilityId);
        ExtensionPayloadValidation.Version(capabilityVersion);
        ExtensionPayloadValidation.Text(catalogRevision, 128);
        CapabilityId = capabilityId;
        CapabilityVersion = capabilityVersion;
        CatalogRevision = catalogRevision;
        Arguments = ExtensionPayloadValidation.Object(arguments);
    }
    public string CapabilityId { get; }
    public string CapabilityVersion { get; }
    public string CatalogRevision { get; }
    public JsonElement Arguments { get; }
}

public sealed record ExtensionDependencyDto
{
    [JsonConstructor]
    public ExtensionDependencyDto(string capabilityId, string capabilityVersion)
    {
        ExtensionPayloadValidation.Text(capabilityId, 128);
        ExtensionPayloadValidation.Version(capabilityVersion);
        CapabilityId = capabilityId;
        CapabilityVersion = capabilityVersion;
    }
    public string CapabilityId { get; }
    public string CapabilityVersion { get; }
}

public sealed record ExtensionDescriptorDto
{
    [JsonConstructor]
    public ExtensionDescriptorDto(string extensionId, string extensionVersion, string capabilityId, string capabilityVersion,
        string title, string description, JsonElement inputSchema, JsonElement outputSchema, string dataClass,
        IReadOnlyList<ExtensionDependencyDto> dependencies, string executionContext, bool available, string? unavailableReason,
        int maximumDurationMs, int maximumResultBytes)
    {
        ExtensionPayloadValidation.Text(extensionId, 128);
        if (!Regex.IsMatch(extensionId, "^[a-z0-9_.-]+$", RegexOptions.CultureInvariant)) throw new ArgumentException("Invalid extension owner.");
        ExtensionPayloadValidation.Version(extensionVersion);
        ExtensionPayloadValidation.Id(capabilityId);
        if (!capabilityId.StartsWith(extensionId + ":", StringComparison.Ordinal)) throw new ArgumentException("Extension namespace mismatch.");
        ExtensionPayloadValidation.Version(capabilityVersion);
        ExtensionPayloadValidation.Text(title, 256);
        ArgumentException.ThrowIfNullOrWhiteSpace(description);
        if (description.Length > 2048) throw new ArgumentException("Extension description is too long.");
        if (dataClass != "ExtensionGameData" || executionContext is not ("client" or "integrated-server") ||
            maximumDurationMs is < 1 or > 10000 || maximumResultBytes is < 1 or > 16384 ||
            available && unavailableReason is not null || !available && unavailableReason is not ("DEPENDENCY_MISSING" or "UNSUPPORTED_VERSION" or "GAME_NOT_READY"))
            throw new ArgumentException("Invalid extension availability or budget.");
        ArgumentNullException.ThrowIfNull(dependencies);
        if (dependencies.Count > 16 || dependencies.Any(static item => item is null) || dependencies.Distinct().Count() != dependencies.Count)
            throw new ArgumentException("Invalid extension dependencies.");
        ExtensionId = extensionId; ExtensionVersion = extensionVersion; CapabilityId = capabilityId; CapabilityVersion = capabilityVersion;
        Title = title; Description = description;
        InputSchema = ExtensionPayloadValidation.Object(inputSchema); OutputSchema = ExtensionPayloadValidation.Object(outputSchema);
        DataClass = dataClass; Dependencies = dependencies.ToImmutableArray(); ExecutionContext = executionContext;
        Available = available; UnavailableReason = unavailableReason; MaximumDurationMs = maximumDurationMs; MaximumResultBytes = maximumResultBytes;
    }
    public string ExtensionId { get; }
    public string ExtensionVersion { get; }
    public string CapabilityId { get; }
    public string CapabilityVersion { get; }
    public string Title { get; }
    public string Description { get; }
    public JsonElement InputSchema { get; }
    public JsonElement OutputSchema { get; }
    public string DataClass { get; }
    public IReadOnlyList<ExtensionDependencyDto> Dependencies { get; }
    public string ExecutionContext { get; }
    public bool Available { get; }
    public string? UnavailableReason { get; }
    public int MaximumDurationMs { get; }
    public int MaximumResultBytes { get; }
}

public sealed record ExtensionListResponse
{
    [JsonConstructor]
    public ExtensionListResponse(string catalogRevision, IReadOnlyList<ExtensionDescriptorDto> capabilities, string? nextCursor,
        Guid? worldScope = null)
    {
        ExtensionPayloadValidation.Text(catalogRevision, 128);
        if (nextCursor is not null) ExtensionPayloadValidation.Text(nextCursor, 128);
        ArgumentNullException.ThrowIfNull(capabilities);
        if (capabilities.Count > 4 || capabilities.Any(static item => item is null) || capabilities.Select(static item => item.CapabilityId).Distinct().Count() != capabilities.Count)
            throw new ArgumentException("Invalid extension page.");
        CatalogRevision = catalogRevision; Capabilities = capabilities.ToImmutableArray(); NextCursor = nextCursor;
        if (worldScope == Guid.Empty) throw new ArgumentException("Invalid extension world scope.");
        WorldScope = worldScope;
    }
    public string CatalogRevision { get; }
    public IReadOnlyList<ExtensionDescriptorDto> Capabilities { get; }
    public string? NextCursor { get; }
    public Guid? WorldScope { get; }
}

public sealed record ExtensionCallResponse
{
    [JsonConstructor]
    public ExtensionCallResponse(string status, JsonElement? data)
    {
        if (status == "SUCCESS")
        {
            if (data is null) throw new ArgumentException("Extension success requires data.");
            Data = ExtensionPayloadValidation.Object(data.Value);
        }
        else if (status is not ("UNAVAILABLE" or "DEPENDENCY_MISSING" or "UNSUPPORTED_VERSION" or "INVALID_ARGUMENTS" or
            "AUTHORITY_CHANGED" or "RATE_LIMITED" or "TIMED_OUT" or "CANCELLED" or "EXECUTION_FAILED" or "UNSUPPORTED_RESULT" or "RESULT_TOO_LARGE") ||
            data is { ValueKind: not JsonValueKind.Null }) throw new ArgumentException("Invalid extension failure.");
        Status = status;
    }
    public string Status { get; }
    public JsonElement? Data { get; }
}
