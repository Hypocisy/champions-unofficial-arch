package top.theillusivec4.champions.common.data;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.MinMaxBounds;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.champions.api.champion.Champion;
import top.theillusivec4.champions.common.filter.EntityFilter;

import java.util.Optional;

/**
 * Filter condition applied to modifier_setting entries.
 *
 * <p>Cross-platform port of the old NeoForge-only condition. The legacy
 * {@code mobList} + {@code permission} (blacklist/whitelist) pair has been replaced
 * with the project's richer {@link EntityFilter} system, which supports entity type,
 * entity tag, mob category, mod id, attribute filters, and {@code all_of}/{@code any_of}
 * composition — the same filter used by archetypes.</p>
 *
 * <p>JSON schema:</p>
 * <pre>{@code
 * "conditions": {
 *   "entity_filter": {
 *     "type": "entity_type",
 *     "types": ["minecraft:creeper"],
 *     "whitelist": false
 *   },
 *   "tier": { "min": 1 },
 *   "affixes": { "values": [], "matches": {}, "count": {} }
 * }
 * }</pre>
 */
public record ChampionModifierCondition(
        EntityFilter entityFilter,
        Optional<MinMaxBounds.Ints> tier,
        Optional<AffixesPredicate> affixes
) {

    public static final MapCodec<ChampionModifierCondition> MAP_CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    EntityFilter.CODEC
                            .optionalFieldOf("entity_filter", EntityFilter.ANY)
                            .forGetter(ChampionModifierCondition::entityFilter),
                    MinMaxBounds.Ints.CODEC
                            .optionalFieldOf("tier")
                            .forGetter(ChampionModifierCondition::tier),
                    AffixesPredicate.CODEC
                            .optionalFieldOf("affixes")
                            .forGetter(ChampionModifierCondition::affixes)
            ).apply(instance, ChampionModifierCondition::new));

    /**
     * Test this condition against a server-side champion.
     *
     * @param champion the champion to test (new API, server view)
     */
    public boolean test(@NotNull Champion.Server champion) {
        int tierLevel = champion.tier().level();
        return entityFilter.matches(champion.entity())
                && tier.map(t -> t.matches(tierLevel)).orElse(true)
                && affixes.map(pred -> pred.matches(champion.affixes())).orElse(true);
    }
}
