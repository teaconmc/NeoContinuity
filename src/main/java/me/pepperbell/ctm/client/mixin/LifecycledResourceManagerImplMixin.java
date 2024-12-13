package me.pepperbell.ctm.client.mixin;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.pepperbell.ctm.client.mixinterface.LifecycledResourceManagerImplExtension;
import me.pepperbell.ctm.client.resource.ResourceRedirectHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.MultiPackResourceManager;

@Mixin(MultiPackResourceManager.class)
abstract class LifecycledResourceManagerImplMixin implements LifecycledResourceManagerImplExtension {
	@Unique
	private ResourceRedirectHandler continuity$redirectHandler;

	@Override
	@Nullable
	public ResourceRedirectHandler continuity$getRedirectHandler() {
		return continuity$redirectHandler;
	}

	@Inject(method = "<init>", at = @At("TAIL"))
	private void continuity$onTailInit(PackType type, List<PackResources> packs, CallbackInfo ci) {
		if (type == PackType.CLIENT_RESOURCES) {
			continuity$redirectHandler = new ResourceRedirectHandler();
		}
	}

	@ModifyVariable(method = "getResource", at = @At("HEAD"), argsOnly = true)
	private ResourceLocation continuity$redirectGetResourceId(ResourceLocation id) {
		if (continuity$redirectHandler != null) {
			return continuity$redirectHandler.redirect(id);
		}
		return id;
	}

	@ModifyVariable(method = "getResourceStack", at = @At("HEAD"), argsOnly = true)
	private ResourceLocation continuity$redirectGetAllResourcesId(ResourceLocation id) {
		if (continuity$redirectHandler != null) {
			return continuity$redirectHandler.redirect(id);
		}
		return id;
	}
}
