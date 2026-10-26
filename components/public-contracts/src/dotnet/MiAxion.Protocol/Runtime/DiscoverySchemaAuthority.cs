namespace MiAxion.Protocol.Runtime;

internal static class DiscoverySchemaAuthority
{
    internal const string ResourceName =
        "MiAxion.Protocol.discovery.schema.json";

    internal static ReadOnlyMemory<byte> LoadUtf8(int version = 1)
    {
        using var stream = typeof(DiscoverySchemaAuthority)
            .Assembly
            .GetManifestResourceStream(version switch
            {
                1 => ResourceName,
                2 => "MiAxion.Protocol.discovery-v2.schema.json",
                _ => throw new ArgumentOutOfRangeException(nameof(version))
            })
            ?? throw new InvalidOperationException(
                "Embedded discovery schema is unavailable.");
        using var buffer = new MemoryStream();
        stream.CopyTo(buffer);
        return buffer.ToArray();
    }
}
