package dev.miaxion.bridge.neoforge.api;

/** Send once via IMC; the registrar is bound to the actual sending mod ID. */
@FunctionalInterface
public interface NeoForgeClientActionEntrypoint {
    String IMC_METHOD = "miaxion:client_action";
    void register(NeoForgeClientActionRegistrar registrar);
    /** Optional additive capability. Legacy implementations remain valid and return no pages. */
    default java.util.List<dev.miaxion.bridge.action.api.ClientActionConfiguration.Page> configurationPages() {
        return java.util.List.of();
    }
}
