package dev.miaxion.bridge.action.api;

/** Loader-independent startup registration; no execution/authorization port. */
@FunctionalInterface
public interface ClientActionRegistrar {
    Result register(ClientActionRegistration registration);
    enum Result { ACCEPTED, FROZEN, INVALID, UNSUPPORTED_API, DUPLICATE }
}
