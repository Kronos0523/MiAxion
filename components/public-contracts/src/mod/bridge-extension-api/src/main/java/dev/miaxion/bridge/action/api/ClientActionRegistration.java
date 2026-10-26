package dev.miaxion.bridge.action.api;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Native-free metadata. The factory must defer all optional native class linking
 * until invoked by Bridge after exact dependency-version checks. Addons compile
 * against this base-owned API; they must not shade it or depend on Bridge internals.
 * API major is compatibility; addonVersion is only a bounded release label.
 * Bridge product release labels are not API requirements. requiredModVersions
 * describes the adapter's native target dependencies, not a Bridge release pin.
 * See docs/current/bridge-addon-compatibility.md for the v1 compatibility policy.
 * Action identity includes addonId, so supplemental addons never share ownership.
 */
public record ClientActionRegistration(String addonId, String addonVersion, int apiMajor,
    String targetModId, Map<String, Set<String>> requiredModVersions, List<Action> actions,
    Supplier<ClientActionAdapter> factory, CatalogKind catalogKind) {
    public enum CatalogKind { STATIC_KEYS, ARS_PAGES, IRON_SPELLS }
    /** Retains the P6 source and binary constructor for ordinary actions. */
    public ClientActionRegistration(String addonId, String addonVersion, int apiMajor,
        String targetModId, Map<String, Set<String>> requiredModVersions, List<Action> actions,
        Supplier<ClientActionAdapter> factory) {
        this(addonId, addonVersion, apiMajor, targetModId, requiredModVersions, actions, factory, CatalogKind.STATIC_KEYS);
    }
    public static final int API_MAJOR = 1;
    public ClientActionRegistration {
        requiredModVersions = requiredModVersions.entrySet().stream().collect(Collectors.toUnmodifiableMap(
            Map.Entry::getKey, e -> Set.copyOf(e.getValue())));
        actions = List.copyOf(actions);
    }
    /** Stable semantic identity, not translated text or a physical key. */
    public record Action(String actionId, String displayName) {}
}
