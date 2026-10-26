package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record CapabilitiesListResponse(
        @JsonProperty(required = true)
        List<CapabilityDescriptor> capabilities) {

    public CapabilitiesListResponse {
        capabilities = List.copyOf(capabilities);
    }
}
