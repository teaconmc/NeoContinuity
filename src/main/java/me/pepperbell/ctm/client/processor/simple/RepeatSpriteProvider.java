package me.pepperbell.ctm.client.processor.simple;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import me.pepperbell.ctm.api.client.ProcessingDataProvider;
import me.pepperbell.ctm.client.processor.OrientationMode;
import me.pepperbell.ctm.client.processor.Symmetry;
import me.pepperbell.ctm.client.properties.RepeatCtmProperties;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadView;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public class RepeatSpriteProvider implements SpriteProvider {
	protected TextureAtlasSprite[] sprites;
	protected int width;
	protected int height;
	protected Symmetry symmetry;
	protected OrientationMode orientationMode;

	public RepeatSpriteProvider(TextureAtlasSprite[] sprites, int width, int height, Symmetry symmetry, OrientationMode orientationMode) {
		this.sprites = sprites;
		this.width = width;
		this.height = height;
		this.symmetry = symmetry;
		this.orientationMode = orientationMode;
	}

	@Override
	@Nullable
	public TextureAtlasSprite getSprite(QuadView quad, TextureAtlasSprite sprite, BlockAndTintGetter blockView, BlockState appearanceState, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, ProcessingDataProvider dataProvider) {
		Direction face = symmetry.apply(quad.lightFace());

		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();

		int spriteX;
		int spriteY;
		switch (face) {
			case DOWN -> {
				// MCPatcher uses a different formula for the down face.
				// It is not used here to maintain Optifine parity.
				// spriteX = -x;
				// spriteY = -z;
				spriteX = x;
				spriteY = -z - 1;
			}
			case UP -> {
				spriteX = x;
				spriteY = z;
			}
			case NORTH -> {
				spriteX = -x - 1;
				spriteY = -y;
			}
			case SOUTH -> {
				spriteX = x;
				spriteY = -y;
			}
			case WEST -> {
				spriteX = z;
				spriteY = -y;
			}
			case EAST -> {
				spriteX = -z - 1;
				spriteY = -y;
			}
			default -> {
				spriteX = 0;
				spriteY = 0;
			}
		}

		switch (orientationMode.getOrientation(quad, appearanceState)) {
			case 1 -> {
				int temp = spriteX;
				spriteX = -spriteY - 1;
				spriteY = temp;
			}
			case 2 -> {
				spriteX = -spriteX - 1;
				spriteY = -spriteY - 1;
			}
			case 3 -> {
				int temp = spriteX;
				spriteX = spriteY;
				spriteY = -temp - 1;
			}
			case 4 -> {
				int temp = spriteX;
				spriteX = spriteY;
				spriteY = temp;
			}
			case 5 -> {
				spriteY = -spriteY - 1;
			}
			case 6 -> {
				int temp = spriteX;
				spriteX = -spriteY - 1;
				spriteY = -temp - 1;
			}
			case 7 -> {
				spriteX = -spriteX - 1;
			}
		}

		spriteX %= width;
		if (spriteX < 0) {
			spriteX += width;
		}
		spriteY %= height;
		if (spriteY < 0) {
			spriteY += height;
		}

		return sprites[width * spriteY + spriteX];
	}

	public static class Factory implements SpriteProvider.Factory<RepeatCtmProperties> {
		@Override
		public SpriteProvider createSpriteProvider(TextureAtlasSprite[] sprites, RepeatCtmProperties properties) {
			return new RepeatSpriteProvider(sprites, properties.getWidth(), properties.getHeight(), properties.getSymmetry(), properties.getOrientationMode());
		}

		@Override
		public int getTextureAmount(RepeatCtmProperties properties) {
			return properties.getWidth() * properties.getHeight();
		}
	}
}
