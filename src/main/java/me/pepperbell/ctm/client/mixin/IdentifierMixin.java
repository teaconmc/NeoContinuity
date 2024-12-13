package me.pepperbell.ctm.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import me.pepperbell.ctm.client.resource.InvalidIdentifierStateHolder;
import net.minecraft.resources.ResourceLocation;

@Mixin(ResourceLocation.class)
abstract class IdentifierMixin {
	@Inject(method = "isValidPath", at = @At("HEAD"), cancellable = true)
	private static void continuity$onIsPathValid(CallbackInfoReturnable<Boolean> cir) {
		if (InvalidIdentifierStateHolder.get().isEnabled()) {
			cir.setReturnValue(true);
		}
	}
}
