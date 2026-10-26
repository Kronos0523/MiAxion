package dev.miaxion.bridge.neoforge.api;

import dev.miaxion.bridge.action.api.ClientActionRegistration;

/** Startup-only registration, deliberately no execution or IntegratedServer port. */
public interface NeoForgeClientActionRegistrar {
    RegistrationResult register(ClientActionRegistration registration);
    enum RegistrationResult { ACCEPTED, FROZEN, INVALID, UNSUPPORTED_API, DUPLICATE }
}
