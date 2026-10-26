package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/** Closed wire DTOs, bound only after schema validation; not permission to modify settings. */
public final class KeybindingPayloads {
    private KeybindingPayloads() {}

    public record Id(String providerId, String rawActionId, String compatibilityFamily) {}
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "kind")
    @JsonSubTypes({
        @JsonSubTypes.Type(value = Unbound.class, name = "unbound"),
        @JsonSubTypes.Type(value = Bound.class, name = "bound")
    })
    public sealed interface KeyValue permits Unbound, Bound {}
    public record Unbound() implements KeyValue {}
    public record Bound(String inputType, int code, String modifier) implements KeyValue {}
    /** Native context is compatibility evidence only, never a writable key value. */
    public record Context(String nativeConflictContext, String compatibilityEvidence) {}
    public record Selection(Id id, KeyValue value, Context context) {}
    public record CatalogRequest(
        @JsonInclude(JsonInclude.Include.NON_NULL) String catalogRevision,
        @JsonInclude(JsonInclude.Include.NON_NULL) String continuation) {}
    public record CatalogEntry(String rawActionId, Id id, String displayName, KeyValue currentValue,
                               Context context, boolean available, String unavailableReason) {}
    public record CatalogResponse(String instanceDirectory, String catalogRevision,
                                  List<CatalogEntry> entries, String nextContinuation) {
        public CatalogResponse { entries = List.copyOf(entries); }
    }
    public record PreviewRequest(String instanceDirectory, String catalogRevision, UUID profileId,
                                 long profileRevision, List<Selection> bindings) {
        public PreviewRequest { bindings = List.copyOf(bindings); }
    }
    public record PreviewEntry(Id id, KeyValue currentValue, KeyValue targetValue, String status, String reason) {}
    public record PreviewResponse(UUID previewToken, String instanceDirectory, String catalogRevision,
                                  List<PreviewEntry> entries) {
        public PreviewResponse { entries = List.copyOf(entries); }
    }
    /** Server-held preview freezes identities, contexts and old/new values. */
    public record ApplyRequest(String instanceDirectory, String catalogRevision, UUID previewToken) {}
    public record RollbackRequest(String instanceDirectory, UUID receiptId) {}
    public record Change(Id id, KeyValue oldValue, KeyValue newValue) {}
    public record Skip(Id id, String reason) {}
    public record Receipt(UUID receiptId, String instanceDirectory, String finalCatalogRevision,
                          List<Change> changed, List<Skip> skipped, String status, OffsetDateTime completedAt) {
        public Receipt { changed = List.copyOf(changed); skipped = List.copyOf(skipped); }
    }
    public record ApplyResponse(Receipt receipt) {}
    public record RollbackResponse(Receipt receipt) {}
}
