using MiAxion.Protocol.Contracts;

namespace MiAxion.Protocol.Runtime;

public static class ProtocolActionBudget
{
    public const string Capability = "runtime.query.budget";
    public const string Version = "1.0";

    public static bool IsRecipe(KnownProtocolAction action) => action is
        KnownProtocolAction.RecipeQuery or KnownProtocolAction.RecipeUses or
        KnownProtocolAction.RecipeQuerySource or KnownProtocolAction.RecipeUsesSource;

    public static bool IsWorld(ProtocolAction action) => action.Value?.StartsWith("world.", StringComparison.Ordinal) == true;

    public static bool IsQuery(KnownProtocolAction action) => action is
        KnownProtocolAction.CapabilitiesList or
        KnownProtocolAction.ExtensionsList or
        KnownProtocolAction.RecipeQuery or
        KnownProtocolAction.RecipeUses or
        KnownProtocolAction.RecipeQuerySource or
        KnownProtocolAction.RecipeUsesSource or
        KnownProtocolAction.RecipeProvidersList or
        KnownProtocolAction.QuestsQuery or
        KnownProtocolAction.QuestsDetail or
        KnownProtocolAction.InventoryView or
        KnownProtocolAction.PlayerStatus or
        KnownProtocolAction.WorldContext or
        KnownProtocolAction.ScreenSnapshot or
        KnownProtocolAction.TargetSnapshot or
        KnownProtocolAction.InventorySnapshot or
        KnownProtocolAction.TerminologySnapshot or
        KnownProtocolAction.PlayerSnapshot or
        KnownProtocolAction.EntityResolve or
        KnownProtocolAction.EntityDescribe or
        KnownProtocolAction.ModVersionsResolve or
        KnownProtocolAction.WorldStructureLocate or
        KnownProtocolAction.WorldBiomeLocate or
        KnownProtocolAction.WorldMachinesScan or
        KnownProtocolAction.WorldContainersScan or
        KnownProtocolAction.WorldStructureTargets or
        KnownProtocolAction.WorldRegistrySearch or
        KnownProtocolAction.WorldRegistryResolve or
        KnownProtocolAction.WorldRegistryDescribe or
        KnownProtocolAction.WorldQueryProgress or
        KnownProtocolAction.KeybindingsCatalog or
        KnownProtocolAction.KeybindingsPreview or
        KnownProtocolAction.ActionsContext or
        KnownProtocolAction.ActionsCatalog or
        KnownProtocolAction.ActionsConfiguration or
        KnownProtocolAction.ActionsStatus;

    public static int DefaultDeadlineMs(ProtocolAction action, ProtocolRuntimeLimits limits)
    {
        if (!action.TryGetKnown(out var known)) return limits.ActionDeadlineMs;
        if (known == KnownProtocolAction.SessionPairRequest) return limits.PairingDeadlineMs;
        if (IsRecipe(known)) return limits.RecipeActionDeadlineMs;
        return known switch
        {
            KnownProtocolAction.WorldStructureLocate or KnownProtocolAction.WorldBiomeLocate => limits.WorldQueryLocatorDeadlineMs,
            KnownProtocolAction.WorldMachinesScan or KnownProtocolAction.WorldContainersScan or
                KnownProtocolAction.WorldStructureTargets => limits.WorldQueryNearbyDeadlineMs,
            _ => limits.ActionDeadlineMs
        };
    }

    public static int MaximumDeadlineMs(ProtocolAction action, ProtocolRuntimeLimits limits) =>
        action.TryGetKnown(out var known) && IsQuery(known)
            ? limits.QueryDeadlineMaximumMs : DefaultDeadlineMs(action, limits);

    public static int ResponseMaximumBytes(ProtocolAction action, ProtocolRuntimeLimits limits) =>
        action.TryGetKnown(out var known) && IsRecipe(known) ? limits.RecipeResponseMessageBytes :
        IsWorld(action) ? limits.WorldQueryResponseMessageBytes : limits.ServerToDesktopMessageBytes;
}
