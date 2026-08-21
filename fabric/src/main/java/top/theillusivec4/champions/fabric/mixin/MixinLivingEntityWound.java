package top.theillusivec4.champions.fabric.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import top.theillusivec4.champions.common.effect.ChampionEffects;

/**
 * Applies the Wound damage-amplification effect on Fabric.
 *
 * <p>NeoForge handles this via {@code LivingDamageEvent.Pre} in
 * {@code ChampionEventsHandler#onWoundedDamage}. Fabric has no equivalent global
 * pre-application damage event for non-champion entities, so we hook
 * {@link LivingEntity#actuallyHurt} instead — the {@code damageAmount} parameter
 * at the {@code HEAD} is still the pre-reduction value, since armour and magic
 * absorption are applied further down inside {@code actuallyHurt}. Amplifying here
 * therefore scales damage before reduction; because armour reduction is
 * percentage-based the end result matches NeoForge's post-reduction
 * {@code getNewDamage() * 1.5f}.</p>
 */
@Mixin(LivingEntity.class)
public abstract class MixinLivingEntityWound {

    @ModifyVariable(
            method = "actuallyHurt",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0)
    private float champions$amplifyWoundDamage(float amount, DamageSource source) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self.level().isClientSide()) return amount;
        if (!ChampionEffects.hasWound(self)) return amount;
        return amount * 1.5f;
    }
}
