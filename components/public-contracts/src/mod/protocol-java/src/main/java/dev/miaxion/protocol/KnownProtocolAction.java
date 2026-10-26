package dev.miaxion.protocol;

import java.util.Arrays;
import java.util.Optional;

public enum KnownProtocolAction {
    SESSION_PAIR_REQUEST("session.pair.request"),
    SESSION_PAIR_APPROVED("session.pair.approved"),
    SESSION_REVOKE("session.revoke"),
    REQUEST_CANCEL("request.cancel"),
    CAPABILITIES_LIST("capabilities.list"),
    EXTENSIONS_LIST("extensions.list"),
    EXTENSIONS_CALL("extensions.call"),
    RECIPE_QUERY("recipe.query"),
    RECIPE_USES("recipe.uses"),
    RECIPE_QUERY_SOURCE("recipe.query.source"),
    RECIPE_USES_SOURCE("recipe.uses.source"),
    RECIPE_PROVIDERS_LIST("recipe.providers.list"),
    QUESTS_QUERY("quests.query"),
    QUESTS_DETAIL("quests.detail"),
    INVENTORY_VIEW("inventory.view"),
    PLAYER_STATUS("player.status"),
    WORLD_CONTEXT("world.context"),
    SCREEN_SNAPSHOT("screen.snapshot"),
    TARGET_SNAPSHOT("target.snapshot"),
    INVENTORY_SNAPSHOT("inventory.snapshot"),
    TERMINOLOGY_CONFIGURE("terminology.configure"),
    TERMINOLOGY_SNAPSHOT("terminology.snapshot"),
    TERMINOLOGY_CLEAR("terminology.clear"),
    TERMINOLOGY_CHANGED("terminology.changed"),
    TERMINOLOGY_V2_CONFIGURE("terminology.v2.configure"),
    TERMINOLOGY_V2_CHANGED("terminology.v2.changed"),
    PLAYER_SNAPSHOT("player.snapshot"),
    ENTITY_RESOLVE("entities.resolve"),
    ENTITY_DESCRIBE("entities.describe"),
    MOD_VERSIONS_RESOLVE("mods.versions.resolve"),
    WORLD_STRUCTURE_LOCATE("world.structure.locate"),
    WORLD_BIOME_LOCATE("world.biome.locate"),
    WORLD_MACHINES_SCAN("world.machines.scan"),
    WORLD_CONTAINERS_SCAN("world.containers.scan"),
    WORLD_STRUCTURE_TARGETS("world.structure.targets"),
    WORLD_REGISTRY_SEARCH("world.registry.search"),
    WORLD_REGISTRY_RESOLVE("world.registry.resolve"),
    WORLD_REGISTRY_DESCRIBE("world.registry.describe"),
    WORLD_QUERY_PROGRESS("world.query.progress"),
    HUD_SHOW("hud.show"),
    HEALTH_PING("health.ping"),
    KEYBINDINGS_CATALOG("keybindings.catalog"),
    KEYBINDINGS_PREVIEW("keybindings.preview"),
    KEYBINDINGS_APPLY("keybindings.apply"),
    KEYBINDINGS_ROLLBACK("keybindings.rollback"),
    ACTIONS_CONTEXT("actions.context"),
    ACTIONS_CATALOG("actions.catalog"),
    ACTIONS_CONFIGURATION("actions.configuration"),
    ACTIONS_GRANT("actions.grant"),
    ACTIONS_REVOKE("actions.revoke"),
    ACTIONS_EXECUTE("actions.execute"),
    ACTIONS_STATUS("actions.status");

    private final String wireValue;

    KnownProtocolAction(String wireValue) {
        this.wireValue = wireValue;
    }

    public String wireValue() {
        return wireValue;
    }

    public ProtocolAction action() {
        return new ProtocolAction(wireValue);
    }

    public static Optional<KnownProtocolAction> fromWire(String value) {
        return Arrays.stream(values())
            .filter(action -> action.wireValue.equals(value))
            .findFirst();
    }
}
