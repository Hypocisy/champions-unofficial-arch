package top.theillusivec4.champions.fabric.event;

import dev.architectury.event.events.client.ClientTickEvent;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import top.theillusivec4.champions.common.network.ChampionSyncPacket;
import top.theillusivec4.champions.common.phase.PhaseProcessor;

import java.util.HashSet;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

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
     * When a player enters tracking range of a champion, queue a sync to be sent at
     * the end of the current server tick.
     *
     * <p>START_TRACKING fires at the HEAD of {@code ServerEntity.addPairing} — before
     * vanilla has queued the entity's spawn packet. A sync sent there would arrive at
     * the client before the entity exists and be silently dropped (the original cause
     * of champions only being recognized seconds later). Deferring to END_SERVER_TICK
     * keeps the connection's FIFO order correct — spawn packet first, sync second —
     * while still applying within the same tick.</p>
     */
    private static void registerTracking() {
        EntityTrackingEvents.START_TRACKING.register((trackedEntity, player) -> {
            if (!(trackedEntity instanceof LivingEntity living)) return;

            ChampionsApi.get().getChampion(living).ifPresent(champion ->
                    PENDING_TRACKING_SYNCS.add(new PendingTrackingSync(
                            player,
                            new ChampionSyncPacket(living.getId(), ChampionSyncData.from(champion))
                    ))
            );
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (PENDING_TRACKING_SYNCS.isEmpty()) return;
            for (PendingTrackingSync sync; (sync = PENDING_TRACKING_SYNCS.poll()) != null; ) {
                if (sync.player().hasDisconnected()) continue;
                FriendlyByteBuf buf = PacketByteBufs.create();
                sync.packet().encode(buf);
                ServerPlayNetworking.send(sync.player(), ChampionSyncPacket.ID, buf);
            }
        });
    }

    /** A sync deferred to the end of the tick it was requested in. */
    private record PendingTrackingSync(ServerPlayer player, ChampionSyncPacket packet) {}

    /** Deferred tracking syncs, drained every server tick. */
    private static final Queue<PendingTrackingSync> PENDING_TRACKING_SYNCS =
            new ConcurrentLinkedQueue<>();
}
