package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

public record WorldQueryProgressRequest(
        @JsonProperty(required = true) WorldQueryProgressLane lane) {
    public WorldQueryProgressRequest {
        Objects.requireNonNull(lane, "lane");
    }
}
