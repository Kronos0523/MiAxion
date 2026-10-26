package dev.miaxion.bridge.forge.api;
import dev.miaxion.bridge.neoforge.api.TerminologyBrowserProvider;
/** One targeted browser source; no transport, settings or execution authority. */
@FunctionalInterface
public interface ForgeTerminologyBrowserEntrypoint {
    String IMC_METHOD = "miaxion:terminology_browser";
    TerminologyBrowserProvider create();
}
