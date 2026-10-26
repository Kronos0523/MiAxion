package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public record ExtensionCallRequest(
    @JsonProperty(required = true) String capabilityId,
    @JsonProperty(required = true) String capabilityVersion,
    @JsonProperty(required = true) String catalogRevision,
    @JsonProperty(required = true) JsonNode arguments
) {
    public ExtensionCallRequest {
        Objects.requireNonNull(capabilityId);
        Objects.requireNonNull(capabilityVersion);
        Objects.requireNonNull(catalogRevision);
        if (capabilityId.length() > 128 || !capabilityId.matches("[a-z0-9_.-]+:[a-z0-9_./-]+") ||
            capabilityVersion.length() > 32 || !capabilityVersion.matches("[0-9]+\\.[0-9]+") ||
            catalogRevision.isBlank() || catalogRevision.length() > 128)
            throw new IllegalArgumentException("Invalid extension call identity.");
        arguments = requireObject(arguments);
    }

    public static JsonNode requireObject(JsonNode node) {
        Objects.requireNonNull(node);
        if (!node.isObject() || node.toString().getBytes(StandardCharsets.UTF_8).length > 16384)
            throw new IllegalArgumentException("Invalid extension data shape or size.");
        checkDepth(node, 0);
        return node.deepCopy();
    }

    private static void checkDepth(JsonNode node, int depth) {
        if (depth > 24) throw new IllegalArgumentException("Extension data is too deep.");
        for (var child : node) checkDepth(child, depth + 1);
    }

    @Override public JsonNode arguments() { return arguments.deepCopy(); }
}
