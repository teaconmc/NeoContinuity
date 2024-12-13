package me.pepperbell.ctm.client.resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

import org.jetbrains.annotations.Nullable;

import me.pepperbell.ctm.client.ContinuityClient;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

public final class EmissiveSuffixLoader {
	public static final ResourceLocation LOCATION = ResourceLocation.withDefaultNamespace("optifine/emissive.properties");

	private static String emissiveSuffix;

	@Nullable
	public static String getEmissiveSuffix() {
		return emissiveSuffix;
	}

	public static void load(ResourceManager manager) {
		emissiveSuffix = null;

		Optional<Resource> optionalResource = manager.getResource(LOCATION);
		if (optionalResource.isPresent()) {
			Resource resource = optionalResource.get();
			try (InputStream inputStream = resource.open()) {
				Properties properties = new Properties();
				properties.load(inputStream);
				emissiveSuffix = properties.getProperty("suffix.emissive");
			} catch (IOException e) {
				ContinuityClient.LOGGER.error("Failed to load emissive suffix from file '" + LOCATION + "'", e);
			}
		}
	}
}
