package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public record ModVersionsResolveRequest(
    @JsonProperty(required = true) List<String> modIds
) {
    public static final int MAXIMUM_MOD_IDS = 32;
    private static final Pattern MOD_ID = Pattern.compile("^[a-z0-9_.-]+$");

    public ModVersionsResolveRequest {
        Objects.requireNonNull(modIds, "modIds");
        if (modIds.isEmpty() || modIds.size() > MAXIMUM_MOD_IDS || modIds.stream().anyMatch(Objects::isNull))
            throw new IllegalArgumentException("Mod IDs must contain 1-32 exact identifiers.");
        var seen = new HashSet<String>();
        for (String modId : modIds) {
            if (modId.length() > 64 || !MOD_ID.matcher(modId).matches() || !seen.add(modId))
                throw new IllegalArgumentException("Mod IDs must be unique lowercase identifiers.");
        }
        modIds = List.copyOf(modIds);
    }
}
