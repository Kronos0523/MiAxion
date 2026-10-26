namespace MiAxion.Protocol.Contracts;

public sealed record CapabilitiesListRequest;

public sealed record CapabilityDescriptor(
    string Name,
    string Version,
    bool Available);

public sealed record CapabilitiesListResponse(
    IReadOnlyList<CapabilityDescriptor> Capabilities);
