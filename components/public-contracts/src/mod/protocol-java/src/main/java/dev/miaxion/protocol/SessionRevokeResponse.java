package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SessionRevokeResponse(
        @JsonProperty(required = true) boolean revoked) {
}
