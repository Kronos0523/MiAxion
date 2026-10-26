using System.Reflection;
using System.Text;
using System.Text.Json;
using System.Text.Json.Serialization;

namespace MiAxion.Protocol.Runtime;

public static class ProtocolRuntimeLimitsLoader
{
    private const string ResourceName =
        "MiAxion.Protocol.runtime-limits.json";
    private static readonly UTF8Encoding StrictUtf8 =
        new(encoderShouldEmitUTF8Identifier: false, throwOnInvalidBytes: true);
    private static readonly JsonSerializerOptions SerializerOptions =
        CreateSerializerOptions();
    private static readonly HashSet<string> ExpectedPropertyNames =
        typeof(ProtocolRuntimeLimits)
            .GetProperties(BindingFlags.Instance | BindingFlags.Public)
            .Select(property =>
                JsonNamingPolicy.CamelCase.ConvertName(property.Name))
            .ToHashSet(StringComparer.Ordinal);

    public static ProtocolRuntimeLimits Load()
    {
        using var stream = typeof(ProtocolRuntimeLimitsLoader)
            .Assembly
            .GetManifestResourceStream(ResourceName)
            ?? throw new InvalidOperationException(
                $"Embedded resource is missing: {ResourceName}");
        using var buffer = new MemoryStream();
        stream.CopyTo(buffer);
        return Parse(buffer.ToArray());
    }

    public static ProtocolRuntimeLimits Parse(
        ReadOnlySpan<byte> utf8Json)
    {
        if (utf8Json.StartsWith(
            new byte[] { 0xef, 0xbb, 0xbf }))
        {
            throw new JsonException("UTF-8 BOM is forbidden.");
        }

        string json;
        try
        {
            json = StrictUtf8.GetString(utf8Json);
        }
        catch (DecoderFallbackException exception)
        {
            throw new JsonException(
                "Runtime limits are not valid UTF-8.",
                exception);
        }

        using var document = JsonDocument.Parse(
            json,
            new JsonDocumentOptions
            {
                AllowTrailingCommas = false,
                CommentHandling = JsonCommentHandling.Disallow
            });
        if (document.RootElement.ValueKind != JsonValueKind.Object)
        {
            throw new JsonException(
                "Runtime limits authority must be a JSON object.");
        }

        var seen = new HashSet<string>(StringComparer.Ordinal);
        foreach (var property in document.RootElement.EnumerateObject())
        {
            if (!ExpectedPropertyNames.Contains(property.Name))
            {
                throw new JsonException(
                    $"Unknown runtime limit property: {property.Name}");
            }

            if (!seen.Add(property.Name))
            {
                throw new JsonException(
                    $"Duplicate runtime limit property: {property.Name}");
            }
        }

        if (!seen.SetEquals(ExpectedPropertyNames))
        {
            var missing = ExpectedPropertyNames
                .Except(seen, StringComparer.Ordinal)
                .Order(StringComparer.Ordinal);
            throw new JsonException(
                $"Missing runtime limit properties: {string.Join(", ", missing)}");
        }

        var limits = JsonSerializer.Deserialize<ProtocolRuntimeLimits>(
            json,
            SerializerOptions)
            ?? throw new JsonException(
                "Runtime limits authority deserialized to null.");

        if (limits.SchemaVersion != 1 ||
            typeof(ProtocolRuntimeLimits)
                .GetProperties(BindingFlags.Instance | BindingFlags.Public)
                .Any(property =>
                    property.GetValue(limits) is int value && value <= 0))
        {
            throw new JsonException(
                "Runtime limit values and schema version must be positive and supported.");
        }

        return limits;
    }

    private static JsonSerializerOptions CreateSerializerOptions()
    {
        var options = new JsonSerializerOptions
        {
            PropertyNamingPolicy = JsonNamingPolicy.CamelCase,
            PropertyNameCaseInsensitive = false,
            UnmappedMemberHandling = JsonUnmappedMemberHandling.Disallow,
            RespectRequiredConstructorParameters = true,
            RespectNullableAnnotations = true,
            ReadCommentHandling = JsonCommentHandling.Disallow,
            AllowTrailingCommas = false
        };
        options.MakeReadOnly(populateMissingResolver: true);
        return options;
    }
}
