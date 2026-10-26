using System.Text.Json.Serialization;

namespace MiAxion.Protocol.Contracts;

public enum HudSeverity
{
    [JsonStringEnumMemberName("info")]
    Info,

    [JsonStringEnumMemberName("warning")]
    Warning,

    [JsonStringEnumMemberName("error")]
    Error
}

public sealed record HudShowRequest(
    string Text,
    HudSeverity Severity,
    int DurationMs);

public sealed record HudShowResponse(bool Shown);

public sealed record HealthPingRequest(string Nonce);

public sealed record HealthPingResponse(
    string Nonce,
    bool GameReady,
    string ProtocolVersion,
    [property: JsonIgnore(Condition = JsonIgnoreCondition.WhenWritingNull)] int? OrdinaryRequestsPerMinute = null,
    [property: JsonIgnore(Condition = JsonIgnoreCondition.WhenWritingNull)] int? OrdinaryRequestBurst = null,
    [property: JsonIgnore(Condition = JsonIgnoreCondition.WhenWritingNull)] string? OrdinaryRequestRateSource = null);
