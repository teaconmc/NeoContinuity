package me.pepperbell.ctm.client.processor;

import java.util.function.Consumer;
import java.util.function.Supplier;

import me.pepperbell.ctm.api.client.ProcessingDataKey;
import me.pepperbell.ctm.api.client.ProcessingDataKeyRegistry;
import me.pepperbell.ctm.client.ContinuityClient;
import me.pepperbell.ctm.client.processor.overlay.SimpleOverlayQuadProcessor;
import me.pepperbell.ctm.client.processor.overlay.StandardOverlayQuadProcessor;
import net.minecraft.core.BlockPos;

public final class ProcessingDataKeys {
	public static final ProcessingDataKey<BlockPos.MutableBlockPos> MUTABLE_POS = create("mutable_pos", BlockPos.MutableBlockPos::new);
	public static final ProcessingDataKey<BaseProcessingPredicate.BiomeCache> BIOME_CACHE = create("biome_cache", BaseProcessingPredicate.BiomeCache::new, BaseProcessingPredicate.BiomeCache::reset);
	public static final ProcessingDataKey<BaseProcessingPredicate.BlockEntityNameCache> BLOCK_ENTITY_NAME_CACHE = create("block_entity_name_cache", BaseProcessingPredicate.BlockEntityNameCache::new, BaseProcessingPredicate.BlockEntityNameCache::reset);
	public static final ProcessingDataKey<CompactCtmQuadProcessor.VertexContainer> VERTEX_CONTAINER = create("vertex_container", CompactCtmQuadProcessor.VertexContainer::new);
	public static final ProcessingDataKey<StandardOverlayQuadProcessor.OverlayEmitterPool> STANDARD_OVERLAY_EMITTER_POOL = create("standard_overlay_emitter_pool", StandardOverlayQuadProcessor.OverlayEmitterPool::new, StandardOverlayQuadProcessor.OverlayEmitterPool::reset);
	public static final ProcessingDataKey<SimpleOverlayQuadProcessor.OverlayEmitterPool> SIMPLE_OVERLAY_EMITTER_POOL = create("simple_overlay_emitter_pool", SimpleOverlayQuadProcessor.OverlayEmitterPool::new, SimpleOverlayQuadProcessor.OverlayEmitterPool::reset);

	private static <T> ProcessingDataKey<T> create(String id, Supplier<T> valueSupplier) {
		return ProcessingDataKeyRegistry.get().registerKey(ContinuityClient.asId(id), valueSupplier);
	}

	private static <T> ProcessingDataKey<T> create(String id, Supplier<T> valueSupplier, Consumer<T> valueResetAction) {
		return ProcessingDataKeyRegistry.get().registerKey(ContinuityClient.asId(id), valueSupplier, valueResetAction);
	}

	public static void init() {
	}
}
