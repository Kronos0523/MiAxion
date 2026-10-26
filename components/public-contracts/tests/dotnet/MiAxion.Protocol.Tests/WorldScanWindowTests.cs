using MiAxion.Protocol.Contracts;
using MiAxion.Protocol.Serialization;

namespace MiAxion.Protocol.Tests;

public sealed class WorldScanWindowTests
{
    [Theory]
    [InlineData(false, true, true)]
    [InlineData(true, true, false)]
    [InlineData(false, false, false)]
    public void NextPageRequiresAnExplicitIncompleteSummary(bool complete, bool truncated, bool valid)
    {
        const string cursor = "0123456789abcdef0123456789abcdef";
        var summary = new WorldQuerySummary(WorldQueryAuthority.LoadedObserved, "minecraft:overworld",
            0, "q1", 1, 1, 129, 0, complete, truncated, []);
        if (!valid)
        {
            Assert.Throws<ArgumentException>(() => new WorldMachinesResponse(summary, WorldChunkRange.ThreeByThree,
                WorldVerticalRange.Ten, [], nextCursor: cursor));
            Assert.Throws<ArgumentException>(() => new WorldContainersResponse(summary, WorldChunkRange.ThreeByThree,
                WorldVerticalRange.Ten, [], nextCursor: cursor));
            return;
        }
        var machines = new WorldMachinesResponse(summary, WorldChunkRange.ThreeByThree, WorldVerticalRange.Ten, [], nextCursor: cursor);
        var containers = new WorldContainersResponse(summary, WorldChunkRange.ThreeByThree, WorldVerticalRange.Ten, [], nextCursor: cursor);
        Assert.Equal(cursor, ProtocolJson.Deserialize<WorldMachinesResponse>(ProtocolJson.Serialize(machines)).NextCursor);
        Assert.Equal(cursor, ProtocolJson.Deserialize<WorldContainersResponse>(ProtocolJson.Serialize(containers)).NextCursor);
    }

    [Theory]
    [InlineData("")]
    [InlineData("0123456789abcdef0123456789abcdef")]
    public void PagedRequestRetainsExplicitStartOrOpaqueContinuation(string cursor)
    {
        var request = new WorldNearbyScanRequest(window: new WorldScanWindow(7, 30), pageCursor: cursor);
        var restored = ProtocolJson.Deserialize<WorldNearbyScanRequest>(ProtocolJson.Serialize(request));
        Assert.Equal(cursor, restored.PageCursor);
        Assert.Equal(request.Window, restored.Window);
    }

    [Theory]
    [InlineData("world-position")]
    [InlineData("0123456789abcdef0123456789abcde")]
    [InlineData("0123456789ABCDEF0123456789ABCDEF")]
    public void ContinuationCannotCarryArbitraryText(string cursor) =>
        Assert.Throws<ArgumentException>(() => new WorldNearbyScanRequest(pageCursor: cursor));

    [Theory]
    [InlineData(1, 1)]
    [InlineData(7, 30)]
    [InlineData(15, 128)]
    public void BoundedWindowRetainsExactNumericValues(int width, int radius)
    {
        var window = new WorldScanWindow(width, radius);
        var restored = ProtocolJson.Deserialize<WorldScanWindow>(ProtocolJson.Serialize(window));
        Assert.Equal(width, restored.ChunkWidth);
        Assert.Equal(radius, restored.VerticalRadius);
    }

    [Theory]
    [InlineData(0, 10)]
    [InlineData(2, 10)]
    [InlineData(17, 10)]
    [InlineData(3, 0)]
    [InlineData(3, 129)]
    public void InvalidWindowCannotBeConstructed(int width, int radius) =>
        Assert.Throws<ArgumentOutOfRangeException>(() => new WorldScanWindow(width, radius));

    [Theory]
    [InlineData("{\"chunkWidth\":7}")]
    [InlineData("{\"verticalRadius\":30}")]
    [InlineData("{\"chunkWidth\":7,\"verticalRadius\":30,\"loadChunks\":true}")]
    public void MissingOrUnknownFieldsAreNotSilentlyDefaulted(string json) =>
        Assert.Throws<System.Text.Json.JsonException>(() => ProtocolJson.Deserialize<WorldScanWindow>(json));

    [Fact]
    public void RequestRetainsExactWindow() {
        var request = new WorldNearbyScanRequest(window: new WorldScanWindow(7, 30));
        var restored = ProtocolJson.Deserialize<WorldNearbyScanRequest>(ProtocolJson.Serialize(request));
        Assert.Equal(request.Window, restored.Window);
    }

    [Fact]
    public void LegacyRequestWireShapeIsUnchanged() =>
        Assert.Equal("{\"chunkRange\":\"3x3\",\"verticalRange\":\"10\"}",
            ProtocolJson.Serialize(new WorldNearbyScanRequest()));
}
