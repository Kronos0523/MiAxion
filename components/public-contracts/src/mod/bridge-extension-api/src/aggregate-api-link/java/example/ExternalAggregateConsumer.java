package example;

import dev.miaxion.bridge.extension.api.*;
import dev.miaxion.bridge.action.api.ClientActionEntrypoint;
import dev.miaxion.bridge.recipe.api.RecipeProvider;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CancellationException;

/** Independent consumer: compiled and run with only the published aggregate SDK and its declared API dependency. */
public final class ExternalAggregateConsumer {
    public static void main(String[] args) throws Exception {
        var expected = Path.of(args[0]).toRealPath();
        for (var type : List.of(BridgeExtension.class, ClientActionEntrypoint.class, RecipeProvider.class)) {
            var actual = Path.of(type.getProtectionDomain().getCodeSource().getLocation().toURI()).toRealPath();
            if (!expected.equals(actual)) throw new AssertionError("API came from unexpected location: " + actual);
        }
        var registration = new BridgeExtension("miaxion_example", "0.1.0", BridgeExtension.API_MAJOR, List.of(), List.of());
        if (!registration.id().equals("miaxion_example")) throw new AssertionError("Descriptor identity");
        new ExtensionInvocation(Instant.now().plusSeconds(30), () -> true).ensureCurrent();
        try {
            new ExtensionInvocation(Instant.now().plusSeconds(30), () -> false).ensureCurrent();
            throw new AssertionError("Invalidated invocation accepted");
        } catch (CancellationException expectedCancellation) { }
        System.out.println("aggregate API independent consumer passed");
    }
}
