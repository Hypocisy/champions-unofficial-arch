package top.theillusivec4.champions.common.archetype;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Inclusive range of tier levels.
 * Used by {@link AffixPool} and {@link ChampionArchetype} to constrain
 * which tiers they apply to.
 */
public record TierRange(int min, int max) {

    public static final Codec<TierRange> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.INT.optionalFieldOf("min", 1).forGetter(TierRange::min),
            Codec.INT.optionalFieldOf("max", 5).forGetter(TierRange::max)
    ).apply(inst, TierRange::new));

    public static final TierRange ANY = new TierRange(1, Integer.MAX_VALUE);

    public boolean contains(int level) {
        return level >= min && level <= max;
    }
}
