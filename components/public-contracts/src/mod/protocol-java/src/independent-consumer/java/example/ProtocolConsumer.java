package example;
import dev.miaxion.protocol.SchemaRegistry;
import dev.miaxion.protocol.ProtocolRuntimeLimitsLoader;
import java.nio.file.Path;
public final class ProtocolConsumer {
    public static void main(String[] args) throws Exception {
        var actual = Path.of(SchemaRegistry.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toRealPath();
        if (!actual.equals(Path.of(args[0]).toRealPath())) throw new AssertionError("Wrong protocol source");
        if (SchemaRegistry.load() == null || ProtocolRuntimeLimitsLoader.load().schemaVersion() != 1)
            throw new AssertionError("Missing packaged resources or runtime dependencies");
        System.out.println("independent protocol consumer passed");
    }
}
