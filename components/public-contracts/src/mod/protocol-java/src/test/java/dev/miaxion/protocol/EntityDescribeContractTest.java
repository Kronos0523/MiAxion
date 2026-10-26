package dev.miaxion.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

final class EntityDescribeContractTest {
    @Test
    void entity_page_contract_roundtrips_and_rejects_invalid_continuations() throws Exception {
        var request = new EntityResolveRequest("Shared", java.util.List.of(), "item", "page_2-A");
        assertEquals("page_2-A", ProtocolJson.read(ProtocolJson.write(request), EntityResolveRequest.class).continuation());
        var matches = java.util.stream.IntStream.range(0, 8)
            .mapToObj(i -> new EntityResolveMatch("item", "mod:item_" + i, "item.shared", "Shared")).toList();
        var response = new EntityResolveResponse("r1", matches, true, "page_2-A");
        assertEquals("page_2-A", ProtocolJson.read(ProtocolJson.write(response), EntityResolveResponse.class).nextContinuation());
        org.junit.jupiter.api.Assertions.assertFalse(ProtocolJson.write(new EntityResolveResponse("r1", java.util.List.of(), false)).contains("Continuation"));
        assertThrows(IllegalArgumentException.class, () -> new EntityResolveResponse("r1", matches, false, "page_2-A"));
        assertThrows(IllegalArgumentException.class, () -> new EntityResolveResponse("r1", matches.subList(0, 1), true, "page_2-A"));
        for (var invalid : java.util.List.of("", "bad token", "token=", "中文", "a".repeat(257)))
            assertThrows(IllegalArgumentException.class, () -> new EntityResolveRequest("Shared", java.util.List.of(), "item", invalid));
    }

    @Test
    void resolve_kind_is_optional_without_changing_legacy_wire_shape()
        throws Exception {
        EntityResolveRequest legacy = new EntityResolveRequest(
            "机械压力机", java.util.List.of("create"));
        assertEquals("any", legacy.effectiveKind());
        String json = ProtocolJson.write(legacy);
        org.junit.jupiter.api.Assertions.assertFalse(json.contains("kind"));

        EntityResolveRequest restored = ProtocolJson.read(
            ProtocolJson.write(new EntityResolveRequest(
                "机械压力机", java.util.List.of(), "item")),
            EntityResolveRequest.class);
        assertEquals("item", restored.kind());
    }

    @Test
    void describe_request_rejects_any_and_duplicate_references() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new EntityReference("any", "create:mechanical_press"));
        EntityReference reference = new EntityReference(
            "item", "create:mechanical_press");
        assertThrows(
            IllegalArgumentException.class,
            () -> new EntityDescribeRequest(
                java.util.List.of(reference, reference)));
    }

    @Test
    void describe_response_rejects_overlapping_description_and_missing() {
        EntityReference reference = new EntityReference(
            "item", "create:mechanical_press");
        EntityDescription description = new EntityDescription(
            "item", "create:mechanical_press",
            "block.create.mechanical_press", "Mechanical Press");
        assertThrows(
            IllegalArgumentException.class,
            () -> new EntityDescribeResponse(
                "r1", java.util.List.of(description),
                java.util.List.of(reference)));
    }

    @Test
    void entity_names_reject_controls_and_keep_emoji_sequences() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new EntityDescription(
                "item", "create:mechanical_press",
                "block.create.mechanical_press", "line\nfeed"));
        assertThrows(
            IllegalArgumentException.class,
            () -> new EntityDescription(
                "item", "create:mechanical_press",
                "block.create.mechanical_press", "\u202ereversed"));
        assertThrows(
            IllegalArgumentException.class,
            () -> new EntityDescription(
                "item", "create:mechanical_press",
                "block.create.mechanical_press", "\ud800"));
        EntityDescription valid = new EntityDescription(
            "item", "create:mechanical_press",
            "block.create.mechanical_press", "🙂️‍🔧");
        assertEquals("🙂️‍🔧", valid.displayName());
    }
}
