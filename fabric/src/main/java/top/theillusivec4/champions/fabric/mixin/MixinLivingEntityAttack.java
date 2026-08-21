package top.theillusivec4.champions.fabric.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.theillusivec4.champions.fabric.event.FabricChampionEventsHandler;

/**
 * Fabric lacks a "living entity attacks another entity" event equivalent to
 * NeoForge's LivingAttackEvent (from the attacker's perspective).
 *
 * <p>We inject into {@code LivingEntity#doHurtTarget} to fire our internal
 * {@link top.theillusivec4.champions.api.affix.handler.event.AttackEvent} whenever
 * a champion entity successfully hits a target.</p>
 */
@Mixin(LivingEntity.class)
public abstract class MixinLivingEntityAttack {

    /**
     * Inject after the vanilla attack succeeds.
     * The {@code RETURN} target fires on every return path, including early returns —
     * we only care about the successful hit, so we check {@code cir.getReturnValue()}.
     */
    @Inject(
            method = "doHurtTarget",
            at = @At("RETURN")
    )
    private void champions$onDoHurtTarget(
            Entity target,
            CallbackInfoReturnable<Boolean> cir
    ) {
        // Only dispatch when the attack actually connected
        if (!cir.getReturnValue()) return;

        LivingEntity self = (LivingEntity) (Object) this;
        if (self.level().isClientSide()) return;

        // Build a synthetic DamageSource — the actual source is not easily
        // accessible here, so we use the generic "mob" source.
        // If more precision is needed, an @ModifyVariable on the hurt call can
        // capture the real source before doHurtTarget returns.
        DamageSource source = self.damageSources().mobAttack(self);

        FabricChampionEventsHandler.dispatchAttack(self, (LivingEntity) target, source, 0f);
    }
}
