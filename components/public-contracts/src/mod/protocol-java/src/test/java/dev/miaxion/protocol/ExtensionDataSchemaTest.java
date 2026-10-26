package dev.miaxion.protocol;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

final class ExtensionDataSchemaTest {
    @Test void object_contract_rejects_unknown_fields_and_does_not_apply_defaults() {
        var schema = new ExtensionDataSchema("""
            {"type":"object","properties":{"count":{"type":"integer","minimum":1,"default":2}},
             "required":["count"],"additionalProperties":false}
            """);
        assertTrue(schema.accepts("{\"count\":1}"));
        assertFalse(schema.accepts("{}"));
        assertFalse(schema.accepts("{\"count\":0}"));
        assertFalse(schema.accepts("{\"count\":1,\"other\":true}"));
        assertFalse(schema.accepts("{\"count\":1,\"count\":2}"));
        assertFalse(schema.accepts("{\"count\":1} {}"));
    }

    @Test void local_refs_work_but_remote_recursive_and_unknown_assertions_are_rejected() {
        var schema = new ExtensionDataSchema("""
            {"type":"object","$defs":{"name":{"type":"string"}},
             "properties":{"name":{"$ref":"#/$defs/name"}}}
            """);
        assertTrue(schema.accepts("{\"name\":\"fixture\"}"));
        assertFalse(schema.accepts("{\"name\":1}"));
        for (var value : new String[] {
            "{\"type\":\"object\",\"$ref\":\"https://invalid.example/schema\"}",
            "{\"type\":\"object\",\"$ref\":\"#\"}",
            "{\"type\":\"object\",\"format\":\"unknown\"}",
            "{\"type\":\"object\",\"properties\":{\"x\":{\"type\":\"bogus\"}}}",
            "{\"type\":\"object\",\"required\":[\"x\",\"x\"]}" }) {
            assertThrows(IllegalArgumentException.class, () -> new ExtensionDataSchema(value));
        }
    }

    @Test void byte_depth_and_unicode_bounds_apply_before_validation() {
        var schema = new ExtensionDataSchema("{\"type\":\"object\"}");
        assertFalse(schema.accepts("{\"x\":\"" + "x".repeat(16384) + "\"}"));
        assertFalse(schema.accepts("{\"x\":".repeat(25) + "0" + "}".repeat(25)));
        assertFalse(schema.accepts("{\"x\":\"\\ud800\"}"));
        assertFalse(schema.accepts("{\"\\ud800\":1}"));
        assertFalse(schema.accepts("[]"));
    }
}
