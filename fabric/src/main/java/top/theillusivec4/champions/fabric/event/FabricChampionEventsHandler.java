package top.theillusivec4.champions.fabric.event;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import top.theillusivec4.champions.api.ChampionsApi;
import top.theillusivec4.champions.api.affix.handler.event.AttackEvent;
import top.theillusivec4.champions.api.affix.handler.event.DeathEvent;
import top.theillusivec4.champions.api.affix.handler.event.HurtEvent;
import top.theillusivec4.champions.api.affix.handler.event.TickEvent;
import top.theillusivec4.champions.api.champion.Champion;
import top.theillusivec4.champions.common.champion.ChampionLootHandler;
import top.theillusivec4.champions.common.champion.ChampionView;
import top.theillusivec4.champions.common.champion.GlobalDispatcher;
import top.theillusivec4.champions.common.network.ChampionSyncData;
import top.theillusivec4.champions.common.phase.PhaseProcessor;
import top.theillusivec4.champions.fabric.network.FabricChampionSyncPacket;

import java.util.HashSet;

/**
 * Fabric event bridge.
 *
 * <p>Same contract as {@code ChampionEventsHandler} on NeoForge — guard + translate,
 * no affix logic. Registered from {@link top.theillusivec4.champions.fabric.ChampionsFabric}.</p>
 */
public final class FabricChampionEventsHandler {

    private FabricChampionEventsHandler() {
    }

    /**
     * Register all event callbacks.
     * Called once from {@link top.theillusivec4.champions.fabric.ChampionsFabric#onInitialize()}.
     */
    public static void register() {
        registerHurt();
        registerDeath();
        registerTick();
        registerTracking();
        // Wound heal-halving is in MixinLivingEntityHeal.
        // Wound damage-amplification is in MixinLivingEntityWound.
    }

    // ── Hurt ──────────────────────────────────────────────────────────────────

    private static void registerHurt() {
        // Fabric: ServerLivingEntityEvents.ALLOW_DAMAGE fires before damage is applied.
        // Return false to cancel. Final amount changes happen in MixinLivingEntityDamage.
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            var champion = ChampionsApi.get().getChampion(entity);
            if (champion.isEmpty()) return true; // not a champion, allow normally

            float[] currentAmount = {amount};
            boolean[] cancelled = {false};

            GlobalDispatcher.dispatch(HurtEvent.class, champion.get(),
                    new HurtEvent(
                            source,
                            amount,
                            currentAmount[0],
                            a -> currentAmount[0] = a,
                            () -> cancelled[0] = true
                    ));

            return !cancelled[0];
        });
    }

    // ── Death + Loot ──────────────────────────────────────────────────────────

    private static void registerDeath() {
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, damageAmount) -> {
            var champion = ChampionsApi.get().getChampion(entity);
            if (champion.isEmpty()) return true;

            boolean[] cancelled = {false};
            GlobalDispatcher.dispatch(DeathEvent.class, champion.get(),
                    new DeathEvent(source, () -> cancelled[0] = true));

            // Drop loot unless the death was cancelled by an affix
            if (!cancelled[0] && entity.level() instanceof ServerLevel level) {
                ChampionLootHandler.dropLoot(entity, champion.get(), level, source);
            }

            return !cancelled[0];
        });
    }

    // ── Tick (with PhaseProcessor) ────────────────────────────────────────────

    /**
     * Drives champion tick + phase evaluation from the server tick event.
     * Fabric has no per-entity tick event; we iterate all loaded entities every server tick.
     */
    private static void registerTick() {
        ServerTickEvents.END_SERVER_TICK.register(mcServer ->
                mcServer.getAllLevels().forEach(level ->
                        level.getAllEntities().forEach(entity -> {
                            if (!(entity instanceof LivingEntity living)) return;
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
                        })
                )
        );
    }

    // ── Attack ────────────────────────────────────────────────────────────────
    // Fabric has no direct "living attacks" event. Attack dispatch is handled
    // via a mixin on LivingEntity#doHurtTarget — see MixinLivingEntityAttack.

    public static void dispatchAttack(LivingEntity attacker, LivingEntity target,
                                      DamageSource source, float amount) {
        ChampionsApi.get().getChampion(attacker).ifPresent(champion ->
                GlobalDispatcher.dispatch(AttackEvent.class, champion,
                        new AttackEvent(target, source, amount, () -> {
                        }))
        );
    }

    // ── Tracking ──────────────────────────────────────────────────────────────

    /**
     * When a player enters tracking range of a champion, send the current sync data
     * directly to that player so they see rank particles and HUD immediately.
     */
    private static void registerTracking() {
        EntityTrackingEvents.START_TRACKING.register((trackedEntity, player) -> {
            if (!(trackedEntity instanceof LivingEntity living)) return;

            ChampionsApi.get().getChampion(living).ifPresent(champion ->
                    ServerPlayNetworking.send(
                            player,
                            new FabricChampionSyncPacket(
                                    living.getId(),
                                    ChampionSyncData.from(champion)
                            )
                    )
            );
        });
    }
}
