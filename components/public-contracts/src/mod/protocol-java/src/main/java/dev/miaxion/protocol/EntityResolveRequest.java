package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record EntityResolveRequest(
        @JsonProperty(required = true) String query,
        @JsonProperty(required = true) List<String> namespaces,
        String kind,
        String continuation,
        String scope) {
    private static final Pattern NAMESPACE = Pattern.compile("^[a-z0-9_.-]+$");

    public EntityResolveRequest(String query, List<String> namespaces, String kind, String continuation) {
        this(query, namespaces, kind, continuation, null);
    }
    public boolean browserScope() { return "browser".equals(scope); }
    public EntityResolveRequest(String query, List<String> namespaces) {
        this(query, namespaces, null);
    }

    public EntityResolveRequest(String query, List<String> namespaces, String kind) {
        this(query, namespaces, kind, null);
    }

    public EntityResolveRequest {
        if (scope != null && !scope.equals("registry") && !scope.equals("browser")) throw new IllegalArgumentException("Invalid directory scope");
        Objects.requireNonNull(query, "query");
        Objects.requireNonNull(namespaces, "namespaces");
        if (kind != null && !kind.equals("item")
            && !kind.equals("block") && !kind.equals("fluid") && !kind.equals("any") && !("browser".equals(scope) && kind.equals("custom"))) {
            throw new IllegalArgumentException(
                "kind must be item, block, fluid, any, or omitted.");
        }
        if (scalarCount(query) < 1 || scalarCount(query) > 512) {
            throw new IllegalArgumentException("query must contain 1-512 Unicode scalars.");
        }
        if (namespaces.size() > 128 || namespaces.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("namespaces must contain at most 128 values.");
        }
        var seen = new HashSet<String>();
        for (String namespace : namespaces) {
            if (namespace.isEmpty() || namespace.length() > 64 ||
                !NAMESPACE.matcher(namespace).matches() || !seen.add(namespace)) {
                throw new IllegalArgumentException("namespaces must be unique lowercase identifiers.");
            }
        }
        namespaces = List.copyOf(namespaces);
        requireContinuation(continuation);
    }

    static void requireContinuation(String value) {
        if (value != null && (value.isEmpty() || value.length() > 256 || !value.matches("[A-Za-z0-9_-]+")))
            throw new IllegalArgumentException("Entity continuation is invalid.");
    }

    public String effectiveKind() {
        return kind == null ? "any" : kind;
    }

    private static int scalarCount(String value) {
        return value.codePointCount(0, value.length());
    }
}
