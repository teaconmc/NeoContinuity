package me.pepperbell.ctm.impl.client;

import org.jetbrains.annotations.Nullable;

import me.pepperbell.ctm.api.client.EmissiveSpriteApi;
import me.pepperbell.ctm.client.mixinterface.SpriteExtension;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public final class EmissiveSpriteApiImpl implements EmissiveSpriteApi {
	public static final EmissiveSpriteApiImpl INSTANCE = new EmissiveSpriteApiImpl();

	@Override
	@Nullable
	public TextureAtlasSprite getEmissiveSprite(TextureAtlasSprite sprite) {
		return ((SpriteExtension) sprite).continuity$getEmissiveSprite();
	}
}
