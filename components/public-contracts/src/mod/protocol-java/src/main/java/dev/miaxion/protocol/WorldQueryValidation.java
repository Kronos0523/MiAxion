package dev.miaxion.protocol;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;

final class WorldQueryValidation {
    private static final Pattern WARNING = Pattern.compile("^[A-Z][A-Z0-9_]*$");

    private WorldQueryValidation() {}

    static void requirePageCursor(String value, boolean allowStart) {
        if (value == null || (allowStart && value.isEmpty())) return;
        if (!value.matches("[0-9a-f]{32}")) throw new IllegalArgumentException("Invalid world page cursor.");
    }

    static void requireContinuation(WorldQuerySummary summary, String value) {
        requirePageCursor(value, false);
        if (value != null && (summary.complete() || !summary.truncated()))
            throw new IllegalArgumentException("A continuation requires an incomplete result page.");
    }

    static void requireDisplayName(String value, String field) {
        Objects.requireNonNull(value, field);
        EntityTextValidation.require(value, field);
        int scalars = value.codePointCount(0, value.length());
        if (scalars < 1 || scalars > 256) {
            throw new IllegalArgumentException(field + " is outside its bounds.");
        }
    }

    static void requireWarning(String value, String field) {
        Objects.requireNonNull(value, field);
        if (value.length() < 1 || value.length() > 64 || !WARNING.matcher(value).matches()) {
            throw new IllegalArgumentException(field + " must be an uppercase warning code.");
        }
    }

    static <T> List<T> copyUnique(
            List<T> values,
            int maximum,
            Function<T, String> key,
            String field) {
        Objects.requireNonNull(values, field);
        if (values.size() > maximum || values.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException(field + " exceeds its result bounds.");
        }
        Set<String> seen = new HashSet<>();
        for (T value : values) {
            if (!seen.add(key.apply(value))) {
                throw new IllegalArgumentException(field + " contains duplicate identities.");
            }
        }
        return List.copyOf(values);
    }

    static void requireNearbySummary(WorldQuerySummary summary) {
        Objects.requireNonNull(summary, "summary");
        if (summary.authority() != WorldQueryAuthority.LOADED_OBSERVED) {
            throw new IllegalArgumentException("Nearby responses require loaded authority.");
        }
    }

    static String requireRevision(String value) {
        QuestContractValidation.requireOpaqueId(value, "revision");
        return value;
    }
}
