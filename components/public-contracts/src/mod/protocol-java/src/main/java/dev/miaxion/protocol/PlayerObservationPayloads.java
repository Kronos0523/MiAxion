package dev.miaxion.protocol;

import java.util.List;

/** Typed public, client-side observations. Names/text are data, never commands. */
public final class PlayerObservationPayloads {
    private PlayerObservationPayloads() {}
    public sealed interface Response permits InventoryView, PlayerStatus, WorldContext, ScreenSnapshot, TargetSnapshot {}
    public record Request(@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL) String detailsSlot) {}
    public record Enchant(String id, String displayName, Integer level, boolean stored) {}
    public record Item(String itemId, String displayName, Integer count, Integer damage, Integer maxDamage, String durabilityStatus, List<Enchant> enchantments, String detailsStatus) {
        public Item {
            enchantments = List.copyOf(enchantments);
        }
    }
    public record Slot(String slotId, String source, String area, Integer index, Integer legacySlot, Integer containerSlot, String status, Item item, String failureCode) {}
    public record Equipment(String location, String slotId, String status) {}
    public record Details(String slotId, String status, List<String> tooltipLines) {
        public Details {
            tooltipLines = List.copyOf(tooltipLines);
        }
    }
    public record BlockPosition(Integer x, Integer y, Integer z) {}
    public record Position(Double x, Double y, Double z) {}
    public record Effect(String id, String displayName, Integer level, Integer durationTicks, Boolean infinite) {}
    public record Property(String name, String value) {}
    public record ScreenText(String source, String elementId, String kind, String text, Integer x, Integer y, Boolean visible, Boolean active, List<String> tooltipLines) {
        public ScreenText {
            tooltipLines = List.copyOf(tooltipLines);
        }
    }
    public record InventoryView(String capturedAt, String status, List<String> sources, Integer selectedSlot, List<Slot> slots, List<Equipment> equipment, Details details) implements Response {
        public InventoryView {
            sources = List.copyOf(sources);
            slots = List.copyOf(slots);
            equipment = List.copyOf(equipment);
        }
    }
    public record PlayerStatus(String capturedAt, String status, PlayerSnapshotResponse player, List<Effect> effects, Double armor, Double toughness, Double experienceProgress, Double saturation, Integer air, Integer maxAir, List<String> unknownFields) implements Response {
        public PlayerStatus {
            effects = List.copyOf(effects);
            unknownFields = List.copyOf(unknownFields);
        }
    }
    public record WorldContext(String capturedAt, String status, String dimensionId, Position position, BlockPosition blockPosition, BlockPosition chunkPosition, BlockPosition relativePosition, String direction, Double yaw, Double pitch, String biomeId, Integer skyLight, Integer blockLight, Integer combinedLight, Long gameTime, Long dayTime, Boolean daylight, String weather, List<String> unknownFields) implements Response {
        public WorldContext {
            unknownFields = List.copyOf(unknownFields);
        }
    }
    public record ScreenSnapshot(String capturedAt, String status, String title, String screenType, Integer menuId, List<Slot> slots, Item carried, List<ScreenText> elements, Details details, List<String> coverageNotes) implements Response {
        public ScreenSnapshot {
            slots = List.copyOf(slots);
            elements = List.copyOf(elements);
            coverageNotes = List.copyOf(coverageNotes);
        }
    }
    public record TargetSnapshot(String capturedAt, String status, String kind, String registryId, String displayName, BlockPosition blockPosition, Integer entityId, List<Property> properties) implements Response {
        public TargetSnapshot {
            properties = List.copyOf(properties);
        }
    }
}
