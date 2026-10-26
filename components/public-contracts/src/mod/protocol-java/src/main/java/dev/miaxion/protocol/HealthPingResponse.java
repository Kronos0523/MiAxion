package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record HealthPingResponse(
        @JsonProperty(required = true) String nonce,
        @JsonProperty(required = true) boolean gameReady,
        @JsonProperty(required = true) String protocolVersion,
        @JsonProperty(required = false) Integer ordinaryRequestsPerMinute,
        @JsonProperty(required = false) Integer ordinaryRequestBurst,
        @JsonProperty(required = false) String ordinaryRequestRateSource) {

    public HealthPingResponse(String nonce, boolean gameReady, String protocolVersion) {
        this(nonce, gameReady, protocolVersion, null, null, null);
    }

    public HealthPingResponse {
        Objects.requireNonNull(nonce, "nonce");
        Objects.requireNonNull(protocolVersion, "protocolVersion");
        boolean complete = ordinaryRequestsPerMinute != null && ordinaryRequestBurst != null && ordinaryRequestRateSource != null;
        if ((ordinaryRequestsPerMinute != null || ordinaryRequestBurst != null || ordinaryRequestRateSource != null) && !complete) {
            throw new IllegalArgumentException("Ordinary request rate metadata must be complete or absent.");
        }
        if (ordinaryRequestsPerMinute != null && (ordinaryRequestsPerMinute <= 0 || ordinaryRequestsPerMinute > 10000)) {
            throw new IllegalArgumentException("Ordinary request rate is outside the safe range.");
        }
        if (ordinaryRequestBurst != null && (ordinaryRequestBurst <= 0 || ordinaryRequestBurst > 10000)) {
            throw new IllegalArgumentException("Ordinary request burst is outside the safe range.");
        }
        if (ordinaryRequestRateSource != null && !ordinaryRequestRateSource.equals("protocol-default") &&
            !ordinaryRequestRateSource.equals("neoforge-config") && !ordinaryRequestRateSource.equals("test-default")) {
            throw new IllegalArgumentException("Ordinary request rate source is not recognized.");
        }
    }
}
