package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/** One independently readable automation face and its non-empty slots. */
public record WorldContainerSide(
        @JsonProperty(required = true) String side,
        @JsonProperty(required = true) List<WorldContainerStack> stacks) {
    private static final List<String> ORDER =
        List.of("down", "up", "north", "south", "west", "east");
    public static final int MAX_STACKS = 256;

    public WorldContainerSide {
        if (!ORDER.contains(side)) {
            throw new IllegalArgumentException("Unknown container side.");
        }
        Objects.requireNonNull(stacks, "stacks");
        if (stacks.size() > MAX_STACKS || stacks.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("stacks exceeds its bounds.");
        }
        ArrayList<WorldContainerStack> sorted = new ArrayList<>(stacks);
        sorted.sort(Comparator.comparingInt(WorldContainerStack::slot));
        var seen = new HashSet<Integer>();
        for (WorldContainerStack stack : sorted) {
            if (!seen.add(stack.slot())) {
                throw new IllegalArgumentException("stacks contains duplicate slots.");
            }
        }
        stacks = List.copyOf(sorted);
    }

    static int order(String side) {
        return ORDER.indexOf(side);
    }
}
