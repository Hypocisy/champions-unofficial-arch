package top.theillusivec4.champions.common.phase;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * A phase that a champion can enter when its condition is satisfied.
 *
 * <p>Phases are defined inside {@link top.theillusivec4.champions.common.archetype.ChampionArchetype}
 * and evaluated by {@link PhaseProcessor} every 10 ticks.</p>
 *
 * <h3>Example — enrage at 50% health:</h3>
 * <pre>{@code
 * {
 *   "id": "mymod:enrage",
 *   "condition": { "type": "health_percent", "below": 0.5 },
 *   "effects": [
 *     { "type": "add_affix", "affix": "champions:enkindling", "strength": 3 },
 *     { "type": "add_mob_effect", "effect": "minecraft:speed", "amplifier": 1 }
 *   ],
 *   "repeatable": false
 * }
 * }</pre>
 */
public record ChampionPhase(
        ResourceLocation id,
        PhaseCondition condition,
        List<PhaseEffect> effects,
        boolean repeatable
) {

    public static final Codec<ChampionPhase> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ResourceLocation.CODEC
                    .fieldOf("id").forGetter(ChampionPhase::id),
            PhaseCondition.CODEC
                    .fieldOf("condition").forGetter(ChampionPhase::condition),
            PhaseEffect.CODEC.listOf()
                    .fieldOf("effects").forGetter(ChampionPhase::effects),
            Codec.BOOL
                    .optionalFieldOf("repeatable", false).forGetter(ChampionPhase::repeatable)
    ).apply(inst, ChampionPhase::new));
}
