package dev.miaxion.protocol;

final class EntityTextValidation {
    private EntityTextValidation() {}

    static void require(String value, String field) {
        for (int index = 0; index < value.length(); index++) {
            char current = value.charAt(index);
            if (Character.isHighSurrogate(current)) {
                if (index + 1 >= value.length()
                    || !Character.isLowSurrogate(value.charAt(index + 1))) {
                    throw new IllegalArgumentException(
                        field + " contains an invalid Unicode scalar.");
                }
                index++;
                continue;
            }
            if (Character.isLowSurrogate(current)) {
                throw new IllegalArgumentException(
                    field + " contains an invalid Unicode scalar.");
            }
            int codePoint = current;
            if ((codePoint >= 0 && codePoint <= 0x1f)
                || (codePoint >= 0x7f && codePoint <= 0x9f)
                || codePoint == 0x061c
                || codePoint == 0x200e
                || codePoint == 0x200f
                || (codePoint >= 0x202a && codePoint <= 0x202e)
                || (codePoint >= 0x2066 && codePoint <= 0x2069)) {
                throw new IllegalArgumentException(
                    field + " contains a forbidden control character.");
            }
        }
    }
}
