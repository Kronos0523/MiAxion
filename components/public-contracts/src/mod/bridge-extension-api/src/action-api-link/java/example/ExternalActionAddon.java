package example;

import dev.miaxion.bridge.action.api.ClientActionAdapter;
import dev.miaxion.bridge.action.api.ClientActionRegistration;
import dev.miaxion.bridge.neoforge.api.NeoForgeClientActionEntrypoint;
import dev.miaxion.bridge.neoforge.api.NeoForgeClientActionRegistrar;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Standalone contract fixture, not a game mod or a game-action authorization path. */
public final class ExternalActionAddon {
    public static void main(String[] args) throws Exception {
        var actual = Path.of(ClientActionRegistration.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toRealPath();
        if (args.length != 1 || !actual.equals(Path.of(args[0]).toRealPath()))
            throw new AssertionError("Unexpected runtime API origin");
        int[] calls = {0};
        ClientActionAdapter adapter = new ClientActionAdapter() {
            public Availability availability(String id) { return id.equals("sample") ? Availability.AVAILABLE : Availability.UNSUPPORTED; }
            public void dispatch(String id) { if (!id.equals("sample")) throw new IllegalArgumentException(); calls[0]++; }
        };
        // Retain the original static-action constructor. New host features must
        // not force old addons to call a different constructor or implement new methods.
        var registration = new ClientActionRegistration("sample_addon", "7.4.2", 1,
            "sample_mod", Map.of("sample_mod", Set.of("2.0")),
            List.of(new ClientActionRegistration.Action("sample", "Sample")), () -> adapter);
        NeoForgeClientActionEntrypoint entrypoint = registrar -> {
            if (registrar.register(registration) != NeoForgeClientActionRegistrar.RegistrationResult.ACCEPTED)
                throw new AssertionError("Registration failed");
        };
        entrypoint.register(value -> {
            if (value.apiMajor() != 1 || value.catalogKind() != ClientActionRegistration.CatalogKind.STATIC_KEYS)
                throw new AssertionError("Registration contract changed");
            if (value.factory().get().availability("sample") != ClientActionAdapter.Availability.AVAILABLE)
                throw new AssertionError("Availability contract changed");
            value.factory().get().dispatch("sample");
            return NeoForgeClientActionRegistrar.RegistrationResult.ACCEPTED;
        });
        if (calls[0] != 1 || !adapter.arsPages().isEmpty() || !adapter.ironSpells().isEmpty() ||
            adapter.arsPageAvailability(1) != ClientActionAdapter.Availability.UNSUPPORTED ||
            adapter.ironSpellAvailability("sample:spell") != ClientActionAdapter.Availability.UNSUPPORTED)
            throw new AssertionError("Default optional surface changed");
        System.out.println("ACTION_API_LINK_OK apiMajor=1 callbacks=1 runtime=" + actual.getFileName());
    }
}
