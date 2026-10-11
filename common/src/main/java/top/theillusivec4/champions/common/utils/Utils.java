package top.theillusivec4.champions.common.utils;

import net.minecraft.resources.Identifier;

public class Utils {
	public static final String MOD_ID = "champions";

	/**
	 * Create identifier with default id
	 * @param path the path of the id
	 * @return path with default mod id
	 */
	public static Identifier getLocation(final String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

}
