package me.pepperbell.ctm.client.properties.overlay;

import java.util.Locale;
import java.util.Properties;

import org.jetbrains.annotations.Nullable;

import me.pepperbell.ctm.client.ContinuityClient;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class OverlayPropertiesSection {
	protected Properties properties;
	protected ResourceLocation resourceId;
	protected String packId;

	protected int tintIndex = -1;
	@Nullable
	protected BlockState tintBlock;
	protected BlendMode layer = BlendMode.CUTOUT_MIPPED;

	public OverlayPropertiesSection(Properties properties, ResourceLocation resourceId, String packId) {
		this.properties = properties;
		this.resourceId = resourceId;
		this.packId = packId;
	}

	public void init() {
		parseTintIndex();
		parseTintBlock();
		parseLayer();
	}

	protected void parseTintIndex() {
		String tintIndexStr = properties.getProperty("tintIndex");
		if (tintIndexStr == null) {
			return;
		}

		try {
			int tintIndex = Integer.parseInt(tintIndexStr.trim());
			if (tintIndex >= 0) {
				this.tintIndex = tintIndex;
				return;
			}
		} catch (NumberFormatException e) {
			//
		}
		ContinuityClient.LOGGER.warn("Invalid 'tintIndex' value '" + tintIndexStr + "' in file '" + resourceId + "' in pack '" + packId + "'");
	}

	protected void parseTintBlock() {
		String tintBlockStr = properties.getProperty("tintBlock");
		if (tintBlockStr == null) {
			return;
		}

		String[] parts = tintBlockStr.trim().split(":", 3);
		if (parts.length != 0) {
			ResourceLocation blockId;
			try {
				if (parts.length == 1 || parts[1].contains("=")) {
					blockId = ResourceLocation.withDefaultNamespace(parts[0]);
				} else {
					blockId = ResourceLocation.fromNamespaceAndPath(parts[0], parts[1]);
				}
			} catch (ResourceLocationException e) {
				ContinuityClient.LOGGER.warn("Invalid 'tintBlock' value '" + tintBlockStr + "' in file '" + resourceId + "' in pack '" + packId + "'", e);
				return;
			}

			if (BuiltInRegistries.BLOCK.containsKey(blockId)) {
				Block block = BuiltInRegistries.BLOCK.get(blockId);
				tintBlock = block.defaultBlockState();
			} else {
				ContinuityClient.LOGGER.warn("Unknown block '" + blockId + "' in 'tintBlock' value '" + tintBlockStr + "' in file '" + resourceId + "' in pack '" + packId + "'");
			}
		} else {
			ContinuityClient.LOGGER.warn("Invalid 'tintBlock' value '" + tintBlockStr + "' in file '" + resourceId + "' in pack '" + packId + "'");
		}
	}

	protected void parseLayer() {
		String layerStr = properties.getProperty("layer");
		if (layerStr == null) {
			return;
		}

		String layerStr1 = layerStr.trim().toLowerCase(Locale.ROOT);
		switch (layerStr1) {
			case "cutout_mipped" -> layer = BlendMode.CUTOUT_MIPPED;
			case "cutout" -> layer = BlendMode.CUTOUT;
			case "translucent" -> layer = BlendMode.TRANSLUCENT;
			default -> ContinuityClient.LOGGER.warn("Unknown 'layer' value '" + layerStr + " in file '" + resourceId + "' in pack '" + packId + "'");
		}
	}

	public int getTintIndex() {
		return tintIndex;
	}

	@Nullable
	public BlockState getTintBlock() {
		return tintBlock;
	}

	public BlendMode getLayer() {
		return layer;
	}

	public interface Provider {
		OverlayPropertiesSection getOverlayPropertiesSection();
	}
}
