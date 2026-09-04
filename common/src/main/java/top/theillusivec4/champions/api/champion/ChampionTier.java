package top.theillusivec4.champions.api.champion;

import net.minecraft.resources.ResourceLocation;

/**
 * Represents a champion tier level.
 *
 * <p>Tiers are defined via datapack and loaded into the tier registry at startup.
 * The {@code level} field determines ordering — higher means stronger.
 * Visual presentation (particle color, HUD icon) is fully decoupled from gameplay values.</p>
 *
 * <p>Numerical stat bonuses (health, damage) are <em>not</em> properties of the tier itself.
 * They are expressed as affix instances in the affix pool — e.g. a {@code VitalityAffix}
 * with strength proportional to tier level. This keeps tier definition minimal and makes
 * stat scaling fully data-driven.</p>
 */
public final class ChampionTier {

    private final ResourceLocation id;
    private final int level;
    private final TierDisplay display;

    public ChampionTier(ResourceLocation id, int level, TierDisplay display) {
        this.id = id;
        this.level = level;
        this.display = display;
    }

    /**
     * Registry key for this tier.
     */
    public ResourceLocation id() {
        return id;
    }

    /**
     * Numeric tier level used for ordering and range comparisons.
     * Higher values indicate a stronger champion.
     */
    public int level() {
        return level;
    }

    /**
     * Visual presentation data (particle color, HUD icon).
     */
    public TierDisplay display() {
        return display;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ChampionTier other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "ChampionTier[id=" + id + ", level=" + level + "]";
    }

    // ── TierDisplay ───────────────────────────────────────────────────────────

    /**
     * Visual representation of a tier.
     * Used by the client for particle color and HUD rendering.
     */
    public record TierDisplay(
            int color,                    // ARGB packed int
            ResourceLocation icon         // HUD icon texture path
    ) {

        /**
         * Default display used when no datapack entry defines a custom one.
         */
        public static TierDisplay defaultFor(int level) {
            int color = switch (level) {
                case 1 -> 0xFFFFFFFF; // white
                case 2 -> 0xFF55FFFF; // aqua
                case 3 -> 0xFFFFFF55; // yellow
                case 4 -> 0xFFFF5555; // red
                default -> 0xFFFF55FF; // purple (tier 5+)
            };
            return new TierDisplay(color, new ResourceLocation("minecraft", "textures/gui/icons.png"));
        }
    }
}
