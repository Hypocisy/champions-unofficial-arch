package top.theillusivec4.champions.common.affix.builtin.StatelessCombatAffixes;

import top.theillusivec4.champions.api.affix.AffixType;
import top.theillusivec4.champions.api.affix.EmptyAffixData;
import top.theillusivec4.champions.api.affix.handler.HandlerRegistry;
import top.theillusivec4.champions.api.affix.handler.event.AttackEvent;
import top.theillusivec4.champions.common.affix.builtin.AffixDefaults;
import top.theillusivec4.champions.common.effect.ChampionEffects;

/**
 * Chance to apply the Wound debuff on hit.
 *
 * <p>While wounded, the target heals for half and takes 50% more damage.
 * These modifiers are applied by global event listeners in the platform bridges
 * ({@code ChampionEventsHandler} on NeoForge, {@code FabricChampionEventsHandler} on Fabric)
 * that check for the {@code champions:wound} effect on any LivingEntity.</p>
 *
 * <p>Proc chance scales with strength: {@code WOUNDING_CHANCE * min(strength * 0.4, 1)}.</p>
 */
public final class WoundingAffix extends AffixType<EmptyAffixData> {

    @Override
    public void registerHandlers(HandlerRegistry<EmptyAffixData> registry) {
        registry.on(AttackEvent.class, (champion, data, strength, evt) -> {
            float chance = (float) (AffixDefaults.WOUNDING_CHANCE() * Math.min(strength * 0.4f, 1f));
            if (evt.target().getRandom().nextFloat() >= chance) return;

            int duration = (int) (AffixDefaults.WOUNDING_DURATION() * (1 + strength * 0.2));

            ChampionEffects.wound().ifPresentOrElse(
                    effect -> {
                        // Don't re-apply if already wounded — let it expire naturally
                        if (!ChampionEffects.hasWound(evt.target())) {
                            ChampionEffects.applyWound(evt.target(), duration);
                        }
                    },
                    // Fallback if effect not yet registered (should not happen in normal gameplay)
                    () -> {}
            );
        });
    }
}
