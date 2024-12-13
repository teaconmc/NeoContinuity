package me.pepperbell.ctm.client.mixinterface;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.jetbrains.annotations.Nullable;

public interface SpriteExtension {
	@Nullable
	TextureAtlasSprite continuity$getEmissiveSprite();

	void continuity$setEmissiveSprite(TextureAtlasSprite sprite);
}
