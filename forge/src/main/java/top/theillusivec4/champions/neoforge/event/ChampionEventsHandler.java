package top.theillusivec4.champions.neoforge.event;


import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import top.theillusivec4.champions.api.ChampionsApi;
import top.theillusivec4.champions.api.affix.AffixInstance;
import top.theillusivec4.champions.api.affix.handler.event.*;
import top.theillusivec4.champions.api.champion.Champion;
import top.theillusivec4.champions.common.api.ChampionsRegistries;
import top.theillusivec4.champions.common.champion.ChampionLootHandler;
import top.theillusivec4.champions.common.champion.ChampionView;
import top.theillusivec4.champions.common.champion.GlobalDispatcher;
import top.theillusivec4.champions.common.config.ChampionsConfig;
import top.theillusivec4.champions.common.effect.ChampionEffects;
import top.theillusivec4.champions.common.network.ChampionSyncData;
import top.theillusivec4.champions.common.network.ChampionSyncPacket;
import top.theillusivec4.champions.common.phase.PhaseProcessor;
import top.theillusivec4.champions.neoforge.ChampionsNeoForge;
import top.theillusivec4.champions.neoforge.network.ForgePacketHandler;

import java.util.HashSet;
import java.util.List;

/**
 * NeoForge event bridge.
 *
 * <p>Each method does exactly two things:</p>
 * <ol>
 *   <li>Guard: return early if the entity is not a champion.</li>
 *   <li>Translate: wrap the NeoForge event into an internal event and call
 *       {@link GlobalDispatcher#dispatch}.</li>
 * </ol>
 *
 * <p>No affix logic lives here. No platform-specific types leak past this class.</p>
 */
@Mod.EventBusSubscriber(modid = ChampionsNeoForge.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ChampionEventsHandler {

    private ChampionEventsHandler() {
    }

    // ── Hurt (incoming damage, before reduction) ──────────────────────────────

    @SubscribeEvent(priority = EventPriority.NORMAL)
    static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;

        ChampionsApi.get().getChampion(entity).ifPresent(champion ->
                GlobalDispatcher.dispatch(HurtEvent.class, champion,
                        new HurtEvent(
                                event.getSource(),
                                event.getAmount(),
                                event.getAmount(),
                                event::setAmount,
                                () -> event.setCanceled(true)
                        ))
        );
    }

    // ── Damage (after reduction, before application) ──────────────────────────

    @SubscribeEvent(priority = EventPriority.NORMAL)
    static void onLivingDamage(LivingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;

        ChampionsApi.get().getChampion(entity).ifPresent(champion ->
                GlobalDispatcher.dispatch(DamageEvent.class, champion,
                        new DamageEvent(
                                event.getSource(),
                                event.getAmount(),
                                event::setAmount
                        ))
        );
    }

    // ── Attack (champion attacks another entity) ──────────────────────────────

    @SubscribeEvent(priority = EventPriority.NORMAL)
    static void onLivingAttack(LivingAttackEvent event) {
        // We want the attacker, not the victim
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) return;
        if (attacker.level().isClientSide()) return;

        ChampionsApi.get().getChampion(attacker).ifPresent(champion ->
                GlobalDispatcher.dispatch(AttackEvent.class, champion,
                        new AttackEvent(
                                event.getEntity(),
                                event.getSource(),
                                event.getAmount(),
                                () -> {
                                }
                        ))
        );
    }

    // ── Heal ──────────────────────────────────────────────────────────────────

    @SubscribeEvent(priority = EventPriority.NORMAL)
    static void onLivingHeal(LivingHealEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;

        ChampionsApi.get().getChampion(entity).ifPresent(champion ->
                GlobalDispatcher.dispatch(HealEvent.class, champion,
                        new HealEvent(
                                event.getAmount(),
                                event::setAmount
                        ))
        );
    }

    // ── Death + Loot ──────────────────────────────────────────────────────────

    @SubscribeEvent(priority = EventPriority.NORMAL)
    static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;
        if (!(entity.level() instanceof ServerLevel level)) return;

        ChampionsApi.get().getChampion(entity).ifPresent(champion -> {
            // Dispatch DeathEvent to affixes (e.g. InfestedAffix burst)
            GlobalDispatcher.dispatch(DeathEvent.class, champion,
                    new DeathEvent(
                            event.getSource(),
                            () -> event.setCanceled(true)
                    ));

            // Drop loot unless the death was cancelled by an affix
            if (!event.isCanceled()) {
                ChampionLootHandler.dropLoot(entity, champion, level, event.getSource());
            }
        });
    }

    // ── XP scaling ────────────────────────────────────────────────────────────

    @SubscribeEvent(priority = EventPriority.NORMAL)
    static void onLivingXpDrop(LivingExperienceDropEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;
        if (ChampionsConfig.experienceGrowth <= 0) return;

        ChampionsApi.get().getChampion(entity).ifPresent(champion -> {
            int tier = champion.tier().level();
            if (tier <= 0) return;
            int bonus = (tier - 1) * ChampionsConfig.experienceGrowth * event.getOriginalExperience();
            event.setDroppedExperience(event.getDroppedExperience() + bonus);
        });
    }

    // ── Explosion scaling ─────────────────────────────────────────────────────

    @SubscribeEvent(priority = EventPriority.NORMAL)
    static void onExplosion(ExplosionEvent.Start event) {
        if (event.getLevel().isClientSide()) return;
        if (ChampionsConfig.explosionGrowth <= 0) return;
        var source = event.getExplosion().getDirectSourceEntity();
        if (!(source instanceof LivingEntity living)) return;

        ChampionsApi.get().getChampion(living).ifPresent(champion -> {
            int tier = champion.tier().level();
            if (tier <= 1) return; // tier 1 gets no bonus
            event.getExplosion().radius +=
                    (float) ChampionsConfig.explosionGrowth * (tier - 1);
        });
    }

    // ── Boss-bar cancel when HUD is rendering ─────────────────────────────────
    // Moved to ClientModEventHandler.GameBus — this client-only event class must
    // never be resolved from a common @EventBusSubscriber (dedicated-server crash).

    // ── Tick ──────────────────────────────────────────────────────────────────

    @SubscribeEvent(priority = EventPriority.NORMAL)
    static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;

        ChampionsApi.get().getChampion(entity).ifPresent(champion -> {
            // Dispatch tick to all affixes every tick
            GlobalDispatcher.dispatch(TickEvent.class, champion,
                    new TickEvent(entity.tickCount));

            // Evaluate phases every 10 ticks
            if (entity.tickCount % 10 == 0 && champion instanceof Champion.Server server) {
                PhaseProcessor.process(
                        server,
                        server instanceof ChampionView.Server serverView
                                ? serverView.getTriggeredPhaseIds() : new HashSet<>()
                );
            }
        });
    }

    // ── Wound global listeners ────────────────────────────────────────────────
    // These apply to ANY LivingEntity that carries the Wound effect,
    // not just champions — matching the original WoundingAffix behaviour.

    /** Wounded targets take 50% more damage. */
    @SubscribeEvent(priority = EventPriority.LOW)
    static void onWoundedDamage(LivingDamageEvent event) {
        if (!ChampionEffects.hasWound(event.getEntity())) return;
        // Scale the damage about to be applied — the pre-reduction value lives on
        // LivingHurtEvent, which DamageEvent affix handlers already adjusted.
        event.setAmount(event.getAmount() * 1.5f);
    }

    /** Wounded targets heal for half the normal amount. */
    @SubscribeEvent(priority = EventPriority.LOW)
    static void onWoundedHeal(LivingHealEvent event) {
        if (!ChampionEffects.hasWound(event.getEntity())) return;
        event.setAmount(event.getAmount() * 0.5f);
    }

    /**
     * 当玩家开始追踪一个实体时，把 champion 数据同步给该玩家。
     * 此时客户端已经收到实体生成包，getEntity() 保证非 null。
     */
    @SubscribeEvent
    static void onStartTracking(PlayerEvent.StartTracking event) {
        if (!(event.getTarget() instanceof LivingEntity living)) return;
        if (!(event.getTarget().level() instanceof ServerLevel)) return;
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;

        ChampionsApi.get().getChampion(living).ifPresent(champion -> {
            ForgePacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() ->serverPlayer),
                    new ChampionSyncPacket(living.getId(), ChampionSyncData.from(champion)));
        });
    }
}
