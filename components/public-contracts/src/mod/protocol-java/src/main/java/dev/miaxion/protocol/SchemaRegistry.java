package dev.miaxion.protocol;

import com.networknt.schema.Error;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SpecificationVersion;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import tools.jackson.databind.JsonNode;

public final class SchemaRegistry {
    private static final String BASE =
        "https://schemas.miaxion.dev/bridge/v1/";
    private static final List<String> RESOURCE_NAMES = List.of(
        "common.schema.json",
        "extensions.schema.json",
        "world-query.schema.json",
        "quest.schema.json",
        "player-observation.schema.json",
        "recipe.schema.json",
        "request-envelope.schema.json",
        "request.schema.json",
        "response.schema.json",
        "terminology-changed.schema.json");
    private static final SchemaRegistry INSTANCE = createPackaged();

    private final Schema envelopeSchema;
    private final Schema requestSchema;
    private final Schema terminologyChangedSchema;

    private SchemaRegistry(
        Schema envelopeSchema,
        Schema requestSchema,
        Schema terminologyChangedSchema
    ) {
        this.envelopeSchema = envelopeSchema;
        this.requestSchema = requestSchema;
        this.terminologyChangedSchema = terminologyChangedSchema;
    }

    public static SchemaRegistry load() {
        return INSTANCE;
    }

    static SchemaRegistry compile(Map<String, String> schemaSources) {
        try {
            Map<String, String> byId = new HashMap<>();
            Map<String, JsonNode> trees = new HashMap<>();
            for (Map.Entry<String, String> entry : schemaSources.entrySet()) {
                JsonNode tree = ProtocolJson.validatorTree(
                    entry.getValue().getBytes(StandardCharsets.UTF_8));
                JsonNode idNode = tree.get("$id");
                if (idNode == null || !idNode.isString()) {
                    throw new ProtocolValidationFailure(
                        "Schema has no local $id: " + entry.getKey());
                }
                String id = idNode.asString();
                if (!id.startsWith(BASE)) {
                    throw new ProtocolValidationFailure(
                        "Schema $id is outside the local registry: " + id);
                }
                byId.put(id, entry.getValue());
                trees.put(id, tree);
            }
            Set<String> ids = Set.copyOf(byId.keySet());
            for (Map.Entry<String, JsonNode> entry : trees.entrySet()) {
                rejectNonLocalReferences(
                    entry.getValue(),
                    URI.create(entry.getKey()),
                    ids,
                    true);
            }

            com.networknt.schema.SchemaRegistry compiled =
                com.networknt.schema.SchemaRegistry.withDefaultDialect(
                    SpecificationVersion.DRAFT_2020_12,
                    builder -> builder
                        .schemas(byId)
                        .schemaLoader(loader -> loader
                            .fetchRemoteResources(false)
                            .allow(location -> ids.contains(
                                documentUri(location.toString())))
                            .block(location -> !ids.contains(
                                documentUri(location.toString())))));
            Schema envelope = compiled.getSchema(SchemaLocation.of(
                BASE + "request-envelope.schema.json"));
            Schema request = compiled.getSchema(SchemaLocation.of(
                BASE + "request.schema.json"));
            Schema terminologyChanged = compiled.getSchema(SchemaLocation.of(
                BASE + "terminology-changed.schema.json"));
            return new SchemaRegistry(envelope, request, terminologyChanged);
        } catch (ProtocolValidationFailure failure) {
            throw failure;
        } catch (RuntimeException exception) {
            throw new ProtocolValidationFailure(
                "Could not compile local protocol schemas.", exception);
        }
    }

    List<Error> validateEnvelope(JsonNode tree) {
        return envelopeSchema.validate(
            tree,
            context -> context.executionConfig(
                config -> config.formatAssertionsEnabled(true)));
    }

    List<Error> validateKnown(
        JsonNode tree,
        KnownProtocolAction action
    ) {
        if (!tree.path("action").asString().equals(action.wireValue())) {
            throw new ProtocolValidationFailure(
                "Schema action does not match the requested known action.");
        }
        return requestSchema.validate(
            tree,
            context -> context.executionConfig(
                config -> config.formatAssertionsEnabled(true)));
    }

    List<Error> validateTerminologyChanged(JsonNode tree) {
        return terminologyChangedSchema.validate(tree,
            context -> context.executionConfig(
                config -> config.formatAssertionsEnabled(true)));
    }

    private static SchemaRegistry createPackaged() {
        Map<String, String> sources = new HashMap<>();
        for (String name : RESOURCE_NAMES) {
            String resource = "schema/" + name;
            try (InputStream stream = SchemaRegistry.class
                .getResourceAsStream(resource)) {
                if (stream == null) {
                    throw new ProtocolValidationFailure(
                        "Classpath schema is missing: " + resource);
                }
                sources.put(
                    name,
                    new String(stream.readAllBytes(), StandardCharsets.UTF_8));
            } catch (IOException exception) {
                throw new ProtocolValidationFailure(
                    "Could not read classpath schema: " + resource,
                    exception);
            }
        }
        return compile(sources);
    }

    private static void rejectNonLocalReferences(
        JsonNode node,
        URI base,
        Set<String> ids,
        boolean documentRoot
    ) {
        if (node.isObject()) {
            JsonNode nestedId = node.get("$id");
            if (!documentRoot && nestedId != null) {
                String value = nestedId.isString()
                    ? nestedId.asString()
                    : nestedId.toString();
                throw new ProtocolValidationFailure(
                    "Nested schema $id is forbidden: " + value);
            }
            for (String keyword : List.of(
                "$ref", "$dynamicRef", "$recursiveRef")) {
                rejectNonLocalReference(node.get(keyword), base, ids);
            }
            for (JsonNode child : node) {
                rejectNonLocalReferences(child, base, ids, false);
            }
        } else if (node.isArray()) {
            for (JsonNode child : node) {
                rejectNonLocalReferences(child, base, ids, false);
            }
        }
    }

    private static void rejectNonLocalReference(
        JsonNode reference,
        URI base,
        Set<String> ids
    ) {
        if (reference == null || !reference.isString()) {
            return;
        }
        String value = reference.asString();
        if (value.startsWith("#")) {
            return;
        }
        final String document;
        try {
            document = documentUri(base.resolve(value).toString());
        } catch (IllegalArgumentException exception) {
            throw new ProtocolValidationFailure(
                "Invalid schema reference: " + value, exception);
        }
        if (!ids.contains(document)) {
            throw new ProtocolValidationFailure(
                "Non-local schema reference is forbidden: " + value);
        }
    }

    private static String documentUri(String value) {
        int fragment = value.indexOf('#');
        return fragment < 0 ? value : value.substring(0, fragment);
    }
}
