package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

public record SessionPairApproved(
        @JsonProperty(required = true) String sessionToken,
        @JsonProperty(required = true) String tokenType) {

    public SessionPairApproved {
        Objects.requireNonNull(sessionToken, "sessionToken");
        Objects.requireNonNull(tokenType, "tokenType");
    }
}
