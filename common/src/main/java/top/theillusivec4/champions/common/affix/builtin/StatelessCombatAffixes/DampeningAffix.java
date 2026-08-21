package top.theillusivec4.champions.common.affix.builtin.StatelessCombatAffixes;

import top.theillusivec4.champions.api.affix.AffixType;
import top.theillusivec4.champions.api.affix.EmptyAffixData;
import top.theillusivec4.champions.api.affix.handler.HandlerRegistry;
import top.theillusivec4.champions.api.affix.handler.event.HurtEvent;
import top.theillusivec4.champions.common.affix.builtin.AffixDefaults;

/**
 * Reduces direct (melee/projectile) damage taken.
 * Magic and environmental damage is not reduced.
 * Scales with strength.
 */
public final class DampeningAffix extends AffixType<EmptyAffixData> {

    @Override
    public void registerHandlers(HandlerRegistry<EmptyAffixData> registry) {
        registry.on(HurtEvent.class, (champion, data, strength, evt) -> {
            if (!evt.source().isDirect()) return;
            float reduction = (float) (AffixDefaults.DAMPENING_REDUCTION() * strength * 0.25);
            evt.setDamage(evt.currentDamage() * (1f - Math.min(reduction, 0.9f)));
        });
    }
}
