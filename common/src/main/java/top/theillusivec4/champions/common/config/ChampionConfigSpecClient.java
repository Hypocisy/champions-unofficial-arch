package top.theillusivec4.champions.common.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class ChampionConfigSpecClient {

	public static final ModConfigSpec SPEC;
	private static final ChampionConfigSpecClient.Entries ENTRIES;

	static {
		Pair<ChampionConfigSpecClient.Entries, ModConfigSpec> pair = new ModConfigSpec.Builder().configure(ChampionConfigSpecClient.Entries::new);
		ENTRIES = pair.getLeft();
		SPEC = pair.getRight();
	}

	private ChampionConfigSpecClient() {
	}

	public static void bakeAndApply() {
		ChampionsClientConfig.Values v = new ChampionsClientConfig.Values();

		v.hudXOffset = ENTRIES.hudXOffset.get();
		v.hudYOffset = ENTRIES.hudYOffset.get();
		v.jadeStarSpacing = ENTRIES.jadeStarSpacing.get();
		v.jadeStarBottomPadding = ENTRIES.jadeStarBottomPadding.get();
		v.hudRange = ENTRIES.hudRange.get();
		v.enableWailaIntegration = ENTRIES.enableWailIntegration.get();

		ChampionsClientConfig.bake(v);
	}

	static final class Entries {
		final ModConfigSpec.IntValue hudXOffset;
		final ModConfigSpec.IntValue hudYOffset;
		final ModConfigSpec.IntValue hudRange;
		final ModConfigSpec.BooleanValue enableWailIntegration;
		final ModConfigSpec.IntValue jadeStarSpacing;
		final ModConfigSpec.IntValue jadeStarBottomPadding;
		final String CONFIG_PREFIX = "gui.champions.config.";

		public Entries(ModConfigSpec.Builder b) {
			b.push("hud");

			hudXOffset = b.comment("The x-offset for the champion HUD")
					.translation(CONFIG_PREFIX + "hudXOffset").defineInRange("hudXOffset", 0, -1000, 1000);

			hudYOffset = b.comment("The y-offset for the champion HUD")
					.translation(CONFIG_PREFIX + "hudYOffset").defineInRange("hudYOffset", 0, -1000, 1000);

			hudRange = b.comment("The distance, in blocks, from which the champion HUD can be seen")
					.comment("Set 0 to use ENTITY_INTERACTION_RANGE attribute")
					.translation(CONFIG_PREFIX + "hudRange").defineInRange("hudRange", 0, 0, 1000);

			enableWailIntegration =
					b.comment("Set to true to move the WAILA(jade) overlay underneath the champion HUD")
							.translation(CONFIG_PREFIX + "enableWailaIntegration")
							.define("enableWailaIntegration", true);
			jadeStarSpacing = b.comment("The Jade Star spacing, when rendering star.").defineInRange("jadeStarSpacing", 2, 0, 25);
			jadeStarBottomPadding = b.comment("The Jade Star bottom padding, when rendering star.").defineInRange("jadeStarBottomPadding", 0, 0, 100);
			b.pop();
		}

	}
}
