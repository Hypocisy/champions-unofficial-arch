package top.theillusivec4.champions.common.config;

public class ChampionsClientConfig {

	public static Integer hudXOffset = 0;
	public static Integer hudYOffset = 0;
	public static Integer jadeStarSpacing = 0;
	public static Integer jadeStarBottomPadding = 0;
	public static Integer hudRange = 0;
	public static Boolean enableWailaIntegration = false;

	private ChampionsClientConfig() {
	}

	public static void bake(Values v) {
		hudXOffset = v.hudXOffset;
		hudYOffset = v.hudYOffset;
		jadeStarSpacing = v.jadeStarSpacing;
		jadeStarBottomPadding = v.jadeStarBottomPadding;
		hudRange = v.hudRange;
		enableWailaIntegration = v.enableWailaIntegration;
	}


	public static final class Values {
		public Integer hudXOffset = 0;
		public Integer hudYOffset = 0;
		public Integer jadeStarSpacing = 2;
		public Integer jadeStarBottomPadding = 2;
		public Integer hudRange = 0;
		public Boolean enableWailaIntegration = false;
	}
}
