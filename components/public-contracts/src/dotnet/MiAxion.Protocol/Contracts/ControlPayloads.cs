using System.Text.Json.Serialization;

namespace MiAxion.Protocol.Contracts;

public sealed record SessionPairRequest(
    string ClientName,
    string ClientVersion);

public sealed record SessionPairApproved(
    string SessionToken,
    string TokenType);

public enum SessionRevokeReason
{
    [JsonStringEnumMemberName("user_requested")]
    UserRequested,

    [JsonStringEnumMemberName("app_shutdown")]
    AppShutdown
}

public sealed record SessionRevokeRequest(SessionRevokeReason Reason);

public sealed record SessionRevokeResponse(bool Revoked);

public sealed record RequestCancelRequest(Guid TargetRequestId);

public enum CancelState
{
    [JsonStringEnumMemberName("cancelled")]
    Cancelled,

    [JsonStringEnumMemberName("already_completed")]
    AlreadyCompleted,

    [JsonStringEnumMemberName("not_cancellable")]
    NotCancellable
}

public sealed record RequestCancelResponse(
    Guid TargetRequestId,
    bool Cancelled,
    CancelState State);
