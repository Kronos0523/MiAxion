package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum SessionRevokeReason {
    @JsonProperty("user_requested")
    USER_REQUESTED,

    @JsonProperty("app_shutdown")
    APP_SHUTDOWN
}
