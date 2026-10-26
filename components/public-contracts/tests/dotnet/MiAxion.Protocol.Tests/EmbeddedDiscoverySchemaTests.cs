using System.Text.Json;
using System.Reflection;
using MiAxion.Protocol.Contracts;
using MiAxion.Protocol.Runtime;

namespace MiAxion.Protocol.Tests;

public sealed class EmbeddedDiscoverySchemaTests
{
    private static readonly string[] SupportedIdentities = ["1.20.1", "1.21.1", "26.3", "27.1-snapshot-1"];

    [Fact]
    public void V2AddsOnlyLocalInstanceHintsAndKeepsV1Available()
    {
        using var schema = JsonDocument.Parse(DiscoverySchemaAuthority.LoadUtf8(2));
        var root = schema.RootElement;
        Assert.False(root.GetProperty("additionalProperties").GetBoolean());
        Assert.Equal(2, root.GetProperty("properties").GetProperty("schemaVersion").GetProperty("const").GetInt32());
        Assert.Contains(root.GetProperty("required").EnumerateArray(), item => item.GetString() == "gameDirectory");
        Assert.DoesNotContain(root.GetProperty("required").EnumerateArray(), item => item.GetString() == "displayNameHint");
        Assert.Throws<ArgumentOutOfRangeException>(() => DiscoverySchemaAuthority.LoadUtf8(3));
        using var previous = JsonDocument.Parse(DiscoverySchemaAuthority.LoadUtf8());
        Assert.False(previous.RootElement.GetProperty("properties").TryGetProperty("gameDirectory", out _));
    }

    [Fact]
    public void LoadsEmbeddedSchemaOutsideRepositoryWorkingDirectory()
    {
        var isolated = Path.Combine(Path.GetTempPath(), $"miaxion-schema-{Guid.NewGuid():N}");
        Directory.CreateDirectory(isolated);
        var original = Environment.CurrentDirectory;
        try
        {
            Environment.CurrentDirectory = isolated;
            using var document = JsonDocument.Parse(DiscoverySchemaAuthority.LoadUtf8());
            var properties = document.RootElement.GetProperty("properties");

            Assert.Equal(ProtocolConstants.DiscoverySchemaVersion, properties.GetProperty("schemaVersion").GetProperty("const").GetInt32());
            Assert.Equal(ProtocolConstants.DiscoverySubprotocol, properties.GetProperty("subprotocol").GetProperty("const").GetString());
            Assert.Equal(ProtocolConstants.Version, properties.GetProperty("protocolVersion").GetProperty("const").GetString());
            Assert.Equal("string", properties.GetProperty("minecraftVersion").GetProperty("type").GetString());
            Assert.False(properties.GetProperty("minecraftVersion").TryGetProperty("enum", out _));
        }
        finally
        {
            Environment.CurrentDirectory = original;
            Directory.Delete(isolated);
        }
    }

    [Fact]
    public void DiscoveryMinecraftIdentityMatchesTheEmbeddedSchemaWithoutReleaseAllowlist()
    {
        using var document = JsonDocument.Parse(DiscoverySchemaAuthority.LoadUtf8());
        var identity = document.RootElement.GetProperty("properties").GetProperty("minecraftVersion");
        Assert.Equal("string", identity.GetProperty("type").GetString());
        Assert.False(identity.TryGetProperty("enum", out _));
        Assert.All(SupportedIdentities, version =>
            Assert.True(ProtocolConstants.IsValidDiscoveryMinecraftVersion(version)));
        Assert.False(ProtocolConstants.IsValidDiscoveryMinecraftVersion(""));
        Assert.False(ProtocolConstants.IsValidDiscoveryMinecraftVersion("invalid/version"));
        Assert.False(ProtocolConstants.IsValidDiscoveryMinecraftVersion(new string('a', 65)));
    }

    [Fact]
    public void AssemblyContainsDiscoverySchemaWithDeterministicLogicalName()
    {
        Assert.Contains(
            DiscoverySchemaAuthority.ResourceName,
            typeof(ProtocolConstants).Assembly.GetManifestResourceNames());
    }

    [Fact]
    public void DiscoverySchemaTestsHaveNoSourceTreePathResolver()
    {
        Assert.DoesNotContain(
            typeof(DiscoverySchemaTests).GetMethods(
                BindingFlags.NonPublic | BindingFlags.Static),
            method => method.Name.Contains(
                "SchemaPath",
                StringComparison.Ordinal));
    }
}
