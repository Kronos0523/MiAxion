package dev.miaxion.bridge.recipe.api;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.BigIntegerNode;
import com.fasterxml.jackson.databind.node.DecimalNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.FloatNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.LongNode;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** RFC-8785-style material encoding shared by public derived identities. */
public final class RecipeCanonicalJson {
    public static final int MAX_DEPTH = 64;
    public static final int MAX_UTF8_BYTES = 1_048_576;
    private static final ObjectMapper STRICT_JSON = new ObjectMapper(JsonFactory.builder().enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION).build());
    private RecipeCanonicalJson() { }
    public static byte[] canonicalUtf8(JsonNode value) { return canonicalUtf8(value, MAX_UTF8_BYTES); }
    public static byte[] canonicalUtf8(JsonNode value, int maxBytes) {
        if (value == null || value.isMissingNode() || maxBytes < 1) throw new IllegalArgumentException("JSON is not canonicalizable.");
        StringBuilder output = new StringBuilder(); append(value, output, 1);
        byte[] bytes = output.toString().getBytes(StandardCharsets.UTF_8);
        if (bytes.length > maxBytes) throw new IllegalArgumentException("Canonical JSON exceeds the byte limit.");
        return bytes;
    }
    private static void append(JsonNode value, StringBuilder output, int depth) {
        if (depth > MAX_DEPTH) throw new IllegalArgumentException("JSON depth exceeds the limit.");
        if (value.isObject()) { output.append('{'); List<String> names = new ArrayList<>(); value.fieldNames().forEachRemaining(names::add); names.sort(Comparator.naturalOrder()); for (int i = 0; i < names.size(); i++) { if (i > 0) output.append(','); String name = names.get(i); appendString(name, output); output.append(':'); append(value.get(name), output, depth + 1); } output.append('}'); return; }
        if (value.isArray()) { output.append('['); for (int i = 0; i < value.size(); i++) { if (i > 0) output.append(','); append(value.get(i), output, depth + 1); } output.append(']'); return; }
        if (value.isTextual()) appendString(value.textValue(), output);
        else if (value.isNumber()) output.append(canonicalNumber(value));
        else if (value.isBoolean()) output.append(value.booleanValue() ? "true" : "false");
        else if (value.isNull()) output.append("null");
        else throw new IllegalArgumentException("Unsupported JSON node.");
    }
    private static void appendString(String value, StringBuilder output) { try { output.append(STRICT_JSON.writeValueAsString(value)); } catch (JsonProcessingException impossible) { throw new IllegalArgumentException(impossible); } }
    private static String canonicalNumber(JsonNode value) {
        if (value instanceof DoubleNode || value instanceof FloatNode) if (!Double.isFinite(value.doubleValue())) throw new IllegalArgumentException("Non-finite numbers are forbidden.");
        if (value instanceof BigIntegerNode || value instanceof IntNode || value instanceof LongNode) { BigInteger integer = value.bigIntegerValue(); return integer.toString(); }
        BigDecimal decimal = value instanceof DecimalNode ? value.decimalValue() : BigDecimal.valueOf(value.doubleValue());
        if (decimal.signum() == 0) return "0"; decimal = decimal.stripTrailingZeros(); int exponent = decimal.precision() - decimal.scale() - 1;
        if (exponent >= -6 && exponent < 21) return decimal.toPlainString();
        BigDecimal mantissa = decimal.movePointLeft(exponent).stripTrailingZeros(); String coefficient = mantissa.toPlainString();
        if (coefficient.endsWith(".0")) coefficient = coefficient.substring(0, coefficient.length() - 2);
        return coefficient + "e" + (exponent >= 0 ? "+" : "") + exponent;
    }
}
