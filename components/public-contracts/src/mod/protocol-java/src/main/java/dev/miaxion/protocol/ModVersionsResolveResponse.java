package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.regex.Pattern;

public record ModVersionsResolveResponse(
    @JsonProperty(required = true) Map<String, String> versions
) {
    private static final Pattern MOD_ID = Pattern.compile("^[a-z0-9_.-]+$");

    public ModVersionsResolveResponse {
        Objects.requireNonNull(versions, "versions");
        if (versions.size() > ModVersionsResolveRequest.MAXIMUM_MOD_IDS)
            throw new IllegalArgumentException("Resolved Mod versions exceed the closed contract.");
        var copy = new TreeMap<String, String>();
        for (var entry : versions.entrySet()) {
            String modId = Objects.requireNonNull(entry.getKey(), "version Mod ID");
            String version = Objects.requireNonNull(entry.getValue(), "version");
            if (modId.length() > 64 || !MOD_ID.matcher(modId).matches() ||
                version.isEmpty() || version.codePointCount(0, version.length()) > 128)
                throw new IllegalArgumentException("Resolved Mod versions are outside the closed contract.");
            EntityTextValidation.require(version, "version");
            if (copy.put(modId, version) != null)
                throw new IllegalArgumentException("Resolved Mod versions contain duplicate IDs.");
        }
        versions = Map.copyOf(copy);
    }
}
