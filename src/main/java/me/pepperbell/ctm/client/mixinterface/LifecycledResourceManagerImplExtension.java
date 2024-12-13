package me.pepperbell.ctm.client.mixinterface;

import org.jetbrains.annotations.Nullable;

import me.pepperbell.ctm.client.resource.ResourceRedirectHandler;

public interface LifecycledResourceManagerImplExtension {
	@Nullable
	ResourceRedirectHandler continuity$getRedirectHandler();
}
