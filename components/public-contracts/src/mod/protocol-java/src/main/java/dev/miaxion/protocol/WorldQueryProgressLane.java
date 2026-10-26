package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;

/** The only scheduler lanes whose bounded progress may cross the bridge. */
public enum WorldQueryProgressLane {
    @JsonProperty("locator") LOCATOR,
    @JsonProperty("nearby") NEARBY
}
