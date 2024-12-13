package me.pepperbell.ctm.client;

import me.pepperbell.ctm.client.util.biome.BiomeHolderManager;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

@EventBusSubscriber(modid = ContinuityClient.ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ContinuityClientGameEvents {

    @SubscribeEvent
    public static void onClientPlayerLoggedIn(ClientPlayerNetworkEvent.LoggingIn event) {
        if (Minecraft.getInstance().level == null) {
            return;
        }

        BiomeHolderManager.registryManager = Minecraft.getInstance().level.registryAccess();
        BiomeHolderManager.refreshHolders();
    }
}
