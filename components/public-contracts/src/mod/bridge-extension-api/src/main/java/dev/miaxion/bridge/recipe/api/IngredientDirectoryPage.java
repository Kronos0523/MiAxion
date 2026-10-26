package dev.miaxion.bridge.recipe.api;
import java.util.List;
public record IngredientDirectoryPage(List<IngredientDirectoryEntry> matches, boolean hasMore, boolean incomplete) {
    public IngredientDirectoryPage { matches = List.copyOf(matches); if (matches.size() > 8) throw new IllegalArgumentException(); }
}
