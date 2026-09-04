package top.theillusivec4.champions.fabric.mixin;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.theillusivec4.champions.api.ChampionsApi;
import top.theillusivec4.champions.api.affix.handler.event.TickEvent;
import top.theillusivec4.champions.api.champion.Champion;
import top.theillusivec4.champions.common.champion.ChampionView;
import top.theillusivec4.champions.common.champion.GlobalDispatcher;
import top.theillusivec4.champions.common.phase.PhaseProcessor;

import java.util.HashSet;

/**
 * Fabric lacks a "living entity attacks another entity" event equivalent to
 * NeoForge's LivingAttackEvent (from the attacker's perspective).
 *
 * <p>We inject into {@code LivingEntity#doHurtTarget} to fire our internal
 * {@link top.theillusivec4.champions.api.affix.handler.event.AttackEvent} whenever
 * a champion entity successfully hits a target.</p>
 */
@Mixin(LivingEntity.class)
public abstract class MixinLivingEntityTick {
	@Inject(
			method = "tick",
			at = @At("TAIL")
	)
	private void champions$onDoHurtTarget(
			CallbackInfo ci
	) {
		LivingEntity living = (LivingEntity) (Object) this;
		ChampionsApi.get().getChampion(living).ifPresent(champion -> {
			// Dispatch tick to all affixes every tick
			GlobalDispatcher.dispatch(TickEvent.class, champion,
					new TickEvent(living.tickCount));

			// Evaluate phases every 10 ticks
			if (living.tickCount % 10 == 0 && champion instanceof Champion.Server server) {
				PhaseProcessor.process(
						server,
						server instanceof ChampionView.Server serverView
								? serverView.getTriggeredPhaseIds() : new HashSet<>()
				);
			}
		});

	}
}
