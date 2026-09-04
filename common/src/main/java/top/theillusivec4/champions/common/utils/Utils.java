package top.theillusivec4.champions.common.utils;

import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.Set;

public class Utils {
	public static final String MOD_ID = "champions";

	@SuppressWarnings("removal")
	public static ResourceLocation key(final String path) {
		return new ResourceLocation(MOD_ID, path);
	}

	@SuppressWarnings("removal")
	public static ResourceLocation key(final String namespace, final String path) {
		return new ResourceLocation(namespace, path);
	}

	public static Set<ResourceLocation> getLocationSet(final String... path) {
		Set<ResourceLocation> locations = new HashSet<>();
		for (String s : path) {
			locations.add(key(s));
		}
		return locations;
	}

}
