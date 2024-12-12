package me.pepperbell.continuity.client;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import me.pepperbell.continuity.client.mixinterface.ModelLoaderExtension;
import me.pepperbell.continuity.client.resource.CustomBlockLayers;
import me.pepperbell.continuity.client.resource.ModelWrappingHandler;
import me.pepperbell.continuity.client.util.RenderUtil;
import me.pepperbell.continuity.impl.client.ProcessingDataKeyRegistryImpl;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.event.AddPackFindersEvent;

import java.util.Map;
import java.util.Set;

@EventBusSubscriber(modid = ContinuityClient.ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ContinuityClientModEvents {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        ProcessingDataKeyRegistryImpl.INSTANCE.setFrozen();
    }

    @SubscribeEvent
    public static void onAddPackFinders(AddPackFindersEvent event) {
        event.addPackFinders(ContinuityClient.asId("resourcepacks/default"), PackType.CLIENT_RESOURCES, Component.translatable("resourcePack.continuity.default.name"), PackSource.BUILT_IN, false, Pack.Position.TOP);
        event.addPackFinders(ContinuityClient.asId("resourcepacks/glass_pane_culling_fix"), PackType.CLIENT_RESOURCES, Component.translatable("resourcePack.continuity.glass_pane_culling_fix.name"), PackSource.BUILT_IN, false, Pack.Position.TOP);
    }

    @SubscribeEvent
    public static void onRegisterResourceReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(RenderUtil.ReloadListener.INSTANCE);
        event.registerReloadListener(CustomBlockLayers.ReloadListener.INSTANCE);
    }

    @SubscribeEvent
    public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        ModelWrappingHandler wrappingHandler = ((ModelLoaderExtension) event.getModelBakery()).continuity$getModelWrappingHandler();

        if (wrappingHandler == null) {
            return;
        }

        Map<ModelResourceLocation, BakedModel> bakedModels = event.getModels();
        Set<ModelResourceLocation> keys = ImmutableSet.copyOf(event.getModels().keySet());

        for (ModelResourceLocation modelResourceLocation : keys) {
            bakedModels.put(modelResourceLocation, wrappingHandler.wrap(bakedModels.get(modelResourceLocation), null, modelResourceLocation));
        }
    }
}
