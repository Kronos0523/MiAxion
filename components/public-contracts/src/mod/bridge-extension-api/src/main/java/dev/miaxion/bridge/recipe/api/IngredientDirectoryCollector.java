package dev.miaxion.bridge.recipe.api;

import java.text.Normalizer;
import java.util.*;
/** Bounded page accumulator over the browser's own directory, not a second database. */
public final class IngredientDirectoryCollector {
    private final IngredientDirectoryQuery query;
    private final List<IngredientDirectoryEntry> matches = new ArrayList<>();
    private final List<java.util.function.Supplier<com.fasterxml.jackson.databind.JsonNode>> variants = new ArrayList<>();
    private int best, count;
    private boolean incomplete;
    public IngredientDirectoryCollector(IngredientDirectoryQuery query) { this.query = query; }
    public void unavailableEntry() { incomplete = true; }
    public void accept(IngredientDirectoryEntry entry) { accept(entry, () -> entry.components()); }
    public void accept(IngredientDirectoryEntry entry, java.util.function.Supplier<com.fasterxml.jackson.databind.JsonNode> variant) {
        if (!query.kind().equals("any") && !query.kind().equals(entry.kind())) return;
        if (!query.namespaces().isEmpty() && !query.namespaces().contains(entry.resourceId().split(":", 2)[0])) return;
        String text = key(query.query()), name = key(entry.displayName());
        int score = entry.resourceId().equals(query.query()) ? 3 : name.equals(text) ? 2
            : text.codePointCount(0, text.length()) >= 1 && (name.contains(text) || key(entry.resourceId()).contains(text)) ? 1 : 0;
        if (score == 0 || score < best) return;
        if (score > best) { matches.clear(); variants.clear(); count = 0; best = score; }
        if (count++ >= query.offset() && matches.size() < 8) { matches.add(entry); variants.add(variant); }
    }
    public IngredientDirectoryPage finish() {
        if (query.offset() > 0 && count <= query.offset()) throw new IllegalArgumentException("Ingredient page changed");
        var projected = new ArrayList<IngredientDirectoryEntry>(matches.size());
        for (int i = 0; i < matches.size(); i++) {
            var entry = matches.get(i);
            com.fasterxml.jackson.databind.JsonNode components = null;
            try { components = variants.get(i).get(); }
            catch (RuntimeException | LinkageError failure) { incomplete = true; }
            if (components != null && components.toString().length() > 8192) { components = null; incomplete = true; }
            projected.add(new IngredientDirectoryEntry(entry.kind(), entry.resourceId(), entry.displayName(), entry.ingredientTypeUid(), components));
        }
        return new IngredientDirectoryPage(projected, count > query.offset() + matches.size(), incomplete);
    }

    private static String key(String value) { return Normalizer.normalize(value, Normalizer.Form.NFKC).toLowerCase(Locale.ROOT).strip(); }
}
