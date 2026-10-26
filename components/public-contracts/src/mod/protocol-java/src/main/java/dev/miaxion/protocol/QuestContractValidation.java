package dev.miaxion.protocol;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

final class QuestContractValidation {
    private static final Pattern OPAQUE_ID = Pattern.compile(
        "^[A-Za-z0-9_.:-]+$");
    private static final Pattern RESOURCE_LOCATION = Pattern.compile(
        "^[a-z0-9_.-]+:[a-z0-9_./-]+$");

    private QuestContractValidation() { }

    static void requireContinuation(String value) {
        if (value != null && (value.isEmpty() || value.length() > 256 || !value.matches("[A-Za-z0-9_-]+"))) {
            throw new IllegalArgumentException("Quest continuation is invalid.");
        }
    }

    static void requireOpaqueId(String value, String description) {
        requireText(value, 1, 64, description);
        if (!OPAQUE_ID.matcher(value).matches()) {
            throw new IllegalArgumentException(
                description + " must be a safe opaque ID.");
        }
    }

    static void requireResourceLocation(String value, String description) {
        requireText(value, 1, 128, description);
        if (!RESOURCE_LOCATION.matcher(value).matches()) {
            throw new IllegalArgumentException(
                description + " must be a resource location.");
        }
    }

    static void requireTitle(String value, String description) {
        requireText(
            value, 0, ProtocolConstants.QUEST_TITLE_SCALARS_MAX,
            description);
    }

    static void requireText(
        String value,
        int minimumScalars,
        int maximumScalars,
        String description
    ) {
        Objects.requireNonNull(value, description);
        int scalars = value.codePointCount(0, value.length());
        if (scalars < minimumScalars || scalars > maximumScalars) {
            throw new IllegalArgumentException(
                description + " exceeds its scalar bounds.");
        }
    }

    static void requireProgress(double value, String description) {
        if (!Double.isFinite(value) || value < 0 || value > 1) {
            throw new IllegalArgumentException(
                description + " must be between zero and one.");
        }
    }

    static <T> List<T> copyWithinBounds(
        List<T> values,
        int maximum,
        String description
    ) {
        Objects.requireNonNull(values, description);
        if (values.size() > maximum || values.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException(
                description + " exceeds its collection bounds.");
        }
        return List.copyOf(values);
    }

    static List<String> copyDescription(
        List<String> values,
        String description
    ) {
        List<String> copy = copyWithinBounds(
            values, ProtocolConstants.QUEST_DETAIL_COLLECTION_MAX,
            description);
        for (String line : copy) {
            requireText(
                line, 0, ProtocolConstants.QUEST_DESCRIPTION_LINE_SCALARS_MAX,
                description);
        }
        return copy;
    }
}
