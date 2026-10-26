package dev.miaxion.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

final class ProtocolDtoRoundTripTest {
    private static final Path ROOT =
        Path.of("..", "..", "..").toAbsolutePath().normalize();
    private static final ObjectMapper TREE_MAPPER = new ObjectMapper();
    private static final UUID REQUEST_ID =
        UUID.fromString("b8b88ff4-36c0-4bdc-a5c1-8a976dc7d2cf");
    private static final UUID TARGET_REQUEST_ID =
        UUID.fromString("9c84c889-4766-412d-8c5a-235dd7d52cb4");
    private static final OffsetDateTime TIMESTAMP =
        OffsetDateTime.parse("2026-07-26T05:30:00Z");
    private static final OffsetDateTime DEADLINE =
        OffsetDateTime.parse("2026-07-26T05:30:03Z");
    private static final String TOKEN =
        "QmFzZTY0dXJsLXNlc3Npb24tdG9rZW4tdjEtMzItYnl0ZXM";
    private static final String UNICODE_HUD_TEXT =
        "中文语音助手🙂 §a绿色 \"引号\" \\反斜杠\n换行";

    @Test
    void dense_scan_progress_round_trips_without_expanding_the_result_page() throws Exception {
        var summary = new WorldQuerySummary(WorldQueryAuthority.LOADED_OBSERVED,
            "minecraft:overworld", 1, "dense-fixture", 25, 25, 25_600, 0,
            false, true, List.of("RESULT_LIMIT_REACHED"));
        var response = new WorldMachinesResponse(summary, WorldChunkRange.FIVE_BY_FIVE,
            WorldVerticalRange.FIFTY, List.of());
        var restored = ProtocolJson.read(ProtocolJson.write(response), WorldMachinesResponse.class);
        assertEquals(25_600, restored.summary().examined());
        assertTrue(restored.machines().isEmpty());
        assertThrows(IllegalArgumentException.class, () -> new WorldQuerySummary(
            WorldQueryAuthority.LOADED_OBSERVED, "minecraft:overworld", 1, "dense-fixture",
            25, 25, -1, 0, false, true, List.of("RESULT_LIMIT_REACHED")));
    }

    static Stream<Arguments> contracts() throws Exception {
        RecipeSlotDto planksSlot = new RecipeSlotDto(List.of(
            new RecipeAlternativeDto("minecraft:oak_planks", 2),
            new RecipeAlternativeDto("minecraft:spruce_planks", 3)));
        RecipeSlotDto repeatedStickSlot = new RecipeSlotDto(List.of(
            new RecipeAlternativeDto("minecraft:stick", 1)));
        RecipeSlotDto catalystSlot = new RecipeSlotDto(List.of(
            new RecipeAlternativeDto("minecraft:crafting_table", 1)));
        RecipeSlotDto outputSlot = new RecipeSlotDto(List.of(
            new RecipeAlternativeDto("minecraft:diamond_pickaxe", 1)));
        RecipeDto recipe = new RecipeDto(
            "minecraft:diamond_pickaxe",
            "minecraft:crafting_shaped",
            "minecraft",
            List.of(planksSlot, repeatedStickSlot, repeatedStickSlot),
            List.of(catalystSlot),
            List.of(outputSlot));
        RecipeResponse recipeResponse =
            new RecipeResponse(
                List.of(recipe), false, true, true, 1,
                "recommended-direct", "inventory-revision-1",
                List.of(recipe.recipeId()));
        InventorySlot slot = new InventorySlot(
            1,
            false,
            "minecraft:diamond",
            3);
        Position position = new Position(0, 64, 0);
        JsonNode requestPayload = TREE_MAPPER.readTree(
            "{\"nonce\":\"fixed-nonce\"}");
        JsonNode responsePayload = TREE_MAPPER.readTree(
            """
            {
              "nonce":"fixed-nonce",
              "gameReady":false,
              "protocolVersion":"1.0"
            }
            """);
        WorldQuerySummary generatorSummary = new WorldQuerySummary(
            WorldQueryAuthority.GENERATOR_LOCATED,
            "minecraft:overworld",
            120,
            "z1",
            1,
            0,
            1,
            1,
            true,
            false,
            List.of());
        WorldQuerySummary loadedSummary = new WorldQuerySummary(
            WorldQueryAuthority.LOADED_OBSERVED,
            "minecraft:overworld",
            120,
            "z1",
            1,
            1,
            1,
            1,
            true,
            false,
            List.of());
        WorldBlockPosition blockPosition = new WorldBlockPosition(1, 64, 2);
        WorldMachineObservation machine = new WorldMachineObservation(
            "minecraft:furnace", "Furnace", blockPosition, 3, "generic",
            WorldMachineCandidateDomain.BLOCK_ENTITY_ONLY, WorldMachineState.UNKNOWN,
            List.of(new WorldMachineFeature("energy", "fe", 40, 100L, null)));
        WorldContainerObservation container = new WorldContainerObservation(
            "minecraft:chest", "Chest", blockPosition, 3, WorldContainerView.FULL);
        WorldContainerStack containerStack = new WorldContainerStack(
            2, "minecraft:iron_ingot", 4, "component-identity");
        WorldContainerSide containerSide = new WorldContainerSide(
            "north", List.of(containerStack));
        WorldContainerObservation sidedContainer = new WorldContainerObservation(
            "minecraft:barrel", "Barrel", blockPosition, 3,
            WorldContainerView.AUTOMATION_SIDED, List.of(), List.of(containerSide));
        WorldTargetObservation target = new WorldTargetObservation(
            "minecraft:gold_block", "Block of Gold", blockPosition, 3,
            WorldQueryTargetKind.METAL_STORAGE_BLOCK, 10);
        WorldRegistryMatch registryMatch = new WorldRegistryMatch(
            WorldRegistryKind.STRUCTURE,
            "minecraft:ancient_city",
            "structure.minecraft.ancient_city",
            "Ancient City");
        WorldRegistryReference missingReference = new WorldRegistryReference(
            WorldRegistryKind.BIOME, "minecraft:plains");

        return Stream.of(
            Arguments.of(
                new SessionPairRequest("MiAxion Desktop", "1.0.0"),
                SessionPairRequest.class),
            Arguments.of(
                new SessionPairApproved(TOKEN, "Bearer"),
                SessionPairApproved.class),
            Arguments.of(
                new SessionRevokeRequest(
                    SessionRevokeReason.USER_REQUESTED),
                SessionRevokeRequest.class),
            Arguments.of(
                new SessionRevokeResponse(true),
                SessionRevokeResponse.class),
            Arguments.of(
                new RequestCancelRequest(TARGET_REQUEST_ID),
                RequestCancelRequest.class),
            Arguments.of(
                new RequestCancelResponse(
                    TARGET_REQUEST_ID,
                    true,
                    CancelState.CANCELLED),
                RequestCancelResponse.class),
            Arguments.of(
                new CapabilitiesListRequest(),
                CapabilitiesListRequest.class),
            Arguments.of(
                new CapabilityDescriptor(
                    "health.ping",
                    ProtocolConstants.VERSION,
                    false),
                CapabilityDescriptor.class),
            Arguments.of(
                new CapabilitiesListResponse(
                    List.of(new CapabilityDescriptor(
                        "health.ping",
                        ProtocolConstants.VERSION,
                        false))),
                CapabilitiesListResponse.class),
            Arguments.of(new RecipeProvidersListRequest(), RecipeProvidersListRequest.class),
            Arguments.of(new RecipeSourceRequest("miaxion_rei", 4, "minecraft:stick", null,
                "item", null, null, null), RecipeSourceRequest.class),
            Arguments.of(new RecipeProvidersListResponse(List.of(
                new RecipeProvidersListResponse.Provider("miaxion_rei", "0.1.0", "roughlyenoughitems",
                    new RecipeProvidersListResponse.Readiness("READY", "READY"),
                    new RecipeProvidersListResponse.Readiness("GAME_NOT_READY", "RUNTIME_PENDING")))),
                RecipeProvidersListResponse.class),
            Arguments.of(
                new RecipeQueryRequest("minecraft:diamond"),
                RecipeQueryRequest.class),
            Arguments.of(
                new RecipeUsesRequest("minecraft:diamond"),
                RecipeUsesRequest.class),
            Arguments.of(
                RecipeSelectionRequest.recommendedDirect(),
                RecipeSelectionRequest.class),
            Arguments.of(
                RecipeSelectionRequest.recommendedProcessFamilies(),
                RecipeSelectionRequest.class),
            Arguments.of(
                RecipeSelectionRequest.recommendedDirect("railcraft:rolling/steel_rail"),
                RecipeSelectionRequest.class),
            Arguments.of(
                new RecipeSelectionRequest("planning-direct", 3, "minecraft:stick"),
                RecipeSelectionRequest.class),
            Arguments.of(
                planksSlot.alternatives().get(0),
                RecipeAlternativeDto.class),
            Arguments.of(planksSlot, RecipeSlotDto.class),
            Arguments.of(recipe, RecipeDto.class),
            Arguments.of(recipeResponse, RecipeResponse.class),
            Arguments.of(
                new InventorySnapshotRequest(true),
                InventorySnapshotRequest.class),
            Arguments.of(slot, InventorySlot.class),
            Arguments.of(
                new InventorySnapshotResponse(
                    "inventory-revision-1",
                    List.of(slot)),
                InventorySnapshotResponse.class),
            Arguments.of(
                new PlayerSnapshotRequest(),
                PlayerSnapshotRequest.class),
            Arguments.of(position, Position.class),
            Arguments.of(
                new PlayerSnapshotResponse(
                    "minecraft:overworld",
                    position,
                    20,
                    20,
                    0,
                    GameMode.SURVIVAL),
                PlayerSnapshotResponse.class),
            Arguments.of(
                new EntityResolveRequest("动力臂怎么用", List.of("create")),
                EntityResolveRequest.class),
            Arguments.of(
                new EntityResolveMatch(
                    "item", "create:deployer", "block.create.deployer", "动力臂"),
                EntityResolveMatch.class),
            Arguments.of(
                new EntityResolveResponse(
                    "r1",
                    List.of(new EntityResolveMatch(
                        "item", "create:deployer", "block.create.deployer", "动力臂")),
                    false),
                EntityResolveResponse.class),
            Arguments.of(
                new EntityReference("item", "create:deployer"),
                EntityReference.class),
            Arguments.of(
                new EntityDescribeRequest(List.of(
                    new EntityReference("item", "create:deployer"),
                    new EntityReference("block", "minecraft:crafting_table"))),
                EntityDescribeRequest.class),
            Arguments.of(
                new EntityDescription(
                    "item", "create:deployer", "block.create.deployer", "动力臂"),
                EntityDescription.class),
            Arguments.of(
                new EntityDescribeResponse(
                    "r1",
                    List.of(new EntityDescription(
                        "item", "create:deployer", "block.create.deployer", "动力臂")),
                    List.of(new EntityReference("block", "minecraft:crafting_table"))),
                EntityDescribeResponse.class),
            Arguments.of(
                new WorldLocateRequest(
                    "minecraft:village", WorldLocatorRange.STANDARD),
                WorldLocateRequest.class),
            Arguments.of(
                new WorldNearbyScanRequest(),
                WorldNearbyScanRequest.class),
            Arguments.of(new WorldScanWindow(7, 30), WorldScanWindow.class),
            Arguments.of(new WorldNearbyScanRequest(WorldChunkRange.THREE_BY_THREE,
                WorldVerticalRange.TEN, new WorldScanWindow(7, 30), ""), WorldNearbyScanRequest.class),
            Arguments.of(new WorldNearbyScanRequest(WorldChunkRange.THREE_BY_THREE,
                WorldVerticalRange.TEN, new WorldScanWindow(7, 30), "0123456789abcdef0123456789abcdef"), WorldNearbyScanRequest.class),
            Arguments.of(new WorldNearbyScanRequest(WorldChunkRange.THREE_BY_THREE,
                WorldVerticalRange.TEN, new WorldScanWindow(7, 30)), WorldNearbyScanRequest.class),
            Arguments.of(new WorldMachinesResponse(loadedSummary, WorldChunkRange.THREE_BY_THREE,
                WorldVerticalRange.TEN, List.of(machine), new WorldScanWindow(7, 30)), WorldMachinesResponse.class),
            Arguments.of(new WorldContainersResponse(loadedSummary, WorldChunkRange.THREE_BY_THREE,
                WorldVerticalRange.TEN, List.of(container), new WorldScanWindow(7, 30)), WorldContainersResponse.class),
            Arguments.of(new WorldMachinesResponse(new WorldQuerySummary(WorldQueryAuthority.LOADED_OBSERVED,
                "minecraft:overworld", 0, "q1", 1, 1, 129, 0, false, true, List.of()),
                WorldChunkRange.THREE_BY_THREE, WorldVerticalRange.TEN, List.of(), null,
                "0123456789abcdef0123456789abcdef"), WorldMachinesResponse.class),
            Arguments.of(new WorldContainersResponse(new WorldQuerySummary(WorldQueryAuthority.LOADED_OBSERVED,
                "minecraft:overworld", 0, "q1", 1, 1, 129, 0, false, true, List.of()),
                WorldChunkRange.THREE_BY_THREE, WorldVerticalRange.TEN, List.of(), null,
                "0123456789abcdef0123456789abcdef"), WorldContainersResponse.class),
            Arguments.of(
                new WorldStructureTargetsRequest(
                    "minecraft:ancient_city", "minecraft:gold_block"),
                WorldStructureTargetsRequest.class),
            Arguments.of(
                new WorldRegistryResolveRequest(
                    WorldRegistryKind.STRUCTURE, "Ancient City"),
                WorldRegistryResolveRequest.class),
            Arguments.of(
                new WorldRegistryDescribeRequest(
                    List.of(new WorldRegistryReference(
                        WorldRegistryKind.STRUCTURE, "minecraft:ancient_city"))),
                WorldRegistryDescribeRequest.class),
            Arguments.of(generatorSummary, WorldQuerySummary.class),
            Arguments.of(blockPosition, WorldBlockPosition.class),
            Arguments.of(
                new WorldLocatedResponse(
                    generatorSummary, "minecraft:village", "Village", 100, -40,
                    null, 108, WorldLocatorRange.STANDARD, 1),
                WorldLocatedResponse.class),
            Arguments.of(machine, WorldMachineObservation.class),
            Arguments.of(
                new WorldMachineFeature("energy", "fe", 40, 100L, null),
                WorldMachineFeature.class),
            Arguments.of(container, WorldContainerObservation.class),
            Arguments.of(containerStack, WorldContainerStack.class),
            Arguments.of(containerSide, WorldContainerSide.class),
            Arguments.of(sidedContainer, WorldContainerObservation.class),
            Arguments.of(target, WorldTargetObservation.class),
            Arguments.of(registryMatch, WorldRegistryMatch.class),
            Arguments.of(
                new WorldMachinesResponse(
                    loadedSummary,
                    WorldChunkRange.THREE_BY_THREE,
                    WorldVerticalRange.TEN,
                    List.of(machine)),
                WorldMachinesResponse.class),
            Arguments.of(
                new WorldContainersResponse(
                    loadedSummary,
                    WorldChunkRange.ONE_BY_ONE,
                    WorldVerticalRange.FIFTY,
                    List.of(container, sidedContainer)),
                WorldContainersResponse.class),
            Arguments.of(
                new WorldStructureTargetsResponse(
                    loadedSummary,
                    "minecraft:ancient_city",
                    "structure_covered_chunks",
                    List.of(target)),
                WorldStructureTargetsResponse.class),
            Arguments.of(
                new WorldRegistryResolveResponse(
                    "z1", List.of(registryMatch), false),
                WorldRegistryResolveResponse.class),
            Arguments.of(
                new WorldRegistryDescribeResponse(
                    "z1", List.of(registryMatch), List.of(missingReference)),
                WorldRegistryDescribeResponse.class),
            Arguments.of(
                new QuestQueryRequest(QuestQueryScope.ACTIVE),
                QuestQueryRequest.class),
            Arguments.of(
                new QuestDetailRequest("0123456789abcdef"),
                QuestDetailRequest.class),
            Arguments.of(
                new QuestQueryEntry(
                    "0123456789abcdef", "Getting Started", "Make Power",
                    QuestState.ACTIVE, 0.5, 2, 1, false),
                QuestQueryEntry.class),
            Arguments.of(
                new QuestQueryResponse(
                    List.of(new QuestQueryEntry(
                        "0123456789abcdef", "Getting Started", "Make Power",
                        QuestState.ACTIVE, 0.5, 2, 1, false)),
                    1,
                    false),
                QuestQueryResponse.class),
            Arguments.of(
                new QuestObjective(
                    "task-1", "ftbquests:item", "Craft a shaft",
                    1, 1, "1 / 1", true, false),
                QuestObjective.class),
            Arguments.of(
                new QuestReward(
                    "reward-1", "ftbquests:item", "8 Andesite Alloy",
                    false, false, QuestRewardClaimState.CLAIMABLE),
                QuestReward.class),
            Arguments.of(
                new QuestDependency("parent-1", "Welcome"),
                QuestDependency.class),
            Arguments.of(
                new QuestDetailResponse(
                    "0123456789abcdef", "Getting Started", "Make Power", "",
                    QuestDescriptionVisibility.VISIBLE,
                    List.of("Build a water wheel.", "Connect it with shafts."),
                    QuestState.ACTIVE, 0.5, false, false,
                    List.of(new QuestObjective(
                        "task-1", "ftbquests:item", "Craft a shaft",
                        1, 1, "1 / 1", true, false)),
                    List.of(new QuestReward(
                        "reward-1", "ftbquests:item", "8 Andesite Alloy",
                        false, false, QuestRewardClaimState.CLAIMABLE)),
                    List.of(new QuestDependency("parent-1", "Welcome"))),
                QuestDetailResponse.class),
            Arguments.of(
                new HudShowRequest(
                    UNICODE_HUD_TEXT,
                    HudSeverity.INFO,
                    3000),
                HudShowRequest.class),
            Arguments.of(
                new HudShowResponse(true),
                HudShowResponse.class),
            Arguments.of(
                new HealthPingRequest("fixed-nonce"),
                HealthPingRequest.class),
            Arguments.of(
                new HealthPingResponse(
                    "fixed-nonce",
                    false,
                    ProtocolConstants.VERSION),
                HealthPingResponse.class),
            Arguments.of(
                new ProtocolError(
                    ProtocolErrorCode.INVALID_REQUEST,
                    "request was invalid",
                    false,
                    null),
                ProtocolError.class),
            Arguments.of(
                new RequestEnvelope(
                    ProtocolConstants.VERSION,
                    REQUEST_ID,
                    TIMESTAMP,
                    DEADLINE,
                    KnownProtocolAction.HEALTH_PING.action(),
                    TOKEN,
                    requestPayload,
                    null),
                RequestEnvelope.class),
            Arguments.of(
                new ResponseEnvelope(
                    ProtocolConstants.VERSION,
                    REQUEST_ID,
                    TIMESTAMP,
                    DEADLINE,
                    KnownProtocolAction.HEALTH_PING.action(),
                    null,
                    responsePayload,
                    null),
                ResponseEnvelope.class),
            Arguments.of(
                new ResponseEnvelope(
                    ProtocolConstants.VERSION,
                    REQUEST_ID,
                    TIMESTAMP,
                    DEADLINE,
                    KnownProtocolAction.HEALTH_PING.action(),
                    null,
                    null,
                    new ProtocolError(
                        ProtocolErrorCode.INTERNAL_ERROR,
                        "internal bridge error",
                        true,
                        null)),
                ResponseEnvelope.class));
    }

    @ParameterizedTest
    @MethodSource("contracts")
    @SuppressWarnings("unchecked")
    void every_contract_family_round_trips(
        Object value,
        Class<?> type
    ) throws Exception {
        String first = ProtocolJson.write(value);
        Object restored = ProtocolJson.read(
            first,
            (Class<Object>) type);

        assertEquals(
            TREE_MAPPER.readTree(first),
            TREE_MAPPER.readTree(ProtocolJson.write(restored)));
    }

    @Test
    void shared_unicode_fixture_round_trips_and_revalidates()
        throws Exception
    {
        String fixtureJson = Files.readString(
            ROOT.resolve("protocol/fixtures/valid/hud-request.json"),
            StandardCharsets.UTF_8);
        RequestEnvelope request = ProtocolJson.read(
            fixtureJson,
            RequestEnvelope.class);
        HudShowRequest payload = ProtocolJson.read(
            request.payload().toString(),
            HudShowRequest.class);

        assertEquals(UNICODE_HUD_TEXT, payload.text());

        String serialized = ProtocolJson.write(request);
        assertEquals(
            TREE_MAPPER.readTree(fixtureJson),
            TREE_MAPPER.readTree(serialized));
        assertTrue(
            ProtocolCorpusTest.validationMessages(
                "request.schema.json",
                serialized).isEmpty());
        assertTrue(
            ProtocolCorpusTest.requestTimeIsStrictlyOrdered(
                TREE_MAPPER.readTree(serialized)));
    }

    @Test
    void approved_collection_records_are_defensively_immutable() {
        List<CapabilityDescriptor> capabilities = new ArrayList<>();
        List<RecipeAlternativeDto> alternatives = new ArrayList<>();
        alternatives.add(
            new RecipeAlternativeDto("minecraft:diamond", 1));
        List<RecipeSlotDto> inputs = new ArrayList<>();
        inputs.add(new RecipeSlotDto(alternatives));
        List<RecipeSlotDto> catalysts = new ArrayList<>();
        catalysts.add(new RecipeSlotDto(List.of(
            new RecipeAlternativeDto("minecraft:crafting_table", 1))));
        List<RecipeSlotDto> outputs = new ArrayList<>();
        outputs.add(new RecipeSlotDto(List.of(
            new RecipeAlternativeDto("minecraft:diamond", 1))));
        List<RecipeDto> recipes = new ArrayList<>();
        RecipeDto recipe = new RecipeDto(
            "minecraft:test",
            "minecraft:crafting",
            "minecraft",
            inputs,
            catalysts,
            outputs);
        recipes.add(recipe);
        List<InventorySlot> slots = new ArrayList<>();

        CapabilitiesListResponse capabilitiesResponse =
            new CapabilitiesListResponse(capabilities);
        RecipeSlotDto ingredient =
            new RecipeSlotDto(alternatives);
        RecipeDto copiedRecipe = new RecipeDto(
            "minecraft:test",
            "minecraft:crafting",
            "minecraft",
            inputs,
            catalysts,
            outputs);
        RecipeResponse response =
            new RecipeResponse(recipes, true);
        InventorySnapshotResponse inventoryResponse =
            new InventorySnapshotResponse("revision", slots);

        capabilities.add(new CapabilityDescriptor(
            "health.ping",
            ProtocolConstants.VERSION,
            false));
        alternatives.add(
            new RecipeAlternativeDto("minecraft:emerald", 1));
        inputs.clear();
        catalysts.clear();
        outputs.clear();
        recipes.clear();
        slots.add(new InventorySlot(0, true, null, null));

        assertTrue(capabilitiesResponse.capabilities().isEmpty());
        assertEquals(1, ingredient.alternatives().size());
        assertEquals(1, copiedRecipe.inputs().size());
        assertEquals(1, copiedRecipe.catalysts().size());
        assertEquals(1, copiedRecipe.outputs().size());
        assertEquals(1, response.recipes().size());
        assertTrue(response.truncated());
        assertTrue(inventoryResponse.slots().isEmpty());

        assertThrows(
            UnsupportedOperationException.class,
            () -> capabilitiesResponse.capabilities().clear());
        assertThrows(
            UnsupportedOperationException.class,
            () -> ingredient.alternatives().clear());
        assertThrows(
            UnsupportedOperationException.class,
            () -> copiedRecipe.inputs().clear());
        assertThrows(
            UnsupportedOperationException.class,
            () -> copiedRecipe.outputs().clear());
        assertThrows(
            UnsupportedOperationException.class,
            () -> response.recipes().clear());
        assertThrows(
            UnsupportedOperationException.class,
            () -> inventoryResponse.slots().clear());
    }

    @Test
    void public_type_inventory_is_exact() throws Exception {
        Set<String> expected = Set.of(
            "CancelState",
            "CapabilitiesListRequest",
            "CapabilitiesListResponse",
            "CapabilityDescriptor",
            "GameMode",
            "HealthPingRequest",
            "HealthPingResponse",
            "HudSeverity",
            "HudShowRequest",
            "HudShowResponse",
            "InventorySlot",
            "InventorySnapshotRequest",
            "InventorySnapshotResponse",
            "KnownProtocolAction",
            "ModVersionsResolveRequest",
            "ModVersionsResolveResponse",
            "ExtensionListRequest",
            "ExtensionCallRequest",
            "ExtensionDataSchema",
            "WorldBlockPosition",
            "WorldChunkRange",
            "WorldContainerObservation",
            "WorldContainerSide",
            "WorldContainerStack",
            "WorldContainersResponse",
            "WorldContainerView",
            "WorldLocatedResponse",
            "WorldLocateRequest",
            "WorldLocatorRange",
            "WorldMachineCandidateDomain",
            "WorldMachineFeature",
            "WorldMachineState",
            "WorldMachineObservation",
            "WorldMachinesResponse",
            "WorldNearbyScanRequest",
            "WorldScanWindow",
            "WorldQueryAuthority",
            "WorldQueryProgressLane",
            "WorldQueryProgressRequest",
            "WorldQueryProgressResponse",
            "WorldQuerySummary",
            "WorldQueryTargetKind",
            "WorldRegistryDescribeRequest",
            "WorldRegistryDescribeResponse",
            "WorldRegistryKind",
            "WorldRegistryMatch",
            "WorldRegistryReference",
            "WorldRegistrySearchRequest",
            "WorldRegistrySearchResponse",
            "WorldRegistryResolveRequest",
            "WorldRegistryResolveResponse",
            "WorldStructureTargetsResponse",
            "WorldStructureTargetsRequest",
            "WorldTargetObservation",
            "WorldVerticalRange",
            "EntityDescription",
            "EntityDescribeRequest",
            "EntityDescribeResponse",
            "EntityReference",
            "KnownRequest",
            "EntityResolveMatch",
            "EntityResolveRequest",
            "EntityResolveResponse",
            "PlayerObservationPayloads",
            "PlayerSnapshotRequest",
            "PlayerSnapshotResponse",
            "Position",
            "ProtocolAction",
            "ProtocolConstants",
            "ProtocolError",
            "ProtocolErrorCode",
            "ProtocolValidationFailure",
            "ProtocolValidator",
            "ProtocolRuntimeLimits",
            "ProtocolRuntimeLimitsLoader",
            "ProtocolActionBudget",
            "QuestDependency",
            "QuestDescriptionVisibility",
            "QuestDetailRequest",
            "QuestDetailResponse",
            "QuestObjective",
            "QuestQueryEntry",
            "QuestQueryRequest",
            "QuestQueryResponse",
            "QuestQueryScope",
            "QuestReward",
            "QuestRewardClaimState",
            "QuestState",
            "RecipeAlternativeDto",
            "RecipeResourceDto",
            "RecipeClassificationDto",
            "RecipeDto",
            "RecipeIdentityDto",
            "RecipeProcessDto",
            "RecipeProvidersListRequest",
            "RecipeProvidersListResponse",
            "RecipeQueryRequest",
            "RecipeSourceRequest",
            "RecipeResponse",
            "RecipeSelectionRequest",
            "RecipeSlotDto",
            "RecipeUsesRequest",
            "RequestCancelRequest",
            "RequestCancelResponse",
            "RequestEnvelope",
            "ReceiveStamp",
            "ResponseEnvelope",
            "SessionPairApproved",
            "SessionPairRequest",
            "SessionRevokeReason",
            "SessionRevokeRequest",
            "SessionRevokeResponse",
            "CommonRequest",
            "SchemaRegistry",
            "TerminologyPayloads",
            "KeybindingPayloads",
            "GameActionPayloads",
            "ActionConfigurationPayloads");
        Path classDirectory = Path.of(
            ProtocolAction.class
                .getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .toURI()).resolve(Path.of(
            "dev",
            "miaxion",
            "protocol")).toAbsolutePath().normalize();

        Set<String> actual;
        try (Stream<Path> classFiles = Files.list(classDirectory)) {
            actual = classFiles
                .map(path -> path.getFileName().toString())
                .filter(name ->
                    name.endsWith(".class") && !name.contains("$"))
                .map(name ->
                    name.substring(0, name.length() - ".class".length()))
                .filter(name -> {
                    try {
                        return Modifier.isPublic(Class.forName(
                            "dev.miaxion.protocol." + name)
                            .getModifiers());
                    } catch (ClassNotFoundException exception) {
                        throw new IllegalStateException(exception);
                    }
                })
                .collect(Collectors.toUnmodifiableSet());
        }

        assertEquals(expected, actual);
        assertEquals(
            Set.of(
                "VERSION",
            "RECIPE_CAPABILITY_VERSION",
            "RECIPE_SOURCE_CAPABILITY_VERSION",
            "RECIPE_SOURCE_PAGE_DEFAULT",
            "RECIPE_SOURCE_PAGE_MAX",
            "RECIPE_SOURCE_CURSOR_MAX",
                "QUEST_CAPABILITY_VERSION",
                "ENTITY_RESOLVE_CAPABILITY_VERSION",
                "ENTITY_DESCRIBE_CAPABILITY_VERSION",
                "MOD_VERSIONS_RESOLVE_CAPABILITY_VERSION",
                "QUEST_QUERY_RESULT_MAX",
                "QUEST_DETAIL_COLLECTION_MAX",
                "QUEST_DESCRIPTION_LINE_SCALARS_MAX",
                "QUEST_TITLE_SCALARS_MAX",
                "QUEST_PROGRESS_INTEGER_MAX",
                "RECIPE_SLOT_ALTERNATIVES_MAX",
                "RECIPE_INPUT_SLOTS_MAX",
                "RECIPE_CATALYST_SLOTS_MAX",
                "RECIPE_OUTPUT_SLOTS_MAX",
                "WORLD_QUERY_RESULT_MAX",
                "WORLD_QUERY_MACHINE_FEATURE_MAX",
                "WORLD_QUERY_CONTAINER_STACK_MAX",
                "WORLD_QUERY_NAME_CANDIDATE_MAX",
                "WORLD_QUERY_WARNING_MAX",
                "WORLD_NEARBY_CAPABILITY_VERSION",
                "WORLD_STRUCTURE_TARGETS_CAPABILITY_VERSION",
                "WORLD_QUERY_STRUCTURE_PIECE_MAX"),
            Stream.of(ProtocolConstants.class.getFields())
                .map(field -> field.getName())
                .collect(Collectors.toUnmodifiableSet()));
        assertFalse(Modifier.isPublic(ProtocolJson.class.getModifiers()));
    }
}
