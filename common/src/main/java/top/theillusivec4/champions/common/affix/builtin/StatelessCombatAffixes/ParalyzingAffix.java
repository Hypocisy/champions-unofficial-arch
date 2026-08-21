package top.theillusivec4.champions.common.affix.builtin.StatelessCombatAffixes;

import top.theillusivec4.champions.api.affix.AffixType;
import top.theillusivec4.champions.api.affix.EmptyAffixData;
import top.theillusivec4.champions.api.affix.handler.HandlerRegistry;
import top.theillusivec4.champions.api.affix.handler.event.AttackEvent;
import top.theillusivec4.champions.common.affix.builtin.AffixDefaults;
import top.theillusivec4.champions.common.effect.ChampionEffects;

/**
 * Chance to paralyse the target on hit.
 *
 * <p>Paralysis uses the custom {@code champions:paralysis} mob effect which zeroes
 * the target's movement velocity every tick. The effect is a full stop — not just
 * slowness — so it is meaningfully different from a slow debuff.</p>
 *
 * <p>Proc chance: {@code PARALYZING_CHANCE * strength}, capped at 0.95 to always
 * leave a non-zero escape chance. Re-application is suppressed while the target is
 * already paralysed so the duration is not constantly reset.</p>
 */
public final class ParalyzingAffix extends AffixType<EmptyAffixData> {

    @Override
    public void registerHandlers(HandlerRegistry<EmptyAffixData> registry) {
        registry.on(AttackEvent.class, (champion, data, strength, evt) -> {
            // Cap at 0.95 so there's always a 5% escape chance
            float chance = Math.min((float) AffixDefaults.PARALYZING_CHANCE() * strength, 0.95f);
            if (evt.target().getRandom().nextFloat() >= chance) return;

            // Don't reset the timer if already paralysed — let it expire naturally
            if (ChampionEffects.hasParalysis(evt.target())) return;

            ChampionEffects.applyParalysis(evt.target(), AffixDefaults.PARALYZING_DURATION());
        });
    }
}
