package me.pepperbell.ctm.client.processor.simple;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import me.pepperbell.ctm.api.client.ProcessingDataProvider;
import me.pepperbell.ctm.client.processor.ConnectionPredicate;
import me.pepperbell.ctm.client.processor.DirectionMaps;
import me.pepperbell.ctm.client.processor.OrientationMode;
import me.pepperbell.ctm.client.processor.ProcessingDataKeys;
import me.pepperbell.ctm.client.properties.OrientedConnectingCtmProperties;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadView;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public class CtmSpriteProvider implements SpriteProvider {
	// Indices for this array are formed from these bit values:
	// 128 64  32
	// 1   *   16
	// 2   4   8
	public static final int[] SPRITE_INDEX_MAP = new int[] {
			0, 3, 0, 3, 12, 5, 12, 15, 0, 3, 0, 3, 12, 5, 12, 15,
			1, 2, 1, 2, 4, 7, 4, 29, 1, 2, 1, 2, 13, 31, 13, 14,
			0, 3, 0, 3, 12, 5, 12, 15, 0, 3, 0, 3, 12, 5, 12, 15,
			1, 2, 1, 2, 4, 7, 4, 29, 1, 2, 1, 2, 13, 31, 13, 14,
			36, 17, 36, 17, 24, 19, 24, 43, 36, 17, 36, 17, 24, 19, 24, 43,
			16, 18, 16, 18, 6, 46, 6, 21, 16, 18, 16, 18, 28, 9, 28, 22,
			36, 17, 36, 17, 24, 19, 24, 43, 36, 17, 36, 17, 24, 19, 24, 43,
			37, 40, 37, 40, 30, 8, 30, 34, 37, 40, 37, 40, 25, 23, 25, 45,
			0, 3, 0, 3, 12, 5, 12, 15, 0, 3, 0, 3, 12, 5, 12, 15,
			1, 2, 1, 2, 4, 7, 4, 29, 1, 2, 1, 2, 13, 31, 13, 14,
			0, 3, 0, 3, 12, 5, 12, 15, 0, 3, 0, 3, 12, 5, 12, 15,
			1, 2, 1, 2, 4, 7, 4, 29, 1, 2, 1, 2, 13, 31, 13, 14,
			36, 39, 36, 39, 24, 41, 24, 27, 36, 39, 36, 39, 24, 41, 24, 27,
			16, 42, 16, 42, 6, 20, 6, 10, 16, 42, 16, 42, 28, 35, 28, 44,
			36, 39, 36, 39, 24, 41, 24, 27, 36, 39, 36, 39, 24, 41, 24, 27,
			37, 38, 37, 38, 30, 11, 30, 32, 37, 38, 37, 38, 25, 33, 25, 26,
	};

	protected TextureAtlasSprite[] sprites;
	protected ConnectionPredicate connectionPredicate;
	protected boolean innerSeams;
	protected OrientationMode orientationMode;

	public CtmSpriteProvider(TextureAtlasSprite[] sprites, ConnectionPredicate connectionPredicate, boolean innerSeams, OrientationMode orientationMode) {
		this.sprites = sprites;
		this.connectionPredicate = connectionPredicate;
		this.innerSeams = innerSeams;
		this.orientationMode = orientationMode;
	}

	@Override
	@Nullable
	public TextureAtlasSprite getSprite(QuadView quad, TextureAtlasSprite sprite, BlockAndTintGetter blockView, BlockState appearanceState, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, ProcessingDataProvider dataProvider) {
		Direction[] directions = DirectionMaps.getDirections(orientationMode, quad, appearanceState);
		BlockPos.MutableBlockPos mutablePos = dataProvider.getData(ProcessingDataKeys.MUTABLE_POS);
		int connections = getConnections(directions, connectionPredicate, innerSeams, mutablePos, blockView, appearanceState, state, pos, quad.lightFace(), sprite);
		return sprites[SPRITE_INDEX_MAP[connections]];
	}

	public static int getConnections(Direction[] directions, ConnectionPredicate connectionPredicate, boolean innerSeams, BlockPos.MutableBlockPos mutablePos, BlockAndTintGetter blockView, BlockState appearanceState, BlockState state, BlockPos pos, Direction face, TextureAtlasSprite quadSprite) {
		int connections = 0;
		for (int i = 0; i < 4; i++) {
			mutablePos.setWithOffset(pos, directions[i]);
			if (connectionPredicate.shouldConnect(blockView, appearanceState, state, pos, mutablePos, face, quadSprite, innerSeams)) {
				connections |= 1 << (i * 2);
			}
		}
		for (int i = 0; i < 4; i++) {
			int index1 = i;
			int index2 = (i + 1) % 4;
			if (((connections >>> index1 * 2) & 1) == 1 && ((connections >>> index2 * 2) & 1) == 1) {
				mutablePos.setWithOffset(pos, directions[index1]).move(directions[index2]);
				if (connectionPredicate.shouldConnect(blockView, appearanceState, state, pos, mutablePos, face, quadSprite, innerSeams)) {
					connections |= 1 << (i * 2 + 1);
				}
			}
		}
		return connections;
	}

	public static class Factory implements SpriteProvider.Factory<OrientedConnectingCtmProperties> {
		@Override
		public SpriteProvider createSpriteProvider(TextureAtlasSprite[] sprites, OrientedConnectingCtmProperties properties) {
			return new CtmSpriteProvider(sprites, properties.getConnectionPredicate(), properties.getInnerSeams(), properties.getOrientationMode());
		}

		@Override
		public int getTextureAmount(OrientedConnectingCtmProperties properties) {
			return 47;
		}
	}
}
