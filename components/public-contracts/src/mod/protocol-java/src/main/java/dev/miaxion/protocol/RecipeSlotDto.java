package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;

public record RecipeSlotDto(
        @JsonProperty(required = true)
        List<RecipeAlternativeDto> alternatives) {

    public RecipeSlotDto {
        Objects.requireNonNull(alternatives, "alternatives");
        if (alternatives.isEmpty()
            || alternatives.size() > ProtocolConstants.RECIPE_SLOT_ALTERNATIVES_MAX) {
            throw new IllegalArgumentException(
                "alternatives exceed the protocol limit.");
        }
        alternatives = List.copyOf(alternatives);
    }
}
