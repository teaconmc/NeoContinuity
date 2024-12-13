package me.pepperbell.ctm.client.mixinterface;

import org.jetbrains.annotations.Nullable;

import me.pepperbell.ctm.client.resource.ModelWrappingHandler;

public interface ModelLoaderExtension {
	@Nullable
	ModelWrappingHandler continuity$getModelWrappingHandler();

	void continuity$setModelWrappingHandler(@Nullable ModelWrappingHandler handler);
}
