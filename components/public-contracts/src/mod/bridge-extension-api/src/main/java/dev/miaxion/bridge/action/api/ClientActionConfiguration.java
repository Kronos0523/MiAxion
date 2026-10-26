package dev.miaxion.bridge.action.api;

import java.util.List;

/** Data-only configuration declarations. The desktop owns controls, storage and execution authority.
 * v1 implements actionList, aliasList and availability only; no script, command or arbitrary UI.
 * Identifiers are stable within the actual IMC sender. Runtime handles never become saved fields. */
public final class ClientActionConfiguration {
    private ClientActionConfiguration() {}
    public static final int SCHEMA_VERSION = 1;
    public record Field(String id, String type, String label, boolean required, boolean readOnly, String storageScope) {}
    public record Group(String id, String label, List<Field> fields) {
        public Group { fields = List.copyOf(fields); }
    }
    public record Page(int schemaVersion, String pageId, String displayName, String actionKind,
        List<String> supportedModes, List<String> requiredCapabilities, List<Group> groups) {
        public Page { supportedModes = List.copyOf(supportedModes); requiredCapabilities = List.copyOf(requiredCapabilities); groups = List.copyOf(groups); }
    }
}
