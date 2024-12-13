package me.pepperbell.ctm.client.properties;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.function.Predicate;

import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import me.pepperbell.ctm.client.ContinuityClient;
import me.pepperbell.ctm.client.processor.OrientationMode;
import me.pepperbell.ctm.client.processor.Symmetry;
import me.pepperbell.ctm.client.resource.ResourceRedirectHandler;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public final class PropertiesParsingHelper {
	public static final Predicate<BlockState> EMPTY_BLOCK_STATE_PREDICATE = state -> false;

	@Nullable
	public static Set<ResourceLocation> parseMatchTiles(Properties properties, String propertyKey, ResourceLocation fileLocation, String packId, @Nullable ResourceRedirectHandler redirectHandler) {
		String matchTilesStr = properties.getProperty(propertyKey);
		if (matchTilesStr == null) {
			return null;
		}

		String[] matchTileStrs = matchTilesStr.trim().split(" ");
		if (matchTileStrs.length != 0) {
			String basePath = FilenameUtils.getPath(fileLocation.getPath());
			ObjectOpenHashSet<ResourceLocation> set = new ObjectOpenHashSet<>();

			for (int i = 0; i < matchTileStrs.length; i++) {
				String matchTileStr = matchTileStrs[i];
				if (matchTileStr.isEmpty()) {
					continue;
				}

				String[] parts = matchTileStr.split(":", 2);
				if (parts.length != 0) {
					String namespace;
					String path;
					if (parts.length > 1) {
						namespace = parts[0];
						path = parts[1];
					} else {
						namespace = null;
						path = parts[0];
					}

					if (path.endsWith(".png")) {
						path = path.substring(0, path.length() - 4);
					}

					if (namespace == null) {
						if (path.startsWith("assets/minecraft/")) {
							path = path.substring(17);
						} else if (path.startsWith("./")) {
							path = basePath + path.substring(2);
						} else if (path.startsWith("~/")) {
							path = "optifine/" + path.substring(2);
						} else if (path.startsWith("/")) {
							path = "optifine/" + path.substring(1);
						}
					}

					if (path.startsWith("textures/")) {
						path = path.substring(9);
					} else if (path.startsWith("optifine/")) {
						if (redirectHandler == null) {
							continue;
						}
						path = redirectHandler.getSourceSpritePath(path + ".png");
						if (namespace == null) {
							namespace = fileLocation.getNamespace();
						}
					} else if (!path.contains("/")) {
						path = "block/" + path;
					}

					if (namespace == null) {
						namespace = ResourceLocation.DEFAULT_NAMESPACE;
					}

					try {
						set.add(ResourceLocation.fromNamespaceAndPath(namespace, path));
					} catch (ResourceLocationException e) {
						ContinuityClient.LOGGER.warn("Invalid '" + propertyKey + "' element '" + matchTileStr + "' at index " + i + " in file '" + fileLocation + "' in pack '" + packId + "'", e);
					}
				} else {
					ContinuityClient.LOGGER.warn("Invalid '" + propertyKey + "' element '" + matchTileStr + "' at index " + i + " in file '" + fileLocation + "' in pack '" + packId + "'");
				}
			}

			set.trim();
			return set;
		}
		return Collections.emptySet();
	}

	@Nullable
	public static Predicate<BlockState> parseBlockStates(Properties properties, String propertyKey, ResourceLocation fileLocation, String packId) {
		String blockStatesStr = properties.getProperty(propertyKey);
		if (blockStatesStr == null) {
			return null;
		}

		String[] blockStateStrs = blockStatesStr.trim().split(" ");
		if (blockStateStrs.length != 0) {
			ReferenceOpenHashSet<Block> blockSet = new ReferenceOpenHashSet<>();
			Reference2ObjectOpenHashMap<Block, Object2ObjectOpenHashMap<Property<?>, ObjectOpenHashSet<Comparable<?>>>> propertyMaps = new Reference2ObjectOpenHashMap<>();

			Block:
			for (int i = 0; i < blockStateStrs.length; i++) {
				String blockStateStr = blockStateStrs[i].trim();
				if (blockStateStr.isEmpty()) {
					continue;
				}

				String[] parts = blockStateStr.split(":");
				if (parts.length != 0) {
					ResourceLocation blockId;
					int startIndex;
					try {
						if (parts.length == 1 || parts[1].contains("=")) {
							blockId = ResourceLocation.withDefaultNamespace(parts[0]);
							startIndex = 1;
						} else {
							blockId = ResourceLocation.fromNamespaceAndPath(parts[0], parts[1]);
							startIndex = 2;
						}
					} catch (ResourceLocationException e) {
						ContinuityClient.LOGGER.warn("Invalid '" + propertyKey + "' element '" + blockStateStr + "' at index " + i + " in file '" + fileLocation + "' in pack '" + packId + "'", e);
						continue;
					}

					if (BuiltInRegistries.BLOCK.containsKey(blockId)) {
						Block block = BuiltInRegistries.BLOCK.get(blockId);
						if (!blockSet.contains(block)) {
							if (parts.length > startIndex) {
								Object2ObjectOpenHashMap<Property<?>, ObjectOpenHashSet<Comparable<?>>> propertyMap = new Object2ObjectOpenHashMap<>();

								for (int j = startIndex; j < parts.length; j++) {
									String part = parts[j];
									if (!part.isEmpty()) {
										String[] propertyParts = part.split("=", 2);
										if (propertyParts.length == 2) {
											String propertyName = propertyParts[0];
											Property<?> property = block.getStateDefinition().getProperty(propertyName);
											if (property != null) {
												String propertyValuesStr = propertyParts[1];
												String[] propertyValueStrs = propertyValuesStr.split(",");
												if (propertyValueStrs.length != 0) {
													ObjectOpenHashSet<Comparable<?>> valueSet = propertyMap.computeIfAbsent(property, p -> new ObjectOpenHashSet<>(Hash.DEFAULT_INITIAL_SIZE, Hash.VERY_FAST_LOAD_FACTOR));

													for (String propertyValueStr : propertyValueStrs) {
														Optional<? extends Comparable<?>> optionalValue = property.getValue(propertyValueStr);
														if (optionalValue.isPresent()) {
															valueSet.add(optionalValue.get());
														} else {
															ContinuityClient.LOGGER.warn("Invalid block property value '" + propertyValueStr + "' for property '" + propertyName + "' for block '" + blockId + "' in '" + propertyKey + "' element '" + blockStateStr + "' at index " + i + " in file '" + fileLocation + "' in pack '" + packId + "'");
															continue Block;
														}
													}
												} else {
													ContinuityClient.LOGGER.warn("Invalid block property definition for block '" + blockId + "' in '" + propertyKey + "' element '" + blockStateStr + "' at index " + i + " in file '" + fileLocation + "' in pack '" + packId + "'");
													continue Block;
												}
											} else {
												ContinuityClient.LOGGER.warn("Unknown block property '" + propertyName + "' for block '" + blockId + "' in '" + propertyKey + "' element '" + blockStateStr + "' at index " + i + " in file '" + fileLocation + "' in pack '" + packId + "'");
												continue Block;
											}
										} else {
											ContinuityClient.LOGGER.warn("Invalid block property definition for block '" + blockId + "' in '" + propertyKey + "' element '" + blockStateStr + "' at index " + i + " in file '" + fileLocation + "' in pack '" + packId + "'");
											continue Block;
										}
									}
								}

								if (!propertyMap.isEmpty()) {
									Object2ObjectOpenHashMap<Property<?>, ObjectOpenHashSet<Comparable<?>>> existingPropertyMap = propertyMaps.get(block);
									if (existingPropertyMap == null) {
										propertyMaps.put(block, propertyMap);
									} else {
										propertyMap.forEach((property, valueSet) -> {
											ObjectOpenHashSet<Comparable<?>> existingValueSet = existingPropertyMap.get(property);
											if (existingValueSet == null) {
												existingPropertyMap.put(property, valueSet);
											} else {
												existingValueSet.addAll(valueSet);
											}
										});
									}
								}
							} else {
								blockSet.add(block);
								propertyMaps.remove(block);
							}
						}
					} else {
						ContinuityClient.LOGGER.warn("Unknown block '" + blockId + "' in '" + propertyKey + "' element '" + blockStateStr + "' at index " + i + " in file '" + fileLocation + "' in pack '" + packId + "'");
					}
				} else {
					ContinuityClient.LOGGER.warn("Invalid '" + propertyKey + "' element '" + blockStateStr + "' at index " + i + " in file '" + fileLocation + "' in pack '" + packId + "'");
				}
			}

			if (!blockSet.isEmpty() || !propertyMaps.isEmpty()) {
				if (propertyMaps.isEmpty()) {
					if (blockSet.size() == 1) {
						Block block = blockSet.toArray(Block[]::new)[0];
						return state -> state.getBlock() == block;
					} else {
						blockSet.trim();
						return state -> blockSet.contains(state.getBlock());
					}
				} else {
					Reference2ReferenceOpenHashMap<Block, Predicate<BlockState>> predicateMap = new Reference2ReferenceOpenHashMap<>();
					blockSet.forEach(block -> {
						predicateMap.put(block, state -> true);
					});
					propertyMaps.forEach((block, propertyMap) -> {
						Map.Entry<Property<?>, ObjectOpenHashSet<Comparable<?>>>[] entryArray = propertyMap.entrySet().toArray((IntFunction<Map.Entry<Property<?>, ObjectOpenHashSet<Comparable<?>>>[]>) Map.Entry[]::new);
						for (Map.Entry<Property<?>, ObjectOpenHashSet<Comparable<?>>> entry : entryArray) {
							entry.getValue().trim();
						}

						predicateMap.put(block, state -> {
							Map<Property<?>, Comparable<?>> targetValueMap = state.getValues();
							for (Map.Entry<Property<?>, ObjectOpenHashSet<Comparable<?>>> entry : entryArray) {
								Comparable<?> targetValue = targetValueMap.get(entry.getKey());
								if (targetValue != null) {
									if (!entry.getValue().contains(targetValue)) {
										return false;
									}
								}
							}
							return true;
						});
					});

					return state -> {
						Predicate<BlockState> predicate = predicateMap.get(state.getBlock());
						return predicate != null && predicate.test(state);
					};
				}
			}
		}
		return EMPTY_BLOCK_STATE_PREDICATE;
	}

	@Nullable
	public static Symmetry parseSymmetry(Properties properties, String propertyKey, ResourceLocation fileLocation, String packId) {
		String symmetryStr = properties.getProperty(propertyKey);
		if (symmetryStr == null) {
			return null;
		}

		try {
			return Symmetry.valueOf(symmetryStr.trim().toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException e) {
			ContinuityClient.LOGGER.warn("Unknown '" + propertyKey + "' value '" + symmetryStr + "' in file '" + fileLocation + "' in pack '" + packId + "'");
		}
		return null;
	}

	@Nullable
	public static OrientationMode parseOrientationMode(Properties properties, String propertyKey, ResourceLocation fileLocation, String packId) {
		String orientationModeStr = properties.getProperty(propertyKey);
		if (orientationModeStr == null) {
			return null;
		}

		try {
			return OrientationMode.valueOf(orientationModeStr.trim().toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException e) {
			ContinuityClient.LOGGER.warn("Unknown '" + propertyKey + "' value '" + orientationModeStr + "' in file '" + fileLocation + "' in pack '" + packId + "'");
		}
		return null;
	}

	public static boolean parseOptifineOnly(Properties properties, ResourceLocation fileLocation) {
		if (!fileLocation.getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE)) {
			return false;
		}

		String optifineOnlyStr = properties.getProperty("optifineOnly");
		if (optifineOnlyStr == null) {
			return false;
		}

		return Boolean.parseBoolean(optifineOnlyStr.trim());
	}
}
