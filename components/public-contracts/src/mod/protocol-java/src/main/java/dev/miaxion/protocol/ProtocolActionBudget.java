package dev.miaxion.protocol;

/** Shared action policy. Query deadlines use the existing request envelope, not a new settings channel. */
public final class ProtocolActionBudget {
    public static final String CAPABILITY = "runtime.query.budget";
    public static final String VERSION = "1.0";
    private ProtocolActionBudget() {}

    public static boolean isRecipe(KnownProtocolAction action) {
        return action == KnownProtocolAction.RECIPE_QUERY || action == KnownProtocolAction.RECIPE_USES
            || action == KnownProtocolAction.RECIPE_QUERY_SOURCE || action == KnownProtocolAction.RECIPE_USES_SOURCE;
    }

    public static boolean isWorld(KnownProtocolAction action) {
        return action != null && action.wireValue().startsWith("world.");
    }

    public static boolean isQuery(KnownProtocolAction action) {
        if (action == null) return false;
        return switch (action) {
            case CAPABILITIES_LIST, EXTENSIONS_LIST, RECIPE_QUERY, RECIPE_USES,
                RECIPE_QUERY_SOURCE, RECIPE_USES_SOURCE, RECIPE_PROVIDERS_LIST,
                QUESTS_QUERY, QUESTS_DETAIL, INVENTORY_VIEW, PLAYER_STATUS, WORLD_CONTEXT,
                SCREEN_SNAPSHOT, TARGET_SNAPSHOT, INVENTORY_SNAPSHOT, TERMINOLOGY_SNAPSHOT,
                PLAYER_SNAPSHOT, ENTITY_RESOLVE, ENTITY_DESCRIBE, MOD_VERSIONS_RESOLVE,
                WORLD_STRUCTURE_LOCATE, WORLD_BIOME_LOCATE, WORLD_MACHINES_SCAN,
                WORLD_CONTAINERS_SCAN, WORLD_STRUCTURE_TARGETS, WORLD_REGISTRY_RESOLVE, WORLD_REGISTRY_SEARCH,
                WORLD_REGISTRY_DESCRIBE, WORLD_QUERY_PROGRESS, KEYBINDINGS_CATALOG,
                KEYBINDINGS_PREVIEW, ACTIONS_CONTEXT, ACTIONS_CATALOG, ACTIONS_CONFIGURATION,
                ACTIONS_STATUS -> true;
            default -> false;
        };
    }

    public static int defaultDeadlineMs(KnownProtocolAction action, ProtocolRuntimeLimits limits) {
        if (action == KnownProtocolAction.SESSION_PAIR_REQUEST) return limits.pairingDeadlineMs();
        if (isRecipe(action)) return limits.recipeActionDeadlineMs();
        if (action == KnownProtocolAction.WORLD_STRUCTURE_LOCATE || action == KnownProtocolAction.WORLD_BIOME_LOCATE)
            return limits.worldQueryLocatorDeadlineMs();
        if (action == KnownProtocolAction.WORLD_MACHINES_SCAN || action == KnownProtocolAction.WORLD_CONTAINERS_SCAN
            || action == KnownProtocolAction.WORLD_STRUCTURE_TARGETS) return limits.worldQueryNearbyDeadlineMs();
        return limits.actionDeadlineMs();
    }

    public static int maximumDeadlineMs(KnownProtocolAction action, ProtocolRuntimeLimits limits) {
        return isQuery(action) ? limits.queryDeadlineMaximumMs() : defaultDeadlineMs(action, limits);
    }

    public static CapabilityDescriptor capability() { return new CapabilityDescriptor(CAPABILITY, VERSION, true); }
}
