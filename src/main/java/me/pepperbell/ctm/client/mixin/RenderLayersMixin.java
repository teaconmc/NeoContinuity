package me.pepperbell.ctm.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import me.pepperbell.ctm.client.config.ContinuityConfig;
import me.pepperbell.ctm.client.resource.CustomBlockLayers;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(ItemBlockRenderTypes.class)
abstract class RenderLayersMixin {
	@Inject(method = "getChunkRenderType", at = @At("HEAD"), cancellable = true)
	private static void continuity$onHeadGetBlockLayer(BlockState state, CallbackInfoReturnable<RenderType> cir) {
		if (!CustomBlockLayers.isEmpty() && ContinuityConfig.INSTANCE.customBlockLayers.get()) {
			RenderType layer = CustomBlockLayers.getLayer(state);
			if (layer != null) {
				cir.setReturnValue(layer);
			}
		}
	}

	@Inject(method = "getMovingBlockRenderType", at = @At("HEAD"), cancellable = true)
	private static void continuity$onHeadGetMovingBlockLayer(BlockState state, CallbackInfoReturnable<RenderType> cir) {
		if (!CustomBlockLayers.isEmpty() && ContinuityConfig.INSTANCE.customBlockLayers.get()) {
			RenderType layer = CustomBlockLayers.getLayer(state);
			if (layer != null) {
				cir.setReturnValue(layer == RenderType.translucent() ? RenderType.translucentMovingBlock() : layer);
			}
		}
	}
}
