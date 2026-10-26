package dev.miaxion.protocol;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ItemDetailsProtocolTest {
    @Test void only_versioned_event_accepts_details_and_the_old_contract_stays_closed() {
        var details = new TerminologyPayloads.ItemDetails(new TerminologyPayloads.Identity("item", "minecraft:potion"),
            "Potion", "hotbar", "2026-10-01T00:00:00Z", "complete", List.of("Pack author text"), "complete",
            List.of(new TerminologyPayloads.Effect("minecraft:speed", 0, "timed", 200)));
        var snapshot = new TerminologyPayloads.Snapshot(1, 1, "en_us",
            new TerminologyPayloads.Settings(true, List.of("hotbar"), 64, 180, 1800), List.of(), List.of(), Map.of(), false, details);
        var validator = new ProtocolValidator();
        assertDoesNotThrow(() -> validator.serializeTerminologyChanged(new TerminologyPayloads.ChangedEnvelope("1.0",
            KnownProtocolAction.TERMINOLOGY_V2_CHANGED.action(), snapshot)));
        assertThrows(ProtocolValidationFailure.class, () -> validator.serializeTerminologyChanged(new TerminologyPayloads.ChangedEnvelope("1.0",
            KnownProtocolAction.TERMINOLOGY_CHANGED.action(), snapshot)));
        assertDoesNotThrow(() -> validator.serializeTerminologyChanged(new TerminologyPayloads.ChangedEnvelope("1.0",
            KnownProtocolAction.TERMINOLOGY_CHANGED.action(), snapshot.legacy())));
        assertThrows(IllegalArgumentException.class, () -> new TerminologyPayloads.ItemDetails(details.identity(), "Potion", "hotbar",
            details.observedAtUtc(), "complete", List.of("😀".repeat(2049)), "complete", List.of()));
    }
}
