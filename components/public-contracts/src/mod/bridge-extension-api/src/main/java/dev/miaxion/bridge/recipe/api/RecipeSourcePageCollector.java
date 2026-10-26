package dev.miaxion.bridge.recipe.api;

import java.util.List;
import java.util.Objects;
import java.util.TreeMap;

/** Stable ordering and bounded continuation helper for public registry traversal. */
public final class RecipeSourcePageCollector {
    private final int capacity; private final String after; private final TreeMap<String, RecipeCandidate> retained = new TreeMap<>(); private boolean more;
    public RecipeSourcePageCollector(int capacity, String after) { if (capacity < 1 || capacity > RecipeProviderResult.MAX_SOURCE_RECIPES) throw new IllegalArgumentException("Invalid page capacity."); validateKey(after); this.capacity = capacity; this.after = after; }
    public static void validateKey(String value) { if (value != null && !value.matches("[a-z0-9_.-]+:[a-z0-9_./-]+\\|[a-z0-9_.-]+:[a-z0-9_./-]+")) throw new IllegalArgumentException("Invalid source continuation key."); }
    public boolean shouldProject(String type, String id) { if (id == null) return true; String key = type + '|' + id; return (after == null || key.compareTo(after) > 0) && !retained.containsKey(key); }
    public void add(RecipeCandidate candidate) { Objects.requireNonNull(candidate); String key = candidate.typeId() + '|' + candidate.recipeId(); if ((after != null && key.compareTo(after) <= 0) || retained.containsKey(key)) return; if (retained.size() == capacity) { more = true; if (key.compareTo(retained.lastKey()) >= 0) return; retained.pollLastEntry(); } retained.put(key, candidate); }
    /** Number of candidates retained for the current bounded source page. */
    public int size() { return retained.size(); }
    public RecipeProviderResult finish(boolean incomplete) { return finish(incomplete, null); }
    public RecipeProviderResult finish(boolean incomplete, RecipeProviderDiagnostics diagnostics) {
        return new RecipeProviderResult(List.copyOf(retained.values()), more || incomplete,
            more ? retained.lastKey() : null, diagnostics, incomplete);
    }
}
