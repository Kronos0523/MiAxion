package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/** Contract only; pairing and caller-supplied scope never constitute an action grant. */
public final class GameActionPayloads {
    private GameActionPayloads() {}

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "kind")
    @JsonSubTypes({
        @JsonSubTypes.Type(value = KeyAction.class, name = "keyAction"),
        @JsonSubTypes.Type(value = ArsPage.class, name = "arsPage"),
        @JsonSubTypes.Type(value = IronSpell.class, name = "ironSpell")
    })
    public sealed interface Target permits KeyAction, ArsPage, IronSpell {}
    public record KeyAction(KeybindingPayloads.Id id) implements Target {}
    public record ArsPage(int pageOneBased) implements Target {}
    public record IronSpell(String namespacedSpellId) implements Target {}
    /** Assertions compared to the server-owned session lease, never trusted as authority. */
    public record Scope(String instanceDirectory, long bridgeSessionGeneration, long worldEpoch,
                        UUID localPlayerUuid, String mode, UUID profileId, long profileRevision,
                        long mappingRevision, long grantRevision) {}
    /** Explicit paired-desktop selection; no world/player/session authority is supplied. Null profile clears it. */
    public record ContextRequest(String mode, UUID profileId, long profileRevision, long mappingRevision) {}
    public record ContextResponse(long grantRevision) {}
    public record CatalogRequest(
        @JsonInclude(JsonInclude.Include.NON_NULL) String catalogRevision,
        @JsonInclude(JsonInclude.Include.NON_NULL) String continuation) {}
    public record CatalogEntry(Target target, String displayName, boolean available, String unavailableReason,
                               @JsonInclude(JsonInclude.Include.NON_NULL) String categoryId,
                               @JsonInclude(JsonInclude.Include.NON_NULL) String categoryDisplayName) {
        public CatalogEntry(Target target, String displayName, boolean available, String unavailableReason) {
            this(target, displayName, available, unavailableReason, null, null);
        }
    }
    public record CatalogResponse(Scope scope, String catalogRevision, List<CatalogEntry> entries,
                                  String nextContinuation) {
        public CatalogResponse { entries = List.copyOf(entries); }
    }
    public record GrantRequest(Scope scope, List<Target> targets) {
        public GrantRequest { targets = List.copyOf(targets); }
    }
    public record GrantResponse(UUID grantId, Scope scope, OffsetDateTime expiresAt) {}
    public record RevokeRequest(UUID grantId, Scope scope) {}
    public record RevokeResponse(boolean revoked) {}
    /** Absolute execution deadline is the existing envelope deadline, not a renewable payload field. */
    public record ExecuteRequest(UUID grantId, Scope scope, UUID captureId, UUID executionId,
                                 Target target, OffsetDateTime releasedAt) {}
    public record StatusRequest(UUID grantId, UUID executionId) {}
    public record Outcome(String state, String reason) {}
    public record ExecutionResponse(UUID executionId, Outcome outcome) {}
}
