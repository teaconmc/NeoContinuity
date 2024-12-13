package me.pepperbell.ctm.client.model;

import java.util.function.Function;
import java.util.function.Supplier;

import me.pepperbell.ctm.api.client.QuadProcessor;
import me.pepperbell.ctm.client.config.ContinuityConfig;
import me.pepperbell.ctm.client.util.RenderUtil;
import me.pepperbell.ctm.impl.client.ProcessingContextImpl;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public class CtmBakedModel extends ForwardingBakedModel {
	public static final int PASSES = 4;

	protected final BlockState defaultState;
	protected volatile Function<TextureAtlasSprite, QuadProcessors.Slice> defaultSliceFunc;

	public CtmBakedModel(BakedModel wrapped, BlockState defaultState) {
		this.wrapped = wrapped;
		this.defaultState = defaultState;
	}

	@Override
	public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context) {
		if (!ContinuityConfig.INSTANCE.connectedTextures.get()) {
			super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
			return;
		}

		ModelObjectsContainer container = ModelObjectsContainer.get();
		if (!container.featureStates.getConnectedTexturesState().isEnabled()) {
			super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
			return;
		}

		CtmQuadTransform quadTransform = container.ctmQuadTransform;
		if (quadTransform.isActive()) {
			super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
			return;
		}

		// The correct way to get the appearance of the origin state from within a block model is to (1) call
		// getAppearance on the result of blockView.getBlockState(pos) instead of the passed state and (2) pass the
		// pos and world state of the adjacent block as the source pos and source state.
		// (1) is not followed here because at this point in execution, within this call to
		// CtmBakedModel#emitBlockQuads, the state parameter must already contain the world state. Even if this
		// CtmBakedModel is wrapped, then the wrapper must pass the same state as it received because not doing so can
		// cause crashes when the wrapped model is a vanilla multipart model or delegates to one. Thus, getting the
		// world state again is inefficient and unnecessary.
		// (2) is not possible here because the appearance state is necessary to get the slice and only the processors
		// within the slice actually perform checks on adjacent blocks. Likewise, the processors themselves cannot
		// retrieve the appearance state since the correct processors can only be chosen with the initially correct
		// appearance state.
		// Additionally, the side is chosen to always be the first constant of the enum (DOWN) for simplicity. Querying
		// the appearance for all six sides would be more correct, but less efficient. This may be fixed in the future,
		// especially if there is an actual use case for it.
		BlockState appearanceState = state.getAppearance(blockView, pos, Direction.DOWN, state, pos);

		quadTransform.prepare(blockView, appearanceState, state, pos, randomSupplier, context, ContinuityConfig.INSTANCE.useManualCulling.get(), getSliceFunc(appearanceState));

		context.pushTransform(quadTransform);
		super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
		context.popTransform();

		quadTransform.processingContext.outputTo(context.getEmitter());
		quadTransform.reset();
	}

	@Override
	public boolean isVanillaAdapter() {
		if (!ContinuityConfig.INSTANCE.connectedTextures.get()) {
			return super.isVanillaAdapter();
		}
		return false;
	}

	protected Function<TextureAtlasSprite, QuadProcessors.Slice> getSliceFunc(BlockState state) {
		if (state == defaultState) {
			Function<TextureAtlasSprite, QuadProcessors.Slice> sliceFunc = defaultSliceFunc;
			if (sliceFunc == null) {
				synchronized (this) {
					sliceFunc = defaultSliceFunc;
					if (sliceFunc == null) {
						sliceFunc = QuadProcessors.getCache(state);
						defaultSliceFunc = sliceFunc;
					}
				}
			}
			return sliceFunc;
		}
		return QuadProcessors.getCache(state);
	}

	protected static class CtmQuadTransform implements RenderContext.QuadTransform {
		protected final ProcessingContextImpl processingContext = new ProcessingContextImpl();

		protected BlockAndTintGetter blockView;
		protected BlockState appearanceState;
		protected BlockState state;
		protected BlockPos pos;
		protected Supplier<RandomSource> randomSupplier;
		protected RenderContext renderContext;
		protected boolean useManualCulling;
		protected Function<TextureAtlasSprite, QuadProcessors.Slice> sliceFunc;

		protected boolean active;

		@Override
		public boolean transform(MutableQuadView quad) {
			if (useManualCulling && renderContext.isFaceCulled(quad.cullFace())) {
				return false;
			}

			for (int pass = 0; pass < PASSES; pass++) {
				Boolean result = transformOnce(quad, pass);
				if (result != null) {
					return result;
				}
			}

			return true;
		}

		protected Boolean transformOnce(MutableQuadView quad, int pass) {
			TextureAtlasSprite sprite = RenderUtil.getSpriteFinder().find(quad);
			QuadProcessors.Slice slice = sliceFunc.apply(sprite);
			QuadProcessor[] processors = pass == 0 ? slice.processors() : slice.multipassProcessors();
			for (QuadProcessor processor : processors) {
				QuadProcessor.ProcessingResult result = processor.processQuad(quad, sprite, blockView, appearanceState, state, pos, randomSupplier, pass, processingContext);
				if (result == QuadProcessor.ProcessingResult.NEXT_PROCESSOR) {
					continue;
				}
				if (result == QuadProcessor.ProcessingResult.NEXT_PASS) {
					return null;
				}
				if (result == QuadProcessor.ProcessingResult.STOP) {
					return true;
				}
				if (result == QuadProcessor.ProcessingResult.DISCARD) {
					return false;
				}
			}
			return true;
		}

		public boolean isActive() {
			return active;
		}

		public void prepare(BlockAndTintGetter blockView, BlockState appearanceState, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext renderContext, boolean useManualCulling, Function<TextureAtlasSprite, QuadProcessors.Slice> sliceFunc) {
			this.blockView = blockView;
			this.appearanceState = appearanceState;
			this.state = state;
			this.pos = pos;
			this.randomSupplier = randomSupplier;
			this.renderContext = renderContext;
			this.useManualCulling = useManualCulling;
			this.sliceFunc = sliceFunc;

			active = true;

			processingContext.prepare();
		}

		public void reset() {
			blockView = null;
			appearanceState = null;
			state = null;
			pos = null;
			randomSupplier = null;
			renderContext = null;
			useManualCulling = false;
			sliceFunc = null;

			active = false;

			processingContext.reset();
		}
	}
}
