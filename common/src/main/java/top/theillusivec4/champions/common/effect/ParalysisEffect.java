package top.theillusivec4.champions.common.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * Paralysis debuff applied by {@link top.theillusivec4.champions.common.affix.builtin.StatelessCombatAffixes.ParalyzingAffix}.
 *
 * <p>While active the target cannot move or attack (implemented by applying
 * {@link net.minecraft.world.effect.MobEffects#MOVEMENT_SLOWDOWN} level 5 and
 * {@link net.minecraft.world.effect.MobEffects#WEAKNESS} level 255 on tick).
 * The effect is purely a marker — the actual debuff stacking is handled inside
 * {@link #applyEffectTick} below.</p>
 */
public final class ParalysisEffect extends MobEffect {

    public ParalysisEffect() {
        super(MobEffectCategory.HARMFUL, 0x9966FF);  // purple
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true; // apply every tick
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // Force velocity to 0 so the entity cannot move while paralysed.
        // Slowness level 6 (255) effectively locks movement.
        entity.setDeltaMovement(
                entity.getDeltaMovement().multiply(0.0, 0.0, 0.0)
        );
    }

    @Override
    public boolean isInstantenous() {
        return false;
    }
}
