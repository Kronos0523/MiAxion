package dev.miaxion.bridge.neoforge.api;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Detached data for ONE observed stack, never a registry or arbitrary component projection. */
public record ObservedItemDetails(String tooltipStatus, List<String> tooltip,
                                  String effectsStatus, List<Effect> effects) {
    public static final int MAX_TOOLTIP_BYTES = 8192;
    public static final int MAX_EFFECTS = 32;
    public record Effect(String resourceId, int amplifier, String durationKind, Integer durationTicks) {
        public Effect {
            if (resourceId == null || resourceId.length() > 128 || !resourceId.matches("[a-z0-9_.-]+:[a-z0-9/._-]+")
                || amplifier < 0 || amplifier > 255
                || !List.of("timed", "instant", "infinite", "unknown").contains(durationKind)
                || (durationKind.equals("timed") ? durationTicks == null || durationTicks < 0 : durationTicks != null))
                throw new IllegalArgumentException("Invalid observed effect");
        }
    }
    public ObservedItemDetails {
        tooltip = List.copyOf(tooltip); effects = List.copyOf(effects);
        if (!List.of("complete", "truncated", "read_failed").contains(tooltipStatus)
            || !List.of("complete", "truncated", "component_absent", "read_failed").contains(effectsStatus)
            || tooltip.size() > 256 || tooltip.stream().mapToInt(s -> s.getBytes(StandardCharsets.UTF_8).length).sum() > MAX_TOOLTIP_BYTES
            || effects.size() > MAX_EFFECTS) throw new IllegalArgumentException("Invalid observed details");
    }
    /** Suppliers execute on the caller's game thread. A missing component is distinct from an empty component. */
    public static ObservedItemDetails capture(Supplier<List<String>> tooltipReader, Supplier<Iterable<Effect>> effectsReader) {
        List<String> lines = new ArrayList<>(); String tooltipState = "complete";
        try {
            int remaining = MAX_TOOLTIP_BYTES;
            outer: for (String line : tooltipReader.get()) {
                if (lines.size() == 256) { tooltipState = "truncated"; break; }
                StringBuilder bounded = new StringBuilder();
                for (int offset = 0; offset < line.length();) {
                    int cp = line.codePointAt(offset); String scalar = new String(Character.toChars(cp));
                    int bytes = scalar.getBytes(StandardCharsets.UTF_8).length;
                    if (bytes > remaining) { lines.add(bounded.toString()); tooltipState = "truncated"; break outer; }
                    bounded.append(scalar); remaining -= bytes; offset += Character.charCount(cp);
                }
                lines.add(bounded.toString());
            }
        } catch (RuntimeException | LinkageError failure) { tooltipState = "read_failed"; }
        List<Effect> effects = new ArrayList<>(); String effectsState = "complete";
        try {
            Iterable<Effect> values = effectsReader.get();
            if (values == null) effectsState = "component_absent";
            else for (Effect effect : values) {
                if (effects.size() == MAX_EFFECTS) { effectsState = "truncated"; break; }
                effects.add(effect);
            }
        } catch (RuntimeException | LinkageError failure) { effectsState = "read_failed"; }
        return new ObservedItemDetails(tooltipState, lines, effectsState, effects);
    }
}
