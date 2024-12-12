package me.pepperbell.continuity.client;

import me.pepperbell.continuity.client.resource.CustomBlockLayers;
import me.pepperbell.continuity.client.util.RenderUtil;
import me.pepperbell.continuity.client.util.biome.BiomeHolderManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.event.AddPackFindersEvent;

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
