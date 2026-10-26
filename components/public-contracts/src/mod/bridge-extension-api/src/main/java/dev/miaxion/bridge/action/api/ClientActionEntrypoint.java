package dev.miaxion.bridge.action.api;

/** Fabric entrypoint or Forge IMC callback, bound by the host to the real sending Mod ID. */
@FunctionalInterface
public interface ClientActionEntrypoint {
    String ENTRYPOINT = "miaxion:client_action";
    void register(ClientActionRegistrar registrar);
    default java.util.List<ClientActionConfiguration.Page> configurationPages() { return java.util.List.of(); }
}
