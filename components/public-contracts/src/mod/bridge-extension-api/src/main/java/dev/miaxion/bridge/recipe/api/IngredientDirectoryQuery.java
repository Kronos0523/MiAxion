package dev.miaxion.bridge.recipe.api;

import java.util.List;
import java.util.Objects;
/** One on-demand page. No native browser type or inventory quantity crosses this boundary. */
public record IngredientDirectoryQuery(String query, String kind, List<String> namespaces, int offset) {
    public IngredientDirectoryQuery {
        Objects.requireNonNull(query); Objects.requireNonNull(kind);
        namespaces = List.copyOf(namespaces);
        if (query.isBlank() || query.length() > 2048 || offset < 0 || offset % 8 != 0
            || !List.of("item", "fluid", "custom", "any").contains(kind)) throw new IllegalArgumentException("Invalid ingredient query");
    }
}
