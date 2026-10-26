package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Metadata search only. An empty query lists the selected live registry. */
public record WorldRegistrySearchRequest(
    @JsonProperty(required = true) WorldRegistryKind kind,
    @JsonProperty(required = true) String query,
    @JsonProperty(required = true) int pageSize,
    String namespace,
    String cursor) {
    public WorldRegistrySearchRequest {
        if (kind == null || query == null || query.codePointCount(0, query.length()) > 512
            || query.codePoints().anyMatch(Character::isISOControl)) throw new IllegalArgumentException("Invalid registry query");
        if (pageSize < 1 || pageSize > 64) throw new IllegalArgumentException("Invalid registry page size");
        if (namespace != null && !namespace.matches("[a-z0-9_.-]{1,128}")) throw new IllegalArgumentException("Invalid namespace");
        if (cursor != null && !cursor.matches("[a-f0-9]{32}")) throw new IllegalArgumentException("Invalid registry cursor");
    }
}
