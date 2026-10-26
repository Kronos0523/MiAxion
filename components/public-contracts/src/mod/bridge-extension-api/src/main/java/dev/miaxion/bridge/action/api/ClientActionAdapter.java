package dev.miaxion.bridge.action.api;

/**
 * Trusted client-mod adapter, not a sandbox and not an authorization issuer.
 * Both methods run on the client thread. Availability must be read-only;
 * dispatch performs one short synchronous semantic action (no queue/retry,
 * physical input broadcast, held-state simulation or server-player selection).
 * Bridge calls dispatch only after its dedicated grant/ledger checks.
 * Registration does not bypass the host's current execution policy. The adapter
 * does not introduce its own pairing, authorization or automatic retry flow.
 */
public interface ClientActionAdapter {
    Availability availability(String actionId);
    void dispatch(String actionId);
    /** Closed Ars page surface; no arbitrary protocol targets or authorization port. */
    default java.util.List<ArsPage> arsPages() { return java.util.List.of(); }
    default Availability arsPageAvailability(int pageOneBased) { return Availability.UNSUPPORTED; }
    default void dispatchArsPage(int pageOneBased) { throw new IllegalStateException("ACTION_UNAVAILABLE"); }
    record ArsPage(int pageOneBased, String displayName) {}
    /** Closed Iron surface; persistent identity is the exact spell ID, never the native index. */
    default java.util.List<IronSpell> ironSpells() { return java.util.List.of(); }
    default Availability ironSpellAvailability(String namespacedSpellId) { return Availability.UNSUPPORTED; }
    default void dispatchIronSpell(String namespacedSpellId) { throw new IllegalStateException("ACTION_UNAVAILABLE"); }
    record IronSpell(String namespacedSpellId, String displayName) {}
    enum Availability { AVAILABLE, UNAVAILABLE, UNSUPPORTED }
}
