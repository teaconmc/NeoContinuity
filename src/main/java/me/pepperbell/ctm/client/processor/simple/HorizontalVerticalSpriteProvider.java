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

public class HorizontalVerticalSpriteProvider extends HorizontalSpriteProvider {
	// Indices for this array are formed from these bit values:
	// 32  16  8
	//     *
	// 1   2   4
	protected static final int[] SECONDARY_SPRITE_INDEX_MAP = new int[] {
			3, 3, 6, 3, 3, 3, 3, 3, 3, 3, 6, 3, 3, 3, 3, 3,
			4, 4, 5, 4, 4, 4, 4, 4, 3, 3, 6, 3, 3, 3, 3, 3,
			3, 3, 6, 3, 3, 3, 3, 3, 3, 3, 6, 3, 3, 3, 3, 3,
			3, 3, 6, 3, 3, 3, 3, 3, 3, 3, 6, 3, 3, 3, 3, 3,
	};

	public HorizontalVerticalSpriteProvider(TextureAtlasSprite[] sprites, ConnectionPredicate connectionPredicate, boolean innerSeams, OrientationMode orientationMode) {
		super(sprites, connectionPredicate, innerSeams, orientationMode);
	}

	@Override
	@Nullable
	public TextureAtlasSprite getSprite(QuadView quad, TextureAtlasSprite sprite, BlockAndTintGetter blockView, BlockState appearanceState, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, ProcessingDataProvider dataProvider) {
		Direction[] directions = DirectionMaps.getDirections(orientationMode, quad, appearanceState);
		BlockPos.MutableBlockPos mutablePos = dataProvider.getData(ProcessingDataKeys.MUTABLE_POS);
		int connections = getConnections(directions, mutablePos, blockView, appearanceState, state, pos, quad.lightFace(), sprite);
		if (connections != 0) {
			return sprites[SPRITE_INDEX_MAP[connections]];
		} else {
			int secondaryConnections = getSecondaryConnections(directions, mutablePos, blockView, appearanceState, state, pos, quad.lightFace(), sprite);
			return sprites[SECONDARY_SPRITE_INDEX_MAP[secondaryConnections]];
		}
	}

	protected int getSecondaryConnections(Direction[] directions, BlockPos.MutableBlockPos mutablePos, BlockAndTintGetter blockView, BlockState appearanceState, BlockState state, BlockPos pos, Direction face, TextureAtlasSprite quadSprite) {
		int connections = 0;
		for (int i = 0; i < 2; i++) {
			Direction direction = directions[i * 2 + 1];
			mutablePos.setWithOffset(pos, direction);
			if (connectionPredicate.shouldConnect(blockView, appearanceState, state, pos, mutablePos, face, quadSprite, innerSeams)) {
				connections |= 1 << (i * 3 + 1);
				for (int j = 0; j < 2; j++) {
					mutablePos.setWithOffset(pos, direction).move(directions[((i + j) % 2) * 2]);
					if (connectionPredicate.shouldConnect(blockView, appearanceState, state, pos, mutablePos, face, quadSprite, innerSeams)) {
						connections |= 1 << (i * 3 + j * 2);
					}
				}
			}
		}
		return connections;
	}

	public static class Factory implements SpriteProvider.Factory<OrientedConnectingCtmProperties> {
		@Override
		public SpriteProvider createSpriteProvider(TextureAtlasSprite[] sprites, OrientedConnectingCtmProperties properties) {
			return new HorizontalVerticalSpriteProvider(sprites, properties.getConnectionPredicate(), properties.getInnerSeams(), properties.getOrientationMode());
		}

		@Override
		public int getTextureAmount(OrientedConnectingCtmProperties properties) {
			return 7;
		}
	}
}
