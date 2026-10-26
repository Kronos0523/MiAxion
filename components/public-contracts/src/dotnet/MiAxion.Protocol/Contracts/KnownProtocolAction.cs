namespace MiAxion.Protocol.Contracts;

public enum KnownProtocolAction
{
    SessionPairRequest,
    SessionPairApproved,
    SessionRevoke,
    RequestCancel,
    CapabilitiesList,
    ExtensionsList,
    ExtensionsCall,
    RecipeQuery,
    RecipeUses,
    RecipeQuerySource,
    RecipeUsesSource,
    RecipeProvidersList,
    QuestsQuery,
    QuestsDetail,
    InventorySnapshot,
    TerminologyConfigure,
    TerminologySnapshot,
    TerminologyClear,
    TerminologyChanged,
    TerminologyV2Configure,
    TerminologyV2Changed,
    PlayerSnapshot,
    EntityResolve,
    EntityDescribe,
    ModVersionsResolve,
    WorldStructureLocate,
    WorldBiomeLocate,
    WorldMachinesScan,
    WorldContainersScan,
    WorldStructureTargets,
    WorldRegistrySearch,
    WorldRegistryResolve,
    WorldRegistryDescribe,
    WorldQueryProgress,
    HudShow,
    HealthPing,
    KeybindingsCatalog,
    KeybindingsPreview,
    KeybindingsApply,
    KeybindingsRollback,
    ActionsContext,
    ActionsCatalog,
    ActionsConfiguration,
    ActionsGrant,
    ActionsRevoke,
    ActionsExecute,
    ActionsStatus,
    InventoryView, PlayerStatus, WorldContext, ScreenSnapshot, TargetSnapshot

}

internal static class KnownProtocolActionWire
{
    private static readonly Dictionary<KnownProtocolAction, string>
        ByAction = new Dictionary<KnownProtocolAction, string>
        {
            [KnownProtocolAction.SessionPairRequest] =
                "session.pair.request",
            [KnownProtocolAction.SessionPairApproved] =
                "session.pair.approved",
            [KnownProtocolAction.SessionRevoke] = "session.revoke",
            [KnownProtocolAction.RequestCancel] = "request.cancel",
            [KnownProtocolAction.CapabilitiesList] = "capabilities.list",
            [KnownProtocolAction.ExtensionsList] = "extensions.list",
            [KnownProtocolAction.ExtensionsCall] = "extensions.call",
            [KnownProtocolAction.RecipeQuery] = "recipe.query",
            [KnownProtocolAction.RecipeUses] = "recipe.uses",
            [KnownProtocolAction.RecipeQuerySource] = "recipe.query.source",
            [KnownProtocolAction.RecipeUsesSource] = "recipe.uses.source",
            [KnownProtocolAction.RecipeProvidersList] = "recipe.providers.list",
            [KnownProtocolAction.QuestsQuery] = "quests.query",
            [KnownProtocolAction.QuestsDetail] = "quests.detail",
            [KnownProtocolAction.InventoryView] = "inventory.view",
            [KnownProtocolAction.PlayerStatus] = "player.status",
            [KnownProtocolAction.WorldContext] = "world.context",
            [KnownProtocolAction.ScreenSnapshot] = "screen.snapshot",
            [KnownProtocolAction.TargetSnapshot] = "target.snapshot",
            [KnownProtocolAction.InventorySnapshot] =
                "inventory.snapshot",
            [KnownProtocolAction.PlayerSnapshot] = "player.snapshot",
            [KnownProtocolAction.TerminologyConfigure] = "terminology.configure",
            [KnownProtocolAction.TerminologySnapshot] = "terminology.snapshot",
            [KnownProtocolAction.TerminologyClear] = "terminology.clear",
            [KnownProtocolAction.TerminologyChanged] = "terminology.changed",
            [KnownProtocolAction.TerminologyV2Configure] = "terminology.v2.configure",
            [KnownProtocolAction.TerminologyV2Changed] = "terminology.v2.changed",
            [KnownProtocolAction.EntityResolve] = "entities.resolve",
            [KnownProtocolAction.EntityDescribe] = "entities.describe",
            [KnownProtocolAction.ModVersionsResolve] = "mods.versions.resolve",
            [KnownProtocolAction.WorldStructureLocate] = "world.structure.locate",
            [KnownProtocolAction.WorldBiomeLocate] = "world.biome.locate",
            [KnownProtocolAction.WorldMachinesScan] = "world.machines.scan",
            [KnownProtocolAction.WorldContainersScan] = "world.containers.scan",
            [KnownProtocolAction.WorldStructureTargets] = "world.structure.targets",
            [KnownProtocolAction.WorldRegistrySearch] = "world.registry.search",
            [KnownProtocolAction.WorldRegistryResolve] = "world.registry.resolve",
            [KnownProtocolAction.WorldRegistryDescribe] = "world.registry.describe",
            [KnownProtocolAction.WorldQueryProgress] = "world.query.progress",
            [KnownProtocolAction.HudShow] = "hud.show",
            [KnownProtocolAction.HealthPing] = "health.ping",
            [KnownProtocolAction.KeybindingsCatalog] = "keybindings.catalog",
            [KnownProtocolAction.KeybindingsPreview] = "keybindings.preview",
            [KnownProtocolAction.KeybindingsApply] = "keybindings.apply",
            [KnownProtocolAction.KeybindingsRollback] = "keybindings.rollback",
            [KnownProtocolAction.ActionsContext] = "actions.context",
            [KnownProtocolAction.ActionsCatalog] = "actions.catalog",
            [KnownProtocolAction.ActionsConfiguration] = "actions.configuration",
            [KnownProtocolAction.ActionsGrant] = "actions.grant",
            [KnownProtocolAction.ActionsRevoke] = "actions.revoke",
            [KnownProtocolAction.ActionsExecute] = "actions.execute",
            [KnownProtocolAction.ActionsStatus] = "actions.status",
        };

    private static readonly Dictionary<string, KnownProtocolAction>
        ByWire = ByAction.ToDictionary(
            pair => pair.Value,
            pair => pair.Key,
            StringComparer.Ordinal);

    internal static bool TryParse(
        string value,
        out KnownProtocolAction action) =>
        ByWire.TryGetValue(value, out action);
}
