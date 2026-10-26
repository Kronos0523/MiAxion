package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Paired-companion terminology state. It deliberately has no reader, cursor, or tool permission. */
public final class TerminologyPayloads {
    private TerminologyPayloads() {}

    public record ConfigureRequest(Settings settings) { public ConfigureRequest { Objects.requireNonNull(settings); } }
    public record SnapshotRequest() {}
    public record ClearRequest() {}
    public record Settings(boolean enabled, List<String> sources, int capacity,
                           int browserWindowSeconds, int retentionSeconds) {
        public Settings {
            sources = List.copyOf(sources);
            if (sources.size() > 4 || sources.stream().distinct().count() != sources.size()
                || !List.of("native_crosshair", "player_inventory", "item_browser", "hotbar").containsAll(sources)
                || capacity < 1 || capacity > 4096 || browserWindowSeconds < 1 || browserWindowSeconds > 3600
                || retentionSeconds < 0 || retentionSeconds > 86400) throw new IllegalArgumentException("Invalid terminology settings");
        }
    }
    public record Identity(String kind, String resourceId, String ingredientTypeUid) {
        public Identity(String kind, String resourceId) { this(kind, resourceId, null); }
    }
    public record Entry(Identity identity, String displayName, List<String> sources, String observedAtUtc) {
        public Entry { sources = List.copyOf(sources); }
    }
    public record BrowserGroup(long groupId, List<Entry> entries, long remainingRetentionMilliseconds) {
        public BrowserGroup { entries = List.copyOf(entries); }
    }
    public record Effect(String resourceId, int amplifier, String durationKind, Integer durationTicks) { }
    public record ItemDetails(Identity identity, String displayName, String source, String observedAtUtc,
                              String tooltipStatus, List<String> tooltip, String effectsStatus, List<Effect> effects) {
        public ItemDetails {
            tooltip = List.copyOf(tooltip); effects = List.copyOf(effects);
            if (tooltip.size() > 256 || tooltip.stream().mapToLong(s -> s.getBytes(java.nio.charset.StandardCharsets.UTF_8).length).sum() > 8192
                || effects.size() > 32) throw new IllegalArgumentException("Item details exceed their byte/count limits");
        }
    }
    public record Snapshot(long producerGeneration, long revision, String language, Settings captureSettings,
                           List<Entry> entries, List<BrowserGroup> browserGroups,
                           Map<String, String> sourceAvailability, boolean truncated,
                           @com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL) ItemDetails itemDetails) {
        public Snapshot(long producerGeneration, long revision, String language, Settings captureSettings,
                        List<Entry> entries, List<BrowserGroup> browserGroups, Map<String, String> sourceAvailability, boolean truncated) {
            this(producerGeneration, revision, language, captureSettings, entries, browserGroups, sourceAvailability, truncated, null);
        }
        public Snapshot legacy() { return new Snapshot(producerGeneration, revision, language, captureSettings, entries, browserGroups, sourceAvailability, truncated); }
        public Snapshot { entries = List.copyOf(entries); browserGroups = List.copyOf(browserGroups); sourceAvailability = Map.copyOf(sourceAvailability); }
    }
    /** Event envelope is intentionally not a response: it has no requestId and cannot enter response correlation. */
    public record ChangedEnvelope(@JsonProperty(required = true) String protocolVersion,
                                 @JsonProperty(required = true) ProtocolAction action,
                                 @JsonProperty(required = true) Snapshot payload) {
        public ChangedEnvelope {
            if (!ProtocolConstants.VERSION.equals(protocolVersion) || !(KnownProtocolAction.TERMINOLOGY_CHANGED.wireValue().equals(action.value())
                || KnownProtocolAction.TERMINOLOGY_V2_CHANGED.wireValue().equals(action.value())))
                throw new IllegalArgumentException("Invalid terminology changed envelope");
            Objects.requireNonNull(payload);
        }
    }
}
