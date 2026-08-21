package top.theillusivec4.champions.common.affix.builtin.StatelessCombatAffixes;

import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import top.theillusivec4.champions.api.affix.AffixType;
import top.theillusivec4.champions.api.affix.EmptyAffixData;
import top.theillusivec4.champions.api.affix.handler.HandlerRegistry;
import top.theillusivec4.champions.api.affix.handler.event.AttackEvent;
import top.theillusivec4.champions.api.affix.handler.event.HurtEvent;
import top.theillusivec4.champions.common.affix.builtin.AffixDefaults;

/**
 * Applies knockback + slowness in two directions:
 *
 * <ol>
 *   <li><b>Attack (active)</b> — when the champion hits a target, the target is knocked back
 *       and slowed. Mirrors the original behaviour of the old project.</li>
 *   <li><b>Hurt (reactive)</b> — when the champion is hit, the attacker is knocked back and
 *       slowed as a defensive recoil.</li>
 * </ol>
 *
 * <p>Knockback force scales with strength for both triggers.</p>
 */
public final class KnockingAffix extends AffixType<EmptyAffixData> {

    @Override
    public void registerHandlers(HandlerRegistry<EmptyAffixData> registry) {

        // Active: champion attacks → knock back target
        registry.on(AttackEvent.class, (champion, data, strength, evt) ->
                applyKnockback(champion.entity(), evt.target(), strength));

        // Reactive: champion is hit → knock back attacker
        registry.on(HurtEvent.class, (champion, data, strength, evt) -> {
            if (!(evt.source().getEntity() instanceof LivingEntity attacker)) return;
            applyKnockback(champion.entity(), attacker, strength);
        });
    }

    private static void applyKnockback(LivingEntity source, LivingEntity target, int strength) {
        float force = (float) AffixDefaults.KNOCKING_KNOCKBACK() * strength;
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2));
        target.knockback(
                force,
                Mth.sin(source.getYRot() * (float) (Math.PI / 180.0)),
                -Mth.cos(source.getYRot() * (float) (Math.PI / 180.0))
        );
    }
}
