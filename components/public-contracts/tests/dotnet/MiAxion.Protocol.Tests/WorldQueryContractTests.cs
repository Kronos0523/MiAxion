using System.Text.Json;
using MiAxion.Protocol.Contracts;
using MiAxion.Protocol.Serialization;

namespace MiAxion.Protocol.Tests;

public sealed class WorldQueryContractTests
{
    [Fact]
    public void DenseLoadedScanProgressIsNotLimitedByTheReturnedPageSize()
    {
        var summary = new WorldQuerySummary(WorldQueryAuthority.LoadedObserved,
            "minecraft:overworld", 1, "fixture", 25, 25, 25_600, 0, false, true, ["RESULT_LIMIT_REACHED"]);
        var response = new WorldMachinesResponse(summary, WorldChunkRange.FiveByFive, WorldVerticalRange.Fifty, []);
        var decoded = ProtocolJson.Deserialize<WorldMachinesResponse>(ProtocolJson.Serialize(response));
        Assert.Equal(25_600, decoded.Summary.Examined);
        Assert.Empty(decoded.Machines);
    }

    [Fact]
    public void StructureTargetContinuationIsOpaqueAndRequiresIncompleteSummary()
    {
        var request = new WorldStructureTargetsRequest(
            "minecraft:village", "minecraft:gold_block", "0123456789abcdef0123456789abcdef");
        var decodedRequest = ProtocolJson.Deserialize<WorldStructureTargetsRequest>(
            ProtocolJson.Serialize(request));
        Assert.Equal(request, decodedRequest);

        var summary = new WorldQuerySummary(WorldQueryAuthority.LoadedObserved,
            "minecraft:overworld", 1, "fixture", 256, 256, 1024, 128, false, true,
            ["RESULT_LIMIT_REACHED"]);
        var response = new WorldStructureTargetsResponse(
            summary, "minecraft:village", "structure_covered_chunks", [],
            "fedcba9876543210fedcba9876543210");
        var decodedResponse = ProtocolJson.Deserialize<WorldStructureTargetsResponse>(
            ProtocolJson.Serialize(response));
        Assert.Equal(response.NextCursor, decodedResponse.NextCursor);
        var completeSummary = new WorldQuerySummary(WorldQueryAuthority.LoadedObserved,
            "minecraft:overworld", 1, "fixture", 1, 1, 1, 1, true, false, []);
        Assert.Throws<ArgumentException>(() => new WorldStructureTargetsResponse(
            completeSummary, "minecraft:village", "structure_covered_chunks", [],
            "fedcba9876543210fedcba9876543210"));
    }

    [Fact]
    public void RequestsUseClosedWireValuesAndRoundTrip()
    {
        var locate = new WorldLocateRequest(
            "minecraft:village",
            WorldLocatorRange.Standard);
        var nearby = new WorldNearbyScanRequest();
        var targets = new WorldStructureTargetsRequest(
            "minecraft:ancient_city",
            "minecraft:gold_block");
        var resolve = new WorldRegistryResolveRequest(
            WorldRegistryKind.Structure,
            "Ancient City");
        var describe = new WorldRegistryDescribeRequest(
        [
            new(WorldRegistryKind.Structure, "minecraft:ancient_city")
        ]);

        Assert.Equal(
            "{\"targetId\":\"minecraft:village\",\"range\":\"standard\"}",
            ProtocolJson.Serialize(locate));
        Assert.Equal(
            "{\"chunkRange\":\"3x3\",\"verticalRange\":\"10\"}",
            ProtocolJson.Serialize(nearby));
        Assert.Equal(
            locate,
            ProtocolJson.Deserialize<WorldLocateRequest>(
                ProtocolJson.Serialize(locate)));
        Assert.Equal(
            nearby,
            ProtocolJson.Deserialize<WorldNearbyScanRequest>(
                ProtocolJson.Serialize(nearby)));
        Assert.Equal(
            targets,
            ProtocolJson.Deserialize<WorldStructureTargetsRequest>(
                ProtocolJson.Serialize(targets)));
        Assert.Equal(
            resolve,
            ProtocolJson.Deserialize<WorldRegistryResolveRequest>(
                ProtocolJson.Serialize(resolve)));
        var roundTripDescribe = ProtocolJson.Deserialize<WorldRegistryDescribeRequest>(
            ProtocolJson.Serialize(describe));
        Assert.Equal(describe.Entries, roundTripDescribe.Entries);
    }

    [Fact]
    public void RequestsRejectUnknownFieldsAndDuplicateRegistryEntries()
    {
        Assert.Throws<JsonException>(() => ProtocolJson.Deserialize<WorldLocateRequest>(
            "{\"targetId\":\"minecraft:village\",\"range\":\"standard\",\"extra\":true}"));
        Assert.Throws<ArgumentException>(() => new WorldRegistryDescribeRequest(
        [
            new(WorldRegistryKind.Structure, "minecraft:village"),
            new(WorldRegistryKind.Structure, "minecraft:village")
        ]));
        Assert.Throws<ArgumentException>(() => new WorldLocateRequest(
            "minecraft:bad id",
            WorldLocatorRange.Near));
    }

    [Fact]
    public void ResponsesRoundTripWithAuthoritySpecificShapes()
    {
        var generatorSummary = new WorldQuerySummary(
            WorldQueryAuthority.GeneratorLocated,
            "minecraft:overworld",
            120,
            "z1",
            1,
            0,
            1,
            1,
            complete: true,
            truncated: false,
            []);
        var loadedSummary = new WorldQuerySummary(
            WorldQueryAuthority.LoadedObserved,
            "minecraft:overworld",
            120,
            "z1",
            1,
            1,
            1,
            1,
            complete: true,
            truncated: false,
            []);
        var position = new WorldBlockPosition(1, 64, 2);
        var energy = new WorldMachineFeature("energy", "fe", 40, 100);
        var machine = new WorldMachineObservation(
            "minecraft:furnace",
            "Furnace",
            position,
            3,
            "generic",
            WorldMachineCandidateDomain.BlockEntityOnly,
            WorldMachineState.Unknown,
            [energy]);
        var container = new WorldContainerObservation(
            "minecraft:chest", "Chest", position, 3, WorldContainerView.Full);
        var target = new WorldTargetObservation(
            "minecraft:gold_block",
            "Block of Gold",
            position,
            3,
            WorldQueryTargetKind.MetalStorageBlock,
            10);
        var registryMatch = new WorldRegistryMatch(
            WorldRegistryKind.Structure,
            "minecraft:ancient_city",
            "structure.minecraft.ancient_city",
            "Ancient City");

        var locator = new WorldLocatedResponse(
            generatorSummary,
            "minecraft:village",
            "Village",
            100,
            -40,
            null,
            108,
            WorldLocatorRange.Standard,
            1);
        var machines = new WorldMachinesResponse(
            loadedSummary,
            WorldChunkRange.ThreeByThree,
            WorldVerticalRange.Ten,
            [machine]);
        var containers = new WorldContainersResponse(
            loadedSummary,
            WorldChunkRange.OneByOne,
            WorldVerticalRange.Fifty,
            [container]);
        var targets = new WorldStructureTargetsResponse(
            loadedSummary,
            "minecraft:ancient_city",
            "structure_covered_chunks",
            [target]);
        var resolved = new WorldRegistryResolveResponse("z1", [registryMatch], false);
        var described = new WorldRegistryDescribeResponse(
            "z1",
            [registryMatch],
            [new WorldRegistryReference(WorldRegistryKind.Biome, "minecraft:plains")]);

        AssertJsonRoundTrip(locator);
        AssertJsonRoundTrip(machines);
        AssertJsonRoundTrip(containers);
        AssertJsonRoundTrip(targets);
        AssertJsonRoundTrip(resolved);
        AssertJsonRoundTrip(described);
    }

    [Fact]
    public void MachineFeaturesAreBoundedAndRejectDuplicateIds()
    {
        Assert.Throws<ArgumentException>(() => new WorldMachineFeature(
            "bad id", "fe", 1));
        Assert.Throws<ArgumentException>(() => new WorldMachineFeature(
            "energy", "fe%", 1));
        var feature = new WorldMachineFeature("energy", "fe", 40, 100);
        Assert.Throws<ArgumentException>(() => new WorldMachineObservation(
            "minecraft:furnace",
            "Furnace",
            new WorldBlockPosition(1, 64, 2),
            3,
            "generic",
            WorldMachineCandidateDomain.BlockEntityOnly,
            WorldMachineState.Unknown,
            [feature, feature]));
        Assert.NotNull(new WorldMachineObservation(
            "minecraft:furnace",
            "Furnace",
            new WorldBlockPosition(1, 64, 2),
            3,
            "generic",
            WorldMachineCandidateDomain.BlockEntityOnly,
            WorldMachineState.Unknown,
            [feature]));
    }

    [Fact]
    public void ResponsesRejectAuthorityMismatchAndDuplicateObservations()
    {
        var loadedSummary = new WorldQuerySummary(
            WorldQueryAuthority.LoadedObserved,
            "minecraft:overworld",
            120,
            "z1",
            1,
            1,
            1,
            1,
            complete: true,
            truncated: false,
            []);
        var machine = new WorldMachineObservation(
            "minecraft:furnace",
            "Furnace",
            new WorldBlockPosition(1, 64, 2),
            3,
            "generic",
            WorldMachineCandidateDomain.BlockEntityOnly,
            WorldMachineState.Unknown);

        Assert.Throws<ArgumentException>(() => new WorldLocatedResponse(
            loadedSummary,
            "minecraft:village",
            "Village",
            100,
            -40,
            null,
            108,
            WorldLocatorRange.Standard,
            1));
        Assert.Throws<ArgumentException>(() => new WorldMachinesResponse(
            loadedSummary,
            WorldChunkRange.ThreeByThree,
            WorldVerticalRange.Ten,
            [machine, machine]));
        Assert.Throws<ArgumentException>(() => new WorldQuerySummary(
            WorldQueryAuthority.LoadedObserved,
            "minecraft:overworld",
            120,
            "z1",
            1,
            1,
            1,
            1,
            complete: true,
            truncated: false,
            ["DUPLICATE", "DUPLICATE"]));
    }

    [Fact]
    public void ContainerResponseIsMetadataOnlyEvenWhenLegacyContentIsSupplied()
    {
        var position = new WorldBlockPosition(1, 64, 2);
        var stack = new WorldContainerItem(2, "minecraft:iron_ingot", 4, "component-identity");
        var side = new WorldContainerSide("north", [stack]);
        var observation = new WorldContainerObservation(
            "minecraft:barrel",
            "Barrel",
            position,
            3,
            WorldContainerView.AutomationSided,
            [],
            [side]);

        var json = ProtocolJson.Serialize(observation);
        var restored = ProtocolJson.Deserialize<WorldContainerObservation>(json);

        Assert.Equal(WorldContainerView.AutomationSided, restored.View);
        Assert.Null(restored.Stacks);
        Assert.Null(restored.Sides);
        Assert.DoesNotContain("stacks", json, StringComparison.Ordinal);
        Assert.DoesNotContain("sides", json, StringComparison.Ordinal);
    }

    [Fact]
    public void ContainerContentTypesRemainBoundedButAreNotRetainedByObservations()
    {
        var stack = new WorldContainerItem(0, "minecraft:stone", 1);
        var observation = new WorldContainerObservation(
            "minecraft:chest",
            "Chest",
            new WorldBlockPosition(0, 64, 0),
            0,
            WorldContainerView.Locked,
            [stack]);
        Assert.Null(observation.Stacks);
        Assert.Throws<ArgumentException>(() => new WorldContainerSide("north", [stack, stack]));
    }

    private static void AssertJsonRoundTrip<T>(T value)
    {
        var json = ProtocolJson.Serialize(value);
        var restored = ProtocolJson.Deserialize<T>(json);
        Assert.Equal(
            JsonDocument.Parse(json).RootElement.ToString(),
            JsonDocument.Parse(ProtocolJson.Serialize(restored)).RootElement.ToString());
    }
}
