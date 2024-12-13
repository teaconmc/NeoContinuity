package me.pepperbell.ctm.client.processor;

import java.util.function.Supplier;

import me.pepperbell.ctm.api.client.QuadProcessor;
import me.pepperbell.ctm.client.processor.simple.SimpleQuadProcessor;
import me.pepperbell.ctm.client.properties.ConnectingCtmProperties;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class TopQuadProcessor extends AbstractQuadProcessor {
	protected ConnectionPredicate connectionPredicate;
	protected boolean innerSeams;

	public TopQuadProcessor(TextureAtlasSprite[] sprites, ProcessingPredicate processingPredicate, ConnectionPredicate connectionPredicate, boolean innerSeams) {
		super(sprites, processingPredicate);
		this.connectionPredicate = connectionPredicate;
		this.innerSeams = innerSeams;
	}

	@Override
	public ProcessingResult processQuadInner(MutableQuadView quad, TextureAtlasSprite sprite, BlockAndTintGetter blockView, BlockState appearanceState, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, int pass, ProcessingContext context) {
		Direction lightFace = quad.lightFace();
		Direction.Axis axis;
		if (appearanceState.hasProperty(BlockStateProperties.AXIS)) {
			axis = appearanceState.getValue(BlockStateProperties.AXIS);
		} else {
			axis = Direction.Axis.Y;
		}
		if (lightFace.getAxis() != axis) {
			Direction up = Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE);
			BlockPos.MutableBlockPos mutablePos = context.getData(ProcessingDataKeys.MUTABLE_POS).setWithOffset(pos, up);
			if (connectionPredicate.shouldConnect(blockView, appearanceState, state, pos, mutablePos, lightFace, sprite, innerSeams)) {
				return SimpleQuadProcessor.process(quad, sprite, sprites[0]);
			}
		}
		return ProcessingResult.NEXT_PROCESSOR;
	}

	public static class Factory extends AbstractQuadProcessorFactory<ConnectingCtmProperties> {
		@Override
		public QuadProcessor createProcessor(ConnectingCtmProperties properties, TextureAtlasSprite[] sprites) {
			return new TopQuadProcessor(sprites, BaseProcessingPredicate.fromProperties(properties), properties.getConnectionPredicate(), properties.getInnerSeams());
		}

		@Override
		public int getTextureAmount(ConnectingCtmProperties properties) {
			return 1;
		}
	}
}
