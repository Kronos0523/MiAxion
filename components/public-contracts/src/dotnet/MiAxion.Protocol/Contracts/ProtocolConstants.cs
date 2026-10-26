namespace MiAxion.Protocol.Contracts;

public static class ProtocolConstants
{
    public const string Version = "1.0";
    public const string RecipeCapabilityVersion = "1.9";
    public const string RecipeSourceCapabilityVersion = "1.3";
    public const int RecipeSourcePageDefault = 4096;
    public const int RecipeSourcePageMaximum = 4096;
    public const int RecipeSourceCursorMaximum = 128;
    public const string QuestCapabilityVersion = "1.1";
    public const string EntityResolveCapabilityVersion = "1.1";
    public const string EntityDescribeCapabilityVersion = "1.0";
    public const string ModVersionsResolveCapabilityVersion = "1.0";
    public const string WorldNearbyCapabilityVersion = "1.2";
    public const string WorldStructureTargetsCapabilityVersion = "1.1";
    public const int QuestQueryResultMaximum = 32;
    public const int QuestDetailCollectionMaximum = 128;
    public const int QuestDescriptionLineScalarMaximum = 1024;
    public const int QuestTitleScalarMaximum = 256;
    public const long QuestProgressIntegerMaximum = 9007199254740991;
    public const int RecipeSlotAlternativesMaximum = 16384;
    public const int RecipeInputSlotsMaximum = 4096;
    public const int RecipeCatalystSlotsMaximum = 4096;
    public const int RecipeOutputSlotsMaximum = 4096;
    public const int WorldQueryResultMaximum = 128;
    public const int WorldQueryMachineFeatureMaximum = 32;
    public const int WorldQueryContainerStackMaximum = 1024;
    public const int WorldQueryNameCandidateMaximum = 8;
    public const int WorldQueryWarningMaximum = 16;
    public const int WorldQueryStructurePieceMaximum = 256;
    // Progress is a diagnostic channel, not an unbounded world-result counter.
    // Keep enough headroom for dense scans while rejecting malformed peers.
    public const int WorldQueryProgressMaximum = 1_048_576;
    public const long MaximumSafeJsonInteger = 9007199254740991;
    public const int DiscoverySchemaVersion = 1;
    public const int DiscoveryRecordMaximumBytes = 4096;
    public const int DiscoveryV2RecordMaximumBytes = 16 * 1024;
    public const int DiscoveryProcessStartToleranceMilliseconds = 2000;
    public const string DiscoveryRecordExtension = ".json";
    public const string DiscoveryEndpointScheme = "ws";
    public const string DiscoveryEndpointPath = "/miaxion/bridge/v1";
    public const string DiscoverySubprotocol = "miaxion.bridge.v1";
    public const string DiscoveryMinecraftVersion = "1.21.1";

    // Metadata identity only. Compatibility is decided by protocol/subprotocol and capabilities,
    // never by an allowlist of Minecraft releases.
    public static bool IsValidDiscoveryMinecraftVersion(string version) =>
        version is { Length: >= 1 and <= 64 } && char.IsAsciiLetterOrDigit(version[0]) &&
        version.All(character => char.IsAsciiLetterOrDigit(character) || character is '.' or '_' or '+' or '-');
}
