package top.theillusivec4.champions.common.archetype;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

/**
 * A pool of candidate affixes active within a specific tier range.
 *
 * <p>Each archetype can have multiple pools, allowing different affix sets per tier.
 * On spawn, all pools whose {@code tierRange} contains the champion's tier level
 * are active and contribute to the draw.</p>
 *
 * <h3>Example — low-tier pool vs high-tier pool in one archetype:</h3>
 * <pre>{@code
 * "affix_pools": [
 *   {
 *     "tier_range": { "min": 1, "max": 2 },
 *     "candidates": [{ "affix": "champions:lively", "weight": 10 }],
 *     "min_count": 1, "max_count": 1
 *   },
 *   {
 *     "tier_range": { "min": 3, "max": 5 },
 *     "candidates": [
 *       { "affix": "champions:lively", "weight": 10, "min_strength": 2, "max_strength": 4 },
 *       { "affix": "champions:shielding", "weight": 6 }
 *     ],
 *     "min_count": 1, "max_count": 2
 *   }
 * ]
 * }</pre>
 */
public record AffixPool(
        TierRange tierRange,
        List<WeightedAffix> candidates,
        int minCount,
        int maxCount
) {

    public static final Codec<AffixPool> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            TierRange.CODEC
                    .optionalFieldOf("tier_range", TierRange.ANY).forGetter(AffixPool::tierRange),
            WeightedAffix.CODEC.listOf()
                    .fieldOf("candidates").forGetter(AffixPool::candidates),
            Codec.INT
                    .optionalFieldOf("min_count", 1).forGetter(AffixPool::minCount),
            Codec.INT
                    .optionalFieldOf("max_count", 1).forGetter(AffixPool::maxCount)
    ).apply(inst, AffixPool::new));
}
