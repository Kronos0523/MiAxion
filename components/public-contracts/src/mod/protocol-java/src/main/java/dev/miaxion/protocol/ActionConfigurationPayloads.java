package dev.miaxion.protocol;

import java.util.List;

/** Optional, paired, read-only declaration capability; independent of action grants. */
public final class ActionConfigurationPayloads {
    private ActionConfigurationPayloads() {}
    public record Request(int schemaVersion) {}
    public record Field(String id, String type, String label, boolean required, boolean readOnly, String storageScope) {}
    public record Group(String id, String label, List<Field> fields) {}
    public record Page(int schemaVersion, String pageId, String displayName, String actionKind,
        List<String> supportedModes, List<String> requiredCapabilities, List<Group> groups) {}
    public record Entry(String addonId, String addonVersion, String targetModId, Page page, String unavailableReason) {}
    public record Response(int schemaVersion, String instanceDirectory, List<Entry> entries) {}
}
