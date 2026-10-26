package dev.miaxion.extension.example;

import dev.miaxion.bridge.extension.api.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import java.util.List;

@Mod(value = "miaxion_extension_example", dist = Dist.CLIENT)
public final class StandingBlockExtension {
    public StandingBlockExtension(IEventBus bus) {
        bus.addListener(InterModEnqueueEvent.class, event -> event.enqueueWork(() ->
            InterModComms.sendTo("miaxion_bridge", BridgeExtension.IMC_METHOD, StandingBlockExtension::registration)));
    }

    public static BridgeExtension registration() {
        return new BridgeExtension("miaxion_extension_example", "0.1.0", BridgeExtension.API_MAJOR, List.of(), List.of(
            new ExtensionCapability("miaxion_extension_example:standing_block", "1.0", "Standing block",
                "Reads only the loaded block directly under the local player; no coordinates or container data.",
                "{\"type\":\"object\",\"additionalProperties\":false}",
                "{\"type\":\"object\",\"properties\":{\"blockId\":{\"type\":\"string\",\"maxLength\":256}},\"required\":[\"blockId\"],\"additionalProperties\":false}",
                List.of(), ExtensionExecutionContext.CLIENT, 1000, 1024, (context, arguments) -> {
                    context.ensureCurrent();
                    var minecraft = Minecraft.getInstance();
                    if (!minecraft.isSameThread() || minecraft.player == null || minecraft.level == null)
                        throw new IllegalStateException("Game thread unavailable.");
                    var position = minecraft.player.blockPosition().below();
                    if (!minecraft.level.hasChunkAt(position)) throw new IllegalStateException("Chunk unavailable.");
                    var id = BuiltInRegistries.BLOCK.getKey(minecraft.level.getBlockState(position).getBlock()).toString();
                    context.ensureCurrent();
                    // Registry identifiers have a restricted alphabet; no player text is serialized.
                    return "{\"blockId\":\"" + id + "\"}";
                })));
    }
}
