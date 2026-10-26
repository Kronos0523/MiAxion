package dev.miaxion.protocol;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SpecificationVersion;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Bounded data validation, with no remote resolution or implicit default insertion. */
public final class ExtensionDataSchema {
    private static final String ID = "https://schemas.miaxion.dev/extension/local";
    private final Schema compiled;

    public ExtensionDataSchema(String source) {
        try {
            JsonNode root = data(source);
            if (!root.path("type").isTextual() || !root.path("type").asText().equals("object")) throw invalid();
            checkSchema(root, root, new HashSet<>(), 0);
            compiled = com.networknt.schema.SchemaRegistry.withDefaultDialect(
                SpecificationVersion.DRAFT_2020_12, builder -> builder.schemas(Map.of(ID, source))
                    .schemaLoader(loader -> loader.fetchRemoteResources(false)
                        .allow(location -> location.toString().split("#", 2)[0].equals(ID))
                        .block(location -> !location.toString().split("#", 2)[0].equals(ID))))
                .getSchema(SchemaLocation.of(ID));
        } catch (Exception | LinkageError ignored) {
            // Never attach parser exceptions: they may contain extension-provided schema text.
            throw invalid();
        }
    }

    public boolean accepts(String source) {
        try {
            data(source);
            return compiled.validate(ProtocolJson.validatorTree(source.getBytes(StandardCharsets.UTF_8))).isEmpty();
        } catch (Exception | LinkageError ignored) {
            return false;
        }
    }

    private static JsonNode data(String source) throws Exception {
        if (source == null || source.getBytes(StandardCharsets.UTF_8).length > 16384) throw invalid();
        unicode(source);
        // The validator reader checks duplicate keys and trailing tokens before the DTO reader runs.
        ProtocolJson.validatorTree(source.getBytes(StandardCharsets.UTF_8));
        JsonNode node = ProtocolJson.tree(source);
        if (node == null || !node.isObject()) throw invalid();
        checkData(node, 0);
        return node;
    }

    private static void checkData(JsonNode node, int depth) {
        if (depth > 24) throw invalid();
        if (node.isObject()) node.properties().forEach(entry -> {
            unicode(entry.getKey());
            checkData(entry.getValue(), depth + 1);
        });
        else if (node.isArray()) node.forEach(child -> checkData(child, depth + 1));
        else if (node.isTextual()) unicode(node.textValue());
    }

    private static void unicode(String value) {
        for (int index = 0; index < value.length(); index++) {
            char unit = value.charAt(index);
            if (Character.isHighSurrogate(unit)) {
                if (++index >= value.length() || !Character.isLowSurrogate(value.charAt(index))) throw invalid();
            } else if (Character.isLowSurrogate(unit)) throw invalid();
        }
    }

    private static void checkSchema(JsonNode node, JsonNode root, Set<String> references, int depth) {
        if (depth > 24) throw invalid();
        if (node.isBoolean()) return;
        if (!node.isObject()) throw invalid();
        for (var entry : node.properties()) {
            JsonNode value = entry.getValue();
            switch (entry.getKey()) {
                case "type" -> {
                    if (value.isTextual()) type(value);
                    else if (value.isArray() && !value.isEmpty()) {
                        var types = new HashSet<JsonNode>();
                        for (var item : value) { type(item); if (!types.add(item)) throw invalid(); }
                    } else throw invalid();
                }
                case "properties", "$defs" -> {
                    if (!value.isObject()) throw invalid();
                    for (var item : value) checkSchema(item, root, references, depth + 1);
                }
                case "additionalProperties", "items", "not" -> checkSchema(value, root, references, depth + 1);
                case "allOf", "anyOf", "oneOf" -> {
                    if (!value.isArray() || value.isEmpty()) throw invalid();
                    for (var item : value) checkSchema(item, root, references, depth + 1);
                }
                case "required" -> {
                    if (!value.isArray()) throw invalid();
                    var names = new HashSet<String>();
                    for (var item : value) if (!item.isTextual() || !names.add(item.textValue())) throw invalid();
                }
                case "minItems", "maxItems", "minLength", "maxLength" -> {
                    if (!value.isIntegralNumber() || !value.canConvertToInt() || value.intValue() < 0) throw invalid();
                }
                case "minimum", "maximum", "exclusiveMinimum", "exclusiveMaximum", "multipleOf" -> {
                    if (!value.isNumber() || !Double.isFinite(value.doubleValue()) ||
                        entry.getKey().equals("multipleOf") && value.doubleValue() <= 0) throw invalid();
                }
                case "uniqueItems" -> { if (!value.isBoolean()) throw invalid(); }
                case "enum" -> { if (!value.isArray() || value.isEmpty()) throw invalid(); }
                case "title", "description" -> { if (!value.isTextual()) throw invalid(); }
                case "examples" -> { if (!value.isArray()) throw invalid(); }
                case "const", "default" -> { /* Literal data, not schemas. */ }
                case "$schema" -> {
                    if (!value.isTextual() || !value.textValue().equals("https://json-schema.org/draft/2020-12/schema")) throw invalid();
                }
                case "$ref" -> {
                    if (!value.isTextual()) throw invalid();
                    String ref = value.textValue();
                    if ((!ref.equals("#") && !ref.startsWith("#/")) || ref.contains("%") ||
                        ref.matches(".*~(?![01]).*") || !references.add(ref)) throw invalid();
                    JsonNode target = root;
                    if (!ref.equals("#")) for (String part : ref.substring(2).split("/", -1)) {
                        if (!target.isObject()) throw invalid();
                        target = target.get(part.replace("~1", "/").replace("~0", "~"));
                        if (target == null) throw invalid();
                    }
                    checkSchema(target, root, references, depth + 1);
                    references.remove(ref);
                }
                default -> throw invalid();
            }
        }
    }

    private static void type(JsonNode node) {
        if (!node.isTextual() || !List.of("object", "array", "string", "number", "integer", "boolean", "null").contains(node.textValue())) throw invalid();
    }

    private static IllegalArgumentException invalid() { return new IllegalArgumentException("Unsupported extension data contract."); }
}
