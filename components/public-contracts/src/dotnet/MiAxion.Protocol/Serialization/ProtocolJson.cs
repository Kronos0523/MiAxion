using System.Text.Json;
using System.Text.Json.Serialization;
using MiAxion.Protocol.Contracts;

namespace MiAxion.Protocol.Serialization;

public static class ProtocolJson
{
    public static JsonSerializerOptions Options { get; } = CreateOptions();

    // Read-side additive metadata is not a request instruction. Keep required
    // members, nullability and value converters, while accepting new fields in
    // responses from compatible Bridge implementations. Request parsing stays strict.
    public static JsonSerializerOptions ResponseOptions { get; } = CreateResponseOptions();

    private static JsonSerializerOptions CreateResponseOptions()
    {
        var options = new JsonSerializerOptions(Options)
        {
            UnmappedMemberHandling = JsonUnmappedMemberHandling.Skip
        };
        options.MakeReadOnly();
        return options;
    }

    public static string Serialize<T>(T value) =>
        JsonSerializer.Serialize(value, Options);

    public static T Deserialize<T>(string json) =>
        JsonSerializer.Deserialize<T>(json, Options)
        ?? throw new JsonException($"JSON did not contain {typeof(T).Name}.");

    private static JsonSerializerOptions CreateOptions()
    {
        var options = new JsonSerializerOptions
        {
            PropertyNamingPolicy = JsonNamingPolicy.CamelCase,
            PropertyNameCaseInsensitive = false,
            UnmappedMemberHandling = JsonUnmappedMemberHandling.Disallow,
            RespectRequiredConstructorParameters = true,
            RespectNullableAnnotations = true,
            DefaultIgnoreCondition = JsonIgnoreCondition.Never,
            ReadCommentHandling = JsonCommentHandling.Disallow,
            AllowTrailingCommas = false
        };

        options.Converters.Add(
            new JsonStringEnumConverter<ProtocolErrorCode>(
                namingPolicy: null,
                allowIntegerValues: false));
        options.Converters.Add(
            new JsonStringEnumConverter<SessionRevokeReason>(
                namingPolicy: null,
                allowIntegerValues: false));
        options.Converters.Add(
            new JsonStringEnumConverter<CancelState>(
                namingPolicy: null,
                allowIntegerValues: false));
        options.Converters.Add(
            new JsonStringEnumConverter<GameMode>(
                namingPolicy: null,
                allowIntegerValues: false));
        options.Converters.Add(
            new JsonStringEnumConverter<HudSeverity>(
                namingPolicy: null,
                allowIntegerValues: false));
        options.Converters.Add(
            new JsonStringEnumConverter<QuestQueryScope>(
                namingPolicy: null,
                allowIntegerValues: false));
        options.Converters.Add(
            new JsonStringEnumConverter<QuestState>(
                namingPolicy: null,
                allowIntegerValues: false));
        options.Converters.Add(
            new JsonStringEnumConverter<QuestDescriptionVisibility>(
                namingPolicy: null,
                allowIntegerValues: false));
        options.Converters.Add(
            new JsonStringEnumConverter<QuestRewardClaimState>(
                namingPolicy: null,
                allowIntegerValues: false));
        options.Converters.Add(
            new JsonStringEnumConverter<WorldQueryAuthority>(
                namingPolicy: null,
                allowIntegerValues: false));
        options.Converters.Add(
            new JsonStringEnumConverter<WorldLocatorRange>(
                namingPolicy: null,
                allowIntegerValues: false));
        options.Converters.Add(
            new JsonStringEnumConverter<WorldChunkRange>(
                namingPolicy: null,
                allowIntegerValues: false));
        options.Converters.Add(
            new JsonStringEnumConverter<WorldVerticalRange>(
                namingPolicy: null,
                allowIntegerValues: false));
        options.Converters.Add(
            new JsonStringEnumConverter<WorldRegistryKind>(
                namingPolicy: null,
                allowIntegerValues: false));
        options.Converters.Add(
            new JsonStringEnumConverter<WorldQueryTargetKind>(
                namingPolicy: null,
                allowIntegerValues: false));
        options.Converters.Add(
            new JsonStringEnumConverter<WorldMachineState>(
                namingPolicy: null,
                allowIntegerValues: false));
        options.Converters.Add(
            new JsonStringEnumConverter<WorldMachineCandidateDomain>(
                namingPolicy: null,
                allowIntegerValues: false));
        options.Converters.Add(
            new JsonStringEnumConverter<WorldContainerView>(
                namingPolicy: null,
                allowIntegerValues: false));

        options.MakeReadOnly(populateMissingResolver: true);
        return options;
    }
}
