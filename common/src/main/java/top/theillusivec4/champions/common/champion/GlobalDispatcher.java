package top.theillusivec4.champions.common.champion;

import net.minecraft.nbt.CompoundTag;
import top.theillusivec4.champions.api.affix.AffixInstance;
import top.theillusivec4.champions.api.affix.AffixType;
import top.theillusivec4.champions.api.affix.IAffixClientSync;
import top.theillusivec4.champions.api.affix.IAffixData;
import top.theillusivec4.champions.api.affix.handler.HandlerRegistry;
import top.theillusivec4.champions.api.champion.Champion;

import java.util.List;

/**
 * Central dispatch point for internal champion events.
 *
 * <p>The platform event bridge (NeoForge {@code ChampionEventsHandler} /
 * Fabric equivalent) translates platform events into internal event wrappers,
 * then calls {@link #dispatch} here. This class forwards to each live affix's
 * {@link HandlerRegistry} — no platform code, no affix-specific logic.</p>
 *
 * <h3>Dispatch path:</h3>
 * <pre>
 * Platform event (LivingIncomingDamageEvent)
 *   → ChampionEventsHandler.onLivingHurt()
 *   → new HurtEvent(...)
 *   → GlobalDispatcher.dispatch(HurtEvent.class, champion, event)
 *   → for each AffixInstance:
 *       HandlerRegistry.dispatch(HurtEvent.class, champion, instance, data, event)
 *       → AffixHandler.handle(champion, data, strength, event)
 * </pre>
 *
 * <h3>Type safety:</h3>
 * <p>The outer {@link #dispatch} method is fully typed ({@code Class<E>}, {@code E event}).
 * The inner {@link #dispatchToInstance} does one unchecked cast to extract {@code D} from
 * the affix type — safe because only that type's {@code createData()} ever produces
 * instances stored in {@link AffixInstance}.</p>
 */
public final class GlobalDispatcher {

    private GlobalDispatcher() {
    }

    /**
     * Dispatch {@code event} to every live affix on {@code champion} that has registered
     * a handler for {@code eventType}.
     *
     * <p>Affixes without a handler for this event type are skipped in O(1) per instance
     * (HashMap lookup in {@link HandlerRegistry}).</p>
     *
     * @param eventType the internal event class (e.g. {@code HurtEvent.class})
     * @param champion  the champion receiving the event — must be server-side
     * @param event     the event payload
     * @param <E>       event type
     */
    public static <E> void dispatch(Class<E> eventType, Champion champion, E event) {
        ChampionView.Server serverView = champion instanceof ChampionView.Server server
                ? server
                : null;
        List<CompoundTag> runtimeBefore = serverView == null
                ? List.of()
                : snapshotRuntimeData(serverView);
        List<CompoundTag> clientBefore = serverView == null
                ? List.of()
                : snapshotClientData(champion);

        for (AffixInstance instance : champion.affixes()) {
            dispatchToInstance(eventType, champion, instance, event);
        }

        if (serverView != null) {
            boolean runtimeChanged = !runtimeBefore.equals(snapshotRuntimeData(serverView));
            boolean clientChanged = !clientBefore.equals(snapshotClientData(champion));
            if (runtimeChanged || clientChanged) {
                serverView.persistRuntimeState(clientChanged);
            }
        }
    }

    /**
     * Variant that dispatches only to a specific instance.
     * Used when the caller already knows which affix to target
     * (e.g. a phase effect triggering a one-time affix response).
     */
    public static <E> void dispatchTo(
            Class<E> eventType,
            Champion champion,
            AffixInstance instance,
            E event
    ) {
        dispatchToInstance(eventType, champion, instance, event);
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    /**
     * Extract the typed data for {@code instance} and forward to its registry.
     *
     * <p>The {@code (AffixType<D>)} cast is unchecked but safe: the only object ever
     * stored as {@link AffixInstance#data()} is the one produced by
     * {@link AffixType#createData()}, so {@code D} is always the correct concrete type.</p>
     */
    @SuppressWarnings("unchecked")
    private static <D extends IAffixData, E> void dispatchToInstance(
            Class<E> eventType,
            Champion champion,
            AffixInstance instance,
            E event
    ) {
        AffixType<D> type = (AffixType<D>) instance.type();
        HandlerRegistry<D> reg = type.getRegistry();
        D data = type.getData(instance);
        reg.dispatch(eventType, champion, instance, data, event);
    }

    private static List<CompoundTag> snapshotRuntimeData(Champion.Server champion) {
        return champion.baseAffixes().stream()
                .map(AffixInstance::save)
                .toList();
    }

    private static List<CompoundTag> snapshotClientData(Champion champion) {
        return champion.affixes().stream()
                .map(instance -> {
                    CompoundTag tag = new CompoundTag();
                    if (instance.type() instanceof IAffixClientSync sync) {
                        tag = sync.writeClientData(instance);
                    }
                    return tag;
                })
                .toList();
    }
}
