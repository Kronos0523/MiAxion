package dev.miaxion.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;

final class RecipeV11ContractTest {
    private static final ObjectMapper JSON = new ObjectMapper();

    @Test
    void recipeV11CarriesIdentityAndCompleteComponents() throws Exception {
        JsonNode components = JSON.readTree(
            "{\"minecraft:custom_name\":{\"text\":\"Example\"},"
                + "\"minecraft:enchantments\":{\"levels\":"
                + "{\"minecraft:sharpness\":3}}}");
        RecipeAlternativeDto alternative =
            new RecipeAlternativeDto("minecraft:stick", 1, components);
        RecipeIdentityDto identity = RecipeIdentityDto.derived(
            "sha256-canonical-v1",
            "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef");
        RecipeDto recipe = new RecipeDto(
            "miaxion:derived/0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef",
            "minecraft:crafting_shaped",
            "unknown",
            identity,
            List.of(new RecipeSlotDto(List.of(alternative))),
            List.of(),
            List.of());

        String wire = ProtocolJson.write(recipe);

        assertTrue(wire.contains("\"identity\""));
        assertTrue(wire.contains("\"components\""));
        assertTrue(wire.contains("\"minecraft:enchantments\""));
        assertEquals(
            components,
            ProtocolJson.read(wire, RecipeDto.class)
                .inputs().get(0).alternatives().get(0).components());
    }

    @Test
    void identityRejectsMalformedDerivedHashAndKind() {
        assertThrows(
            IllegalArgumentException.class,
            () -> RecipeIdentityDto.derived(
                "sha256-canonical-v1", "not-a-sha256"));
        assertThrows(
            IllegalArgumentException.class,
            () -> new RecipeIdentityDto("other", null, null));
    }

    @Test
    void componentKeysMustBeResourceLocations() throws Exception {
        JsonNode invalid = JSON.readTree("{\"not valid\":true}");

        assertThrows(
            IllegalArgumentException.class,
            () -> new RecipeAlternativeDto("minecraft:stick", 1, invalid));
    }

    @Test
    void recipeCapabilityVersionIsSeparateFromEnvelopeVersion() {
        assertEquals("1.0", ProtocolConstants.VERSION);
        assertEquals("1.9", ProtocolConstants.RECIPE_CAPABILITY_VERSION);
        assertEquals("1.3", ProtocolConstants.RECIPE_SOURCE_CAPABILITY_VERSION);
    }

    @Test
    void acquisitionClassificationRoundTripsAndCannotClaimDeterministicPlanning() throws Exception {
        RecipeClassificationDto classification = new RecipeClassificationDto(
            "loot", "Ancient Vault", "acquisition_only");
        RecipeDto recipe = new RecipeDto(
            "example:from_vault", "example:loot_table", "example",
            RecipeIdentityDto.registered(), List.of(), List.of(),
            List.of(new RecipeSlotDto(List.of(new RecipeAlternativeDto("example:artifact", 1)))),
            null, classification);

        RecipeDto restored = ProtocolJson.read(ProtocolJson.write(recipe), RecipeDto.class);

        assertEquals("loot", restored.classification().category());
        assertEquals("Ancient Vault", restored.classification().visibleCategoryName());
        assertEquals("acquisition_only", restored.classification().planningEligibility());
        assertThrows(IllegalArgumentException.class, () -> new RecipeClassificationDto(
            "drops", "", "deterministic"));
        assertThrows(IllegalArgumentException.class, () -> new RecipeClassificationDto(
            "other_unclassified", null, "deterministic"));
        assertThrows(IllegalArgumentException.class, () -> new RecipeClassificationDto(
            "loot", null, "future"));
    }

    @Test
    void recipeV15CarriesClosedOptionalProcessFacts() throws Exception {
        RecipeProcessDto process = new RecipeProcessDto(
            "minecraft:furnace", 200, "fuel_ticks", 200L);
        RecipeDto recipe = new RecipeDto(
            "minecraft:iron_ingot_from_smelting_iron_ore",
            "minecraft:smelting",
            "minecraft",
            RecipeIdentityDto.registered(),
            List.of(),
            List.of(),
            List.of(),
            process);

        RecipeDto roundTrip = ProtocolJson.read(ProtocolJson.write(recipe), RecipeDto.class);

        assertEquals("minecraft:furnace", roundTrip.process().machineFamily());
        assertEquals(200, roundTrip.process().durationTicks());
        assertThrows(IllegalArgumentException.class, () ->
            new RecipeProcessDto("minecraft:furnace", 200, "fe", null));
    }

    @Test
    void recipeV13AlternativeCapacityIsRaisedButStillBounded() {
        RecipeAlternativeDto alternative =
            new RecipeAlternativeDto("minecraft:stick", 1);

        assertEquals(
            ProtocolConstants.RECIPE_SLOT_ALTERNATIVES_MAX,
            new RecipeSlotDto(java.util.Collections.nCopies(
                ProtocolConstants.RECIPE_SLOT_ALTERNATIVES_MAX,
                alternative)).alternatives().size());
        assertThrows(
            IllegalArgumentException.class,
            () -> new RecipeSlotDto(java.util.Collections.nCopies(
                ProtocolConstants.RECIPE_SLOT_ALTERNATIVES_MAX + 1,
                alternative)));
    }

    @Test
    void recipeSourcePageSizeRoundTripsWithContinuation() throws Exception {
        RecipeResponse response = new RecipeResponse(
            List.of(), true, null, null, null, null, null, null,
            "0123456789abcdef0123456789abcdef", 32);

        RecipeResponse restored = ProtocolJson.read(
            ProtocolJson.write(response), RecipeResponse.class);

        assertEquals(32, restored.sourcePageSize());
        assertEquals(response.nextSourceCursor(), restored.nextSourceCursor());
    }

    @Test
    void recipeContinuationRequiresItsOriginatingPageSize() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new RecipeResponse(
                List.of(), true, null, null, null, null, null, null,
                "0123456789abcdef0123456789abcdef", null));
    }
}
