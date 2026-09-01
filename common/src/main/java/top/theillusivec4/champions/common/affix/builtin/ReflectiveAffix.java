package top.theillusivec4.champions.common.affix.builtin;

import net.minecraft.world.entity.LivingEntity;
import top.theillusivec4.champions.api.affix.AffixType;
import top.theillusivec4.champions.api.affix.EmptyAffixData;
import top.theillusivec4.champions.api.affix.handler.HandlerRegistry;
import top.theillusivec4.champions.api.affix.handler.event.DamageEvent;
import top.theillusivec4.champions.common.config.ChampionsConfig;
import top.theillusivec4.champions.common.registry.ModDamageTypes;

/**
 * Reflects a random percentage of incoming damage back to the attacker.
 * Strength increases the reflection ceiling.
 *
 * <p>Uses the {@code champions:reflection} damage type so reflected damage cannot
 * itself be reflected — preventing infinite loops.</p>
 */
public final class ReflectiveAffix extends AffixType<EmptyAffixData> {

    @Override
    public void registerHandlers(HandlerRegistry<EmptyAffixData> registry) {
        registry.on(DamageEvent.class, (champion, data, strength, evt) -> {
            // Avoid reflecting our own reflection (infinite loop guard)
            if (evt.source().is(ModDamageTypes.REFLECTION)) return;

            if (!(evt.source().getDirectEntity() instanceof LivingEntity attacker)) return;

            float minPct = (float) ChampionsConfig.reflectiveMinPercent;
            float maxPct = (float) (ChampionsConfig.reflectiveMaxPercent * (0.5 + strength * 0.1));
            float range = maxPct - minPct;
            float pct = minPct + attacker.getRandom().nextFloat() * range;
            float dmg = (float) Math.min(evt.originalDamage() * pct, ChampionsConfig.reflectiveMax);

            if (!ChampionsConfig.reflectiveLethal && dmg >= attacker.getHealth()) {
                dmg = attacker.getHealth() - 1f;
            }
            if (dmg > 0) {
                attacker.hurt(
                        attacker.level().damageSources().source(ModDamageTypes.REFLECTION, champion.entity()),
                        dmg
                );
            }
        });
    }
}
