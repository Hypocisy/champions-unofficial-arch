package top.theillusivec4.champions.common.archetype;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import top.theillusivec4.champions.common.filter.EntityFilter;
import top.theillusivec4.champions.common.phase.ChampionPhase;

import java.util.List;

/**
 * Template for a class of champions.
 *
 * <p>Loaded from {@code data/<namespace>/champions/archetype/*.json}.
 * The file name becomes the archetype's id path.</p>
 *
 * <h3>Example — a tank archetype:</h3>
 * <pre>{@code
 * {
 *   "tier_range": { "min": 2, "max": 5 },
 *   "weight": 8,
 *   "entity_filter": {
 *     "type": "mob_category",
 *     "categories": ["monster"]
 *   },
 *   "affix_pools": [
 *     {
 *       "candidates": [
 *         { "affix": "champions:shielding", "weight": 10 },
 *         { "affix": "champions:dampening", "weight": 8 }
 *       ],
 *       "min_count": 1, "max_count": 2
 *     }
 *   ]
 * }
 * }</pre>
 */
public record ChampionArchetype(
        ResourceLocation id,
        TierRange tierRange,
        int weight,
        EntityFilter entityFilter,
        List<AffixPool> affixPools,
        List<ChampionPhase> phases
) {

    /**
     * Codec for the file body. {@code id} is injected from the file path after parsing.
     */
    public static final Codec<ChampionArchetype> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ResourceLocation.CODEC
                    .fieldOf("id").forGetter(ChampionArchetype::id),
            TierRange.CODEC
                    .optionalFieldOf("tier_range", TierRange.ANY).forGetter(ChampionArchetype::tierRange),
            Codec.INT
                    .optionalFieldOf("weight", 10).forGetter(ChampionArchetype::weight),
            EntityFilter.CODEC
                    .optionalFieldOf("entity_filter", EntityFilter.ANY).forGetter(ChampionArchetype::entityFilter),
            AffixPool.CODEC.listOf()
                    .fieldOf("affix_pools").forGetter(ChampionArchetype::affixPools),
            ChampionPhase.CODEC.listOf()
                    .optionalFieldOf("phases", List.of()).forGetter(ChampionArchetype::phases)
    ).apply(inst, ChampionArchetype::new));

    /**
     * Returns true if this archetype can apply to {@code entity} at {@code tierLevel}.
     */
    public boolean matches(LivingEntity entity, int tierLevel) {
        return tierRange.contains(tierLevel) && entityFilter.matches(entity);
    }

    /**
     * Returns the active affix pools for the given tier level.
     */
    public List<AffixPool> getActivePools(int tierLevel) {
        return affixPools.stream()
                .filter(pool -> pool.tierRange().contains(tierLevel))
                .toList();
    }
}
