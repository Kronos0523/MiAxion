using MiAxion.Protocol.Contracts;
using MiAxion.Protocol.Serialization;

namespace MiAxion.Protocol.Tests;

public sealed class WorldQueryProgressTests
{
    [Fact]
    public void DenseProgressRetainsTheBoundedCounterBudget()
    {
        var progress = new WorldQueryProgressResponse(
            true,
            "nearby.scan",
            ProtocolConstants.WorldQueryProgressMaximum,
            ProtocolConstants.WorldQueryProgressMaximum,
            ProtocolConstants.WorldQueryProgressMaximum,
            ProtocolConstants.WorldQueryProgressMaximum);

        var restored = ProtocolJson.Deserialize<WorldQueryProgressResponse>(
            ProtocolJson.Serialize(progress));

        Assert.Equal(progress, restored);
    }

    [Theory]
    [InlineData(1_048_577, 1_048_577, 1_048_577, 1_048_577)]
    [InlineData(0, 1_048_577, 0, 0)]
    public void ProgressRejectsCountersOutsideTheDiagnosticBudget(
        int completed,
        int total,
        int examined,
        int loaded) =>
        Assert.Throws<ArgumentOutOfRangeException>(() => new WorldQueryProgressResponse(
            true, "nearby.scan", completed, total, examined, loaded));
}
