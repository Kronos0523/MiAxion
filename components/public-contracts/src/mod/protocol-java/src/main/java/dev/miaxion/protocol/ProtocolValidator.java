package dev.miaxion.protocol;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import tools.jackson.databind.JsonNode;

public final class ProtocolValidator {
    private static final ProtocolRuntimeLimits LIMITS = loadLimits();
    private static final Pattern ACTION = Pattern.compile(
        "^[a-z][a-z0-9]*(?:\\.[a-z][a-z0-9]*)+$");

    private final SchemaRegistry schemas;
    private final AtomicInteger rawParseCount = new AtomicInteger();

    public ProtocolValidator() {
        this(SchemaRegistry.load());
    }

    ProtocolValidator(SchemaRegistry schemas) {
        this.schemas = Objects.requireNonNull(schemas, "schemas");
    }

    public CommonRequest parseCommon(byte[] utf8, ReceiveStamp received) {
        Objects.requireNonNull(received, "received");
        rawParseCount.incrementAndGet();
        JsonNode tree = ProtocolJson.validatorTree(utf8);
        if (tree == null || !tree.isObject()) {
            throw new ProtocolValidationFailure(
                "A protocol request must be one JSON object.");
        }
        enforceSemanticStringLimits(tree);
        UUID requestId = parseRequestId(tree.get("requestId"));
        String action = parseAction(tree.get("action"));
        OffsetDateTime timestamp = parseTimestamp(tree.get("timestamp"));
        JsonNode deadlineNode = tree.get("deadline");
        if (deadlineNode == null) {
            throw new ProtocolValidationFailure(
                "Request common fields are invalid.");
        }
        OffsetDateTime deadline = parseDeadline(
            deadlineNode, requestId, action);
        if (!deadline.isAfter(timestamp)) {
            throw correlatedFailure(
                "Request deadline must be after its timestamp.",
                null, requestId, action, null, true,
                ProtocolErrorCode.INVALID_REQUEST);
        }

        JsonNode versionNode = tree.get("protocolVersion");
        if (versionNode != null && versionNode.isString()
            && !ProtocolConstants.VERSION.equals(versionNode.asString())) {
            throw correlatedFailure(
                "Request protocol version is unsupported.",
                null, requestId, action, deadline, false,
                ProtocolErrorCode.UNSUPPORTED_VERSION);
        }
        if (!schemas.validateEnvelope(tree).isEmpty()) {
            throw correlatedFailure(
                "Request envelope does not match the common schema.",
                null, requestId, action, deadline, false,
                ProtocolErrorCode.INVALID_REQUEST);
        }

        String version = versionNode.asString();
        JsonNode tokenNode = tree.get("sessionToken");
        String token = tokenNode.isNull() ? null : tokenNode.asString();
        return new CommonRequest(
            version,
            requestId,
            timestamp,
            deadline,
            action,
            token,
            received,
            new CommonRequest.ValidatorHandle(
                this,
                tree));
    }

    public KnownRequest validateKnown(
        CommonRequest request,
        KnownProtocolAction action
    ) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(action, "action");
        CommonRequest.ValidatorHandle handle = request.validatorHandle();
        if (handle.owner() != this) {
            throw new ProtocolValidationFailure(
                "Common request belongs to another validator.");
        }
        if (!request.rawAction().equals(action.wireValue())) {
            throw new ProtocolValidationFailure(
                "Known action does not match the parsed request action.");
        }
        if (!schemas.validateKnown(handle.tree(), action).isEmpty()) {
            throw new ProtocolValidationFailure(
                "Known request payload does not match its schema.");
        }

        byte[] canonicalBytes = ProtocolJson.validatorBytes(handle.tree());
        if (canonicalBytes.length > LIMITS.canonicalInboundBytes()) {
            throw new ProtocolValidationFailure(
                "Canonical request exceeds "
                    + LIMITS.canonicalInboundBytes() + " bytes.");
        }
        try {
            RequestEnvelope envelope = ProtocolJson.read(
                canonicalBytes,
                RequestEnvelope.class);
            Object payload = ProtocolJson.read(
                ProtocolJson.writeBytes(envelope.payload()),
                payloadType(action));
            return new KnownRequest(
                envelope,
                action,
                payload,
                canonicalBytes);
        } catch (java.io.IOException exception) {
            throw new ProtocolValidationFailure(
                "Known request could not bind to its typed DTO.", exception);
        }
    }

    public byte[] serialize(ResponseEnvelope response) {
        Objects.requireNonNull(response, "response");
        try {
            return ProtocolJson.writeBytes(response);
        } catch (com.fasterxml.jackson.core.JsonProcessingException exception) {
            throw new ProtocolValidationFailure(
                "Response could not be serialized.", exception);
        }
    }

    /** Unsolicited paired companion event; deliberately separate from response serialization. */
    public byte[] serializeTerminologyChanged(TerminologyPayloads.ChangedEnvelope event) {
        try {
            byte[] serialized = ProtocolJson.writeBytes(event);
            if (!schemas.validateTerminologyChanged(ProtocolJson.validatorTree(serialized)).isEmpty())
                throw new ProtocolValidationFailure("Terminology event does not match its schema.");
            return serialized;
        }
        catch (com.fasterxml.jackson.core.JsonProcessingException exception) {
            throw new ProtocolValidationFailure("Terminology event could not be serialized.", exception);
        }
    }

    int rawParseCount() {
        return rawParseCount.get();
    }

    private static UUID parseRequestId(JsonNode node) {
        if (node == null || !node.isString()) {
            throw new ProtocolValidationFailure(
                "Request common fields are invalid.");
        }
        try {
            return UUID.fromString(node.asString());
        } catch (IllegalArgumentException exception) {
            throw new ProtocolValidationFailure(
                "Request common fields are invalid.", exception);
        }
    }

    private static String parseAction(JsonNode node) {
        if (node == null || !node.isString()) {
            throw new ProtocolValidationFailure(
                "Request common fields are invalid.");
        }
        String action = node.asString();
        if (action.length() < 3 || action.length() > 64
            || !ACTION.matcher(action).matches()) {
            throw new ProtocolValidationFailure(
                "Request common fields are invalid.");
        }
        return action;
    }

    private static OffsetDateTime parseTimestamp(JsonNode node) {
        if (node == null || !node.isString()) {
            throw new ProtocolValidationFailure(
                "Request common fields are invalid.");
        }
        try {
            return OffsetDateTime.parse(node.asString());
        } catch (DateTimeParseException exception) {
            throw new ProtocolValidationFailure(
                "Request common fields are invalid.", exception);
        }
    }

    private static OffsetDateTime parseDeadline(
        JsonNode node,
        UUID requestId,
        String action
    ) {
        if (!node.isString()) {
            throw correlatedFailure(
                "Request deadline is invalid.",
                null, requestId, action, null, true,
                ProtocolErrorCode.INVALID_REQUEST);
        }
        try {
            return OffsetDateTime.parse(node.asString());
        } catch (DateTimeParseException exception) {
            throw correlatedFailure(
                "Request deadline is invalid.",
                exception, requestId, action, null, true,
                ProtocolErrorCode.INVALID_REQUEST);
        }
    }

    private static ProtocolValidationFailure correlatedFailure(
        String message,
        Throwable cause,
        UUID requestId,
        String action,
        OffsetDateTime deadline,
        boolean sentinel,
        ProtocolErrorCode errorCode
    ) {
        return new ProtocolValidationFailure(
            message,
            cause,
            new ProtocolValidationFailure.Correlation(
                requestId, action, deadline, sentinel),
            errorCode);
    }

    private static void enforceSemanticStringLimits(JsonNode node) {
        if (node.isString()) {
            enforceUnicodeScalars(node.asString(), "JSON string");
        }
        if (node.isObject()) {
            for (String name : node.propertyNames()) {
                enforceUnicodeScalars(name, "JSON property name");
            }
        }
        for (JsonNode child : node) {
            enforceSemanticStringLimits(child);
        }
    }

    private static void enforceUnicodeScalars(
        String value,
        String description
    ) {
        int scalars = 0;
        for (int index = 0; index < value.length(); index++) {
            char current = value.charAt(index);
            if (Character.isHighSurrogate(current)) {
                if (index + 1 >= value.length()
                    || !Character.isLowSurrogate(value.charAt(index + 1))) {
                    throw new ProtocolValidationFailure(
                        description + " contains an invalid Unicode scalar.");
                }
                index++;
            } else if (Character.isLowSurrogate(current)) {
                throw new ProtocolValidationFailure(
                    description + " contains an invalid Unicode scalar.");
            }
            scalars++;
            if (scalars > LIMITS.semanticJsonStringScalars()) {
                throw new ProtocolValidationFailure(
                    description + " exceeds the Unicode scalar limit.");
            }
        }
    }

    private static Class<?> payloadType(KnownProtocolAction action) {
        return switch (action) {
            case KEYBINDINGS_CATALOG -> KeybindingPayloads.CatalogRequest.class;
            case KEYBINDINGS_PREVIEW -> KeybindingPayloads.PreviewRequest.class;
            case KEYBINDINGS_APPLY -> KeybindingPayloads.ApplyRequest.class;
            case KEYBINDINGS_ROLLBACK -> KeybindingPayloads.RollbackRequest.class;
            case ACTIONS_CONTEXT -> GameActionPayloads.ContextRequest.class;
            case ACTIONS_CATALOG -> GameActionPayloads.CatalogRequest.class;
            case ACTIONS_CONFIGURATION -> ActionConfigurationPayloads.Request.class;
            case ACTIONS_GRANT -> GameActionPayloads.GrantRequest.class;
            case ACTIONS_REVOKE -> GameActionPayloads.RevokeRequest.class;
            case ACTIONS_EXECUTE -> GameActionPayloads.ExecuteRequest.class;
            case ACTIONS_STATUS -> GameActionPayloads.StatusRequest.class;
            case SESSION_PAIR_REQUEST -> SessionPairRequest.class;
            case SESSION_REVOKE -> SessionRevokeRequest.class;
            case REQUEST_CANCEL -> RequestCancelRequest.class;
            case CAPABILITIES_LIST -> CapabilitiesListRequest.class;
            case EXTENSIONS_LIST -> ExtensionListRequest.class;
            case EXTENSIONS_CALL -> ExtensionCallRequest.class;
            case RECIPE_QUERY -> RecipeQueryRequest.class;
            case RECIPE_USES -> RecipeUsesRequest.class;
            case RECIPE_QUERY_SOURCE, RECIPE_USES_SOURCE -> RecipeSourceRequest.class;
            case RECIPE_PROVIDERS_LIST -> RecipeProvidersListRequest.class;
            case QUESTS_QUERY -> QuestQueryRequest.class;
            case QUESTS_DETAIL -> QuestDetailRequest.class;
            case INVENTORY_VIEW, PLAYER_STATUS, WORLD_CONTEXT, SCREEN_SNAPSHOT, TARGET_SNAPSHOT -> PlayerObservationPayloads.Request.class;
            case INVENTORY_SNAPSHOT -> InventorySnapshotRequest.class;
            case TERMINOLOGY_CONFIGURE, TERMINOLOGY_V2_CONFIGURE -> TerminologyPayloads.ConfigureRequest.class;
            case TERMINOLOGY_SNAPSHOT -> TerminologyPayloads.SnapshotRequest.class;
            case TERMINOLOGY_CLEAR -> TerminologyPayloads.ClearRequest.class;
            case PLAYER_SNAPSHOT -> PlayerSnapshotRequest.class;
            case ENTITY_RESOLVE -> EntityResolveRequest.class;
            case ENTITY_DESCRIBE -> EntityDescribeRequest.class;
            case MOD_VERSIONS_RESOLVE -> ModVersionsResolveRequest.class;
            case WORLD_STRUCTURE_LOCATE, WORLD_BIOME_LOCATE -> WorldLocateRequest.class;
            case WORLD_MACHINES_SCAN, WORLD_CONTAINERS_SCAN -> WorldNearbyScanRequest.class;
            case WORLD_STRUCTURE_TARGETS -> WorldStructureTargetsRequest.class;
            case WORLD_REGISTRY_SEARCH -> WorldRegistrySearchRequest.class;
            case WORLD_REGISTRY_RESOLVE -> WorldRegistryResolveRequest.class;
            case WORLD_REGISTRY_DESCRIBE -> WorldRegistryDescribeRequest.class;
            case WORLD_QUERY_PROGRESS -> WorldQueryProgressRequest.class;
            case HUD_SHOW -> HudShowRequest.class;
            case HEALTH_PING -> HealthPingRequest.class;
            case SESSION_PAIR_APPROVED -> throw new ProtocolValidationFailure(
                "session.pair.approved is response-only.");
            case TERMINOLOGY_CHANGED, TERMINOLOGY_V2_CHANGED -> throw new ProtocolValidationFailure(
                "terminology.changed is an unsolicited event, not a request.");
        };
    }

    private static ProtocolRuntimeLimits loadLimits() {
        try {
            return ProtocolRuntimeLimitsLoader.load();
        } catch (java.io.IOException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }
}
