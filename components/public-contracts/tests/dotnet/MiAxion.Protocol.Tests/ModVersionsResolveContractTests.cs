using System.Text.Json;
using MiAxion.Protocol.Contracts;
using MiAxion.Protocol.Serialization;

namespace MiAxion.Protocol.Tests;

public sealed class ModVersionsResolveContractTests
{
    [Fact]
    public void RequestIsClosedBoundedAndAcceptsOnlyExactLoaderIds()
    {
        var request = new ModVersionsResolveRequest(["create", "jei"]);
        var wire = ProtocolJson.Serialize(request);
        Assert.Equal("[\"create\",\"jei\"]", ProtocolJson.Serialize(
            ProtocolJson.Deserialize<ModVersionsResolveRequest>(wire).ModIds));
        Assert.Throws<ArgumentException>(() => new ModVersionsResolveRequest([]));
        Assert.Throws<ArgumentException>(() => new ModVersionsResolveRequest(Enumerable.Range(0, 33)
            .Select(static index => $"mod{index}").ToArray()));
        Assert.Throws<ArgumentException>(() => new ModVersionsResolveRequest(["create", "create"]));
        Assert.Throws<ArgumentException>(() => new ModVersionsResolveRequest(["example:create"]));
        Assert.Throws<JsonException>(() => ProtocolJson.Deserialize<ModVersionsResolveRequest>(
            "{\"modIds\":[\"create\"],\"allMods\":true}"));
    }

    [Fact]
    public void ResponseCanRepresentLoadedAndSuccessfullyAbsentIdsSeparatelyFromLookupFailure()
    {
        var loaded = new ModVersionsResolveResponse(new Dictionary<string, string>(StringComparer.Ordinal)
        {
            ["create"] = "0.8.0"
        });
        var restored = ProtocolJson.Deserialize<ModVersionsResolveResponse>(ProtocolJson.Serialize(loaded));
        Assert.Equal("0.8.0", restored.Versions["create"]);
        Assert.Empty(new ModVersionsResolveResponse(new Dictionary<string, string>()).Versions);

        Assert.Throws<ArgumentException>(() => new ModVersionsResolveResponse(new Dictionary<string, string>
        {
            ["example:create"] = "0.8.0"
        }));
    }
}
