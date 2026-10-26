package dev.miaxion.bridge.forge.api;
/** Send during Forge IMC enqueue; the host binds the actual sender before registration freeze. */
@FunctionalInterface
public interface ForgeRecipeProviderEntrypoint {
    String IMC_METHOD = "miaxion:recipe_provider";
    void register(ForgeRecipeProviderRegistrar registrar);
}
