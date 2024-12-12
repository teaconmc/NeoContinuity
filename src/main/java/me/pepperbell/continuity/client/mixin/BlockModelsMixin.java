package me.pepperbell.continuity.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.pepperbell.continuity.client.util.SpriteCalculator;
import net.minecraft.client.renderer.block.BlockModelShaper;

@Mixin(BlockModelShaper.class)
abstract class BlockModelsMixin {
	@Inject(method = "replaceCache", at = @At("HEAD"))
	private void continuity$onHeadSetModels(CallbackInfo ci) {
		SpriteCalculator.clearCache();
	}
}
