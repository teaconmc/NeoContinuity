package me.pepperbell.continuity.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import me.pepperbell.continuity.client.resource.InvalidIdentifierStateHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.FallbackResourceManager;

@Mixin(FallbackResourceManager.class)
abstract class NamespaceResourceManagerMixin {
	@Inject(method = "getResourceLocationFromMetadata", at = @At("HEAD"))
	private static void continuity$onHeadGetMetadataPath(CallbackInfoReturnable<ResourceLocation> cir) {
		InvalidIdentifierStateHolder.get().enable();
	}

	@Inject(method = "getResourceLocationFromMetadata", at = @At("TAIL"))
	private static void continuity$onTailGetMetadataPath(CallbackInfoReturnable<ResourceLocation> cir) {
		InvalidIdentifierStateHolder.get().disable();
	}
}
