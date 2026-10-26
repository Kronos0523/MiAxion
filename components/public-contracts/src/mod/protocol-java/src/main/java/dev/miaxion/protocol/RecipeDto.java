package dev.miaxion.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public record RecipeDto(
        @JsonProperty(required = true) String recipeId,
        @JsonProperty(required = true) String type,
        @JsonProperty(required = true) String sourceModId,
        @JsonProperty(required = true) RecipeIdentityDto identity,
        @JsonProperty(required = true) List<RecipeSlotDto> inputs,
        @JsonProperty(required = true) List<RecipeSlotDto> catalysts,
        @JsonProperty(required = true) List<RecipeSlotDto> outputs,
        @JsonInclude(JsonInclude.Include.NON_NULL) RecipeProcessDto process,
        @JsonInclude(JsonInclude.Include.NON_NULL) RecipeClassificationDto classification) {
    private static final int RESOURCE_LOCATION_MAX_LENGTH = 128;
    private static final int NAMESPACE_MAX_LENGTH = 64;
    private static final Pattern RESOURCE_LOCATION_PATTERN = Pattern.compile(
        "^[a-z0-9_.-]+:[a-z0-9_./-]+$");
    private static final Pattern NAMESPACE_PATTERN = Pattern.compile(
        "^[a-z0-9_.-]+$");

    public RecipeDto(
        String recipeId,
        String type,
        String sourceModId,
        List<RecipeSlotDto> inputs,
        List<RecipeSlotDto> catalysts,
        List<RecipeSlotDto> outputs
    ) {
        this(recipeId, type, sourceModId, RecipeIdentityDto.registered(),
            inputs, catalysts, outputs, null, null);
    }

    public RecipeDto(
        String recipeId,
        String type,
        String sourceModId,
        RecipeIdentityDto identity,
        List<RecipeSlotDto> inputs,
        List<RecipeSlotDto> catalysts,
        List<RecipeSlotDto> outputs
    ) {
        this(recipeId, type, sourceModId, identity, inputs, catalysts, outputs, null, null);
    }

    public RecipeDto(
        String recipeId,
        String type,
        String sourceModId,
        RecipeIdentityDto identity,
        List<RecipeSlotDto> inputs,
        List<RecipeSlotDto> catalysts,
        List<RecipeSlotDto> outputs,
        RecipeProcessDto process
    ) {
        this(recipeId, type, sourceModId, identity, inputs, catalysts, outputs, process, null);
    }

    public RecipeDto {
        requireResourceLocation(recipeId, "recipeId");
        requireResourceLocation(type, "type");
        Objects.requireNonNull(sourceModId, "sourceModId");
        Objects.requireNonNull(identity, "identity");
        // process is optional by design; its constructor owns closed validation.
        if (sourceModId.length() > NAMESPACE_MAX_LENGTH
            || !NAMESPACE_PATTERN.matcher(sourceModId).matches()) {
            throw new IllegalArgumentException(
                "sourceModId must be a valid namespace.");
        }
        if (identity.isRegistered() && recipeId.startsWith("miaxion:derived/")) {
            throw new IllegalArgumentException(
                "derived recipe IDs require a derived identity.");
        }
        if (identity.isDerived() && !recipeId.equals(
            "miaxion:derived/" + identity.contentSha256())) {
            throw new IllegalArgumentException(
                "derived recipe ID and identity hash differ.");
        }
        inputs = copyWithinLimit(inputs, ProtocolConstants.RECIPE_INPUT_SLOTS_MAX, "inputs");
        catalysts = copyWithinLimit(catalysts, ProtocolConstants.RECIPE_CATALYST_SLOTS_MAX, "catalysts");
        outputs = copyWithinLimit(outputs, ProtocolConstants.RECIPE_OUTPUT_SLOTS_MAX, "outputs");
    }

    private static List<RecipeSlotDto> copyWithinLimit(
        List<RecipeSlotDto> values,
        int maxSize,
        String name
    ) {
        Objects.requireNonNull(values, name);
        if (values.size() > maxSize) {
            throw new IllegalArgumentException(
                name + " exceeds its maximum size.");
        }
        return List.copyOf(values);
    }

    private static void requireResourceLocation(
        String value,
        String name
    ) {
        Objects.requireNonNull(value, name);
        if (value.length() > RESOURCE_LOCATION_MAX_LENGTH
            || !RESOURCE_LOCATION_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException(
                name + " must be a valid resource location.");
        }
    }
}
