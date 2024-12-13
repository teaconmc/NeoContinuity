package me.pepperbell.ctm.client.resource;

import java.io.InputStream;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import me.pepperbell.ctm.api.client.CachingPredicates;
import me.pepperbell.ctm.api.client.CtmLoader;
import me.pepperbell.ctm.api.client.CtmLoaderRegistry;
import me.pepperbell.ctm.api.client.CtmProperties;
import me.pepperbell.ctm.api.client.QuadProcessor;
import me.pepperbell.ctm.client.ContinuityClient;
import me.pepperbell.ctm.client.model.QuadProcessors;
import me.pepperbell.ctm.client.util.BooleanState;
import me.pepperbell.ctm.client.util.biome.BiomeHolderManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;

public class CtmPropertiesLoader {
	private final ResourceManager resourceManager;
	private final List<LoadingContainer<?>> containers = new ObjectArrayList<>();
	private final Map<ResourceLocation, Set<ResourceLocation>> textureDependencies = new Object2ObjectOpenHashMap<>();

	private CtmPropertiesLoader(ResourceManager resourceManager) {
		this.resourceManager = resourceManager;
	}

	public static LoadingResult loadAllWithState(ResourceManager resourceManager) {
		// TODO: move these to the very beginning of resource reload
		BiomeHolderManager.clearCache();

		LoadingResult result = loadAll(resourceManager);

		// TODO: move these to the very end of resource reload
		BiomeHolderManager.refreshHolders();

		return result;
	}

	public static LoadingResult loadAll(ResourceManager resourceManager) {
		return new CtmPropertiesLoader(resourceManager).loadAll();
	}

	private LoadingResult loadAll() {
		int packPriority = 0;
		Iterator<PackResources> iterator = resourceManager.listPacks().iterator();
		BooleanState invalidIdentifierState = InvalidIdentifierStateHolder.get();
		invalidIdentifierState.enable();
		while (iterator.hasNext()) {
			PackResources pack = iterator.next();
			loadAll(pack, packPriority);
			packPriority++;
		}
		invalidIdentifierState.disable();

		containers.sort(Comparator.reverseOrder());

		return new LoadingResult(containers, textureDependencies);
	}

	private void loadAll(PackResources pack, int packPriority) {
		for (String namespace : pack.getNamespaces(PackType.CLIENT_RESOURCES)) {
			pack.listResources(PackType.CLIENT_RESOURCES, namespace, "optifine/ctm", (resourceId, inputSupplier) -> {
				if (resourceId.getPath().endsWith(".properties")) {
					try (InputStream stream = inputSupplier.get()) {
						Properties properties = new Properties();
						properties.load(stream);
						load(properties, resourceId, pack, packPriority);
					} catch (Exception e) {
						ContinuityClient.LOGGER.error("Failed to load CTM properties from file '" + resourceId + "' in pack '" + pack.packId() + "'", e);
					}
				}
			});
		}
	}

	private void load(Properties properties, ResourceLocation resourceId, PackResources pack, int packPriority) {
		String method = properties.getProperty("method", "ctm").trim();
		CtmLoader<?> loader = CtmLoaderRegistry.get().getLoader(method);
		if (loader != null) {
			load(loader, properties, resourceId, pack, packPriority, method);
		} else {
			ContinuityClient.LOGGER.error("Unknown 'method' value '" + method + "' in file '" + resourceId + "' in pack '" + pack.packId() + "'");
		}
	}

	private <T extends CtmProperties> void load(CtmLoader<T> loader, Properties properties, ResourceLocation resourceId, PackResources pack, int packPriority, String method) {
		T ctmProperties = loader.getPropertiesFactory().createProperties(properties, resourceId, pack, packPriority, resourceManager, method);
		if (ctmProperties != null) {
			LoadingContainer<T> container = new LoadingContainer<>(loader, ctmProperties);
			containers.add(container);
			for (Material spriteId : ctmProperties.getTextureDependencies()) {
				Set<ResourceLocation> atlasTextureDependencies = textureDependencies.computeIfAbsent(spriteId.atlasLocation(), id -> new ObjectOpenHashSet<>());
				atlasTextureDependencies.add(spriteId.texture());
			}
		}
	}

	private record LoadingContainer<T extends CtmProperties>(CtmLoader<T> loader, T properties) implements Comparable<LoadingContainer<?>> {
		public QuadProcessors.ProcessorHolder toProcessorHolder(Function<Material, TextureAtlasSprite> textureGetter) {
			QuadProcessor processor = loader.getProcessorFactory().createProcessor(properties, textureGetter);
			CachingPredicates predicates = loader.getPredicatesFactory().createPredicates(properties, textureGetter);
			return new QuadProcessors.ProcessorHolder(processor, predicates);
		}

		@Override
		public int compareTo(@NotNull LoadingContainer<?> o) {
			return properties.compareTo(o.properties);
		}
	}

	public static class LoadingResult {
		private final List<LoadingContainer<?>> containers;
		private final Map<ResourceLocation, Set<ResourceLocation>> textureDependencies;

		private LoadingResult(List<LoadingContainer<?>> containers, Map<ResourceLocation, Set<ResourceLocation>> textureDependencies) {
			this.containers = containers;
			this.textureDependencies = textureDependencies;
		}

		public List<QuadProcessors.ProcessorHolder> createProcessorHolders(Function<Material, TextureAtlasSprite> textureGetter) {
			List<QuadProcessors.ProcessorHolder> processorHolders = new ObjectArrayList<>();
			for (LoadingContainer<?> container : containers) {
				processorHolders.add(container.toProcessorHolder(textureGetter));
			}
			return processorHolders;
		}

		public Map<ResourceLocation, Set<ResourceLocation>> getTextureDependencies() {
			return textureDependencies;
		}
	}
}
