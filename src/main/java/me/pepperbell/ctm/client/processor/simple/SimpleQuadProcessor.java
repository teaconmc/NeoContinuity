package me.pepperbell.ctm.client.processor.simple;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import me.pepperbell.ctm.api.client.QuadProcessor;
import me.pepperbell.ctm.client.processor.AbstractQuadProcessorFactory;
import me.pepperbell.ctm.client.processor.BaseProcessingPredicate;
import me.pepperbell.ctm.client.processor.ProcessingPredicate;
import me.pepperbell.ctm.client.properties.BaseCtmProperties;
import me.pepperbell.ctm.client.util.QuadUtil;
import me.pepperbell.ctm.client.util.TextureUtil;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public class SimpleQuadProcessor implements QuadProcessor {
	protected SpriteProvider spriteProvider;
	protected ProcessingPredicate processingPredicate;

	public SimpleQuadProcessor(SpriteProvider spriteProvider, ProcessingPredicate processingPredicate) {
		this.spriteProvider = spriteProvider;
		this.processingPredicate = processingPredicate;
	}

	@Override
	public ProcessingResult processQuad(MutableQuadView quad, TextureAtlasSprite sprite, BlockAndTintGetter blockView, BlockState appearanceState, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, int pass, ProcessingContext context) {
		if (!processingPredicate.shouldProcessQuad(quad, sprite, blockView, appearanceState, state, pos, context)) {
			return ProcessingResult.NEXT_PROCESSOR;
		}
		TextureAtlasSprite newSprite = spriteProvider.getSprite(quad, sprite, blockView, appearanceState, state, pos, randomSupplier, context);
		return process(quad, sprite, newSprite);
	}

	public static ProcessingResult process(MutableQuadView quad, TextureAtlasSprite oldSprite, @Nullable TextureAtlasSprite newSprite) {
		if (newSprite == null) {
			return ProcessingResult.STOP;
		}
		if (TextureUtil.isMissingSprite(newSprite)) {
			return ProcessingResult.NEXT_PROCESSOR;
		}
		QuadUtil.interpolate(quad, oldSprite, newSprite);
		return ProcessingResult.NEXT_PASS;
	}

	public static class Factory<T extends BaseCtmProperties> extends AbstractQuadProcessorFactory<T> {
		protected SpriteProvider.Factory<? super T> spriteProviderFactory;

		public Factory(SpriteProvider.Factory<? super T> spriteProviderFactory) {
			this.spriteProviderFactory = spriteProviderFactory;
		}

		@Override
		public QuadProcessor createProcessor(T properties, TextureAtlasSprite[] sprites) {
			return new SimpleQuadProcessor(spriteProviderFactory.createSpriteProvider(sprites, properties), BaseProcessingPredicate.fromProperties(properties));
		}

		@Override
		public int getTextureAmount(T properties) {
			return spriteProviderFactory.getTextureAmount(properties);
		}
	}
}
