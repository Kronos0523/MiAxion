using System.Text.Json.Serialization;

namespace MiAxion.Protocol.Contracts;

/// <summary>An exact, player-centered scan window; never rounded to a legacy preset.</summary>
public sealed record WorldScanWindow
{
    [JsonConstructor]
    public WorldScanWindow(int chunkWidth, int verticalRadius)
    {
        // Odd widths give a unique center chunk. These are allocation bounds,
        // not permission to load chunks or to extend the request deadline.
        if (chunkWidth is < 1 or > 15 || chunkWidth % 2 == 0)
            throw new ArgumentOutOfRangeException(nameof(chunkWidth));
        if (verticalRadius is < 1 or > 128)
            throw new ArgumentOutOfRangeException(nameof(verticalRadius));
        ChunkWidth = chunkWidth;
        VerticalRadius = verticalRadius;
    }

    public int ChunkWidth { get; }
    public int VerticalRadius { get; }
}
