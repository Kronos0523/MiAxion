using System.Text.Json.Serialization;

namespace MiAxion.Protocol.Contracts;

public enum ProtocolErrorCode
{
    [JsonStringEnumMemberName("INVALID_REQUEST")]
    InvalidRequest,

    [JsonStringEnumMemberName("UNAUTHORIZED")]
    Unauthorized,

    [JsonStringEnumMemberName("UNSUPPORTED_VERSION")]
    UnsupportedVersion,

    [JsonStringEnumMemberName("CAPABILITY_UNAVAILABLE")]
    CapabilityUnavailable,

    [JsonStringEnumMemberName("GAME_NOT_READY")]
    GameNotReady,

    [JsonStringEnumMemberName("NOT_FOUND")]
    NotFound,

    [JsonStringEnumMemberName("TIMEOUT")]
    Timeout,

    [JsonStringEnumMemberName("RATE_LIMITED")]
    RateLimited,

    [JsonStringEnumMemberName("DETAILS_HIDDEN")]
    DetailsHidden,

    [JsonStringEnumMemberName("RESULT_TOO_LARGE")]
    ResultTooLarge,

    [JsonStringEnumMemberName("INTERNAL_ERROR")]
    InternalError
}

public sealed record ProtocolError(
    ProtocolErrorCode Code,
    string Message,
    bool Retryable,
    object? Details);
