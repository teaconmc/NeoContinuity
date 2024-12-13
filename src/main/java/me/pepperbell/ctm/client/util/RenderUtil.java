package me.pepperbell.ctm.client.util;

import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.material.MaterialFinder;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.model.SpriteFinder;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public final class RenderUtil {
	private static final BlockColors BLOCK_COLORS = Minecraft.getInstance().getBlockColors();
	private static final ModelManager MODEL_MANAGER = Minecraft.getInstance().getModelManager();

	private static final ThreadLocal<MaterialFinder> MATERIAL_FINDER = ThreadLocal.withInitial(() -> RendererAccess.INSTANCE.getRenderer().materialFinder());

	private static SpriteFinder blockAtlasSpriteFinder;

	public static int getTintColor(@Nullable BlockState state, BlockAndTintGetter blockView, BlockPos pos, int tintIndex) {
		if (state == null || tintIndex == -1) {
			return -1;
		}
		return 0xFF000000 | BLOCK_COLORS.getColor(state, blockView, pos, tintIndex);
	}

	public static RenderMaterial findOverlayMaterial(BlendMode blendMode, @Nullable BlockState tintBlock) {
		MaterialFinder finder = getMaterialFinder();
		finder.blendMode(blendMode);
		if (tintBlock != null) {
			finder.ambientOcclusion(TriState.of(canHaveAO(tintBlock)));
		} else {
			finder.ambientOcclusion(TriState.TRUE);
		}
		return finder.find();
	}

	public static boolean canHaveAO(BlockState state) {
		return state.getLightEmission() == 0;
	}

	public static MaterialFinder getMaterialFinder() {
		return MATERIAL_FINDER.get().clear();
	}

	public static SpriteFinder getSpriteFinder() {
		return blockAtlasSpriteFinder;
	}


	public static class ReloadListener implements /*SimpleSynchronousResourceReloadListener*/ ResourceManagerReloadListener {
		//public static final ResourceLocation ID = ContinuityClient.asId("render_util");
		//public static final List<ResourceLocation> DEPENDENCIES = List.of(ResourceReloadListenerKeys.MODELS);
		/*private*/ public static final ReloadListener INSTANCE = new ReloadListener();

		/*public static void init() {
			ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(INSTANCE);
		}*/

		@Override
		public void onResourceManagerReload(ResourceManager manager) {
			blockAtlasSpriteFinder = SpriteFinder.get(MODEL_MANAGER.getAtlas(TextureAtlas.LOCATION_BLOCKS));
		}

		/*@Override
		public ResourceLocation getFabricId() {
			return ID;
		}

		@Override
		public Collection<ResourceLocation> getFabricDependencies() {
			return DEPENDENCIES;
		}*/
	}
}
