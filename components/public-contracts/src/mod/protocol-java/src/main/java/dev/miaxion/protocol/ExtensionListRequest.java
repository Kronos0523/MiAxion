package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ExtensionListRequest(
    @JsonProperty(required = true) String cursor,
    @JsonProperty(required = true) String catalogRevision
) {
    public ExtensionListRequest {
        if ((cursor == null) != (catalogRevision == null)) throw new IllegalArgumentException("Cursor requires its revision.");
        if (cursor != null && (cursor.isBlank() || cursor.length() > 128 || catalogRevision.isBlank() || catalogRevision.length() > 128))
            throw new IllegalArgumentException("Invalid extension cursor.");
    }
}
