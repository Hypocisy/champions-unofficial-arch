package top.theillusivec4.champions.fabric.mixin;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import top.theillusivec4.champions.api.ChampionsApi;
import top.theillusivec4.champions.api.affix.handler.event.HealEvent;
import top.theillusivec4.champions.common.champion.GlobalDispatcher;
import top.theillusivec4.champions.common.effect.ChampionEffects;

/**
 * Fabric API (1.x) has no LivingHealEvent equivalent.
 *
 * <p>We use {@code @ModifyVariable} on the {@code amount} parameter of
 * {@link LivingEntity#heal(float)} to:</p>
 * <ol>
 *   <li>Dispatch {@link HealEvent} to champion affix handlers (e.g. Lively).</li>
 *   <li>Halve the heal amount for any entity carrying the Wound effect —
 *       matching the global {@code LivingHealEvent} listener on NeoForge.</li>
 * </ol>
 */
@Mixin(LivingEntity.class)
public abstract class MixinLivingEntityHeal {

    @ModifyVariable(
            method = "heal",
            at = @At("HEAD"), argsOnly = true)
    private float champions$onHeal(float amount) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self.level().isClientSide()) return amount;

        // 1. Dispatch HealEvent to champion affix handlers
        var champion = ChampionsApi.get().getChampion(self);
        if (champion.isPresent()) {
            float[] result = {amount};
            GlobalDispatcher.dispatch(HealEvent.class, champion.get(),
                    new HealEvent(amount, modified -> result[0] = modified));
            amount = result[0];
        }

        // 2. Wound global modifier — halve healing for any wounded entity
        if (ChampionEffects.hasWound(self)) {
            amount *= 0.5f;
        }

        return amount;
    }
}
