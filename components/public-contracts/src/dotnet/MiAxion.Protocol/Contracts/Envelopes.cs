using System.Text.Json;

namespace MiAxion.Protocol.Contracts;

public sealed record RequestEnvelope
{
    public RequestEnvelope(
        string protocolVersion,
        Guid requestId,
        DateTimeOffset timestamp,
        DateTimeOffset deadline,
        ProtocolAction action,
        string? sessionToken,
        JsonElement payload,
        object? error)
    {
        if (deadline <= timestamp)
        {
            throw new ArgumentException(
                "Request deadline must be later than its timestamp.",
                nameof(deadline));
        }

        ProtocolVersion = protocolVersion;
        RequestId = requestId;
        Timestamp = timestamp;
        Deadline = deadline;
        Action = action;
        SessionToken = sessionToken;
        Payload = payload;
        Error = error;
    }

    public string ProtocolVersion { get; }

    public Guid RequestId { get; }

    public DateTimeOffset Timestamp { get; }

    public DateTimeOffset Deadline { get; }

    public ProtocolAction Action { get; }

    public string? SessionToken { get; }

    public JsonElement Payload { get; }

    public object? Error { get; }
}

public sealed record ResponseEnvelope(
    string ProtocolVersion,
    Guid RequestId,
    DateTimeOffset Timestamp,
    DateTimeOffset Deadline,
    ProtocolAction Action,
    object? SessionToken,
    JsonElement? Payload,
    ProtocolError? Error);
