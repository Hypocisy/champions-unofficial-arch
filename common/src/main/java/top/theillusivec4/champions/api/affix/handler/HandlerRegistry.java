package top.theillusivec4.champions.api.affix.handler;

import net.minecraft.world.entity.ai.goal.GoalSelector;
import top.theillusivec4.champions.api.affix.AffixInstance;
import top.theillusivec4.champions.api.affix.IAffixData;
import top.theillusivec4.champions.api.champion.Champion;

import java.util.*;

/**
 * Registry of event handlers for a single {@link top.theillusivec4.champions.api.affix.AffixType}.
 *
 * <p>Each {@code AffixType} owns one {@code HandlerRegistry}. Handlers are registered once during
 * {@link top.theillusivec4.champions.api.affix.AffixType#registerHandlers(HandlerRegistry)} and
 * are never modified afterwards.</p>
 *
 * <p>Dispatch is performed by {@code GlobalDispatcher} on the server. The registry is not
 * thread-safe after initialization — registering handlers after startup is not supported.</p>
 *
 * @param <D> the affix's per-champion data type
 */
public final class HandlerRegistry<D extends IAffixData> {

    private final Map<Class<?>, List<AffixHandler<D, ?>>> handlers = new HashMap<>();
    private final List<GoalHandler<D>> goalHandlers = new ArrayList<>();

    /**
     * Register a handler for internal event type {@code E}.
     *
     * <pre>{@code
     * registry.on(HurtEvent.class, (champion, data, strength, evt) -> {
     *     data.accumulatedDamage += evt.originalDamage();
     *     evt.setDamage(evt.currentDamage() * computeReduction(data, strength));
     * });
     * }</pre>
     */
    public <E> void on(Class<E> eventType, AffixHandler<D, E> handler) {
        handlers.computeIfAbsent(eventType, k -> new ArrayList<>()).add(handler);
    }

    /**
     * Register a goal setup/teardown pair.
     *
     * <pre>{@code
     * registry.onGoal(new GoalHandler<>() {
     *     public void setup(Champion c, Data d, int s, GoalSelector gs) {
     *         gs.addGoal(2, new SummonSpidersGoal(c.entity(), d));
     *     }
     *     public void teardown(Champion c, Data d, int s, GoalSelector gs) {
     *         gs.removeAllGoals(g -> g instanceof SummonSpidersGoal);
     *     }
     * });
     * }</pre>
     */
    public void onGoal(GoalHandler<D> handler) {
        goalHandlers.add(handler);
    }

    // ── Internal dispatch ────────────────────────────────────────────────────

    /**
     * Dispatch {@code event} to all handlers registered for {@code eventType}.
     * Called by {@code GlobalDispatcher} — not for direct use by affix implementations.
     */
    @SuppressWarnings("unchecked")
    public <E> void dispatch(
            Class<E> eventType,
            Champion champion,
            AffixInstance instance,
            D data,
            E event
    ) {
        List<AffixHandler<D, ?>> list = handlers.get(eventType);
        if (list == null) return;
        for (AffixHandler<D, ?> h : list) {
            ((AffixHandler<D, E>) h).handle(champion, data, instance.strength(), event);
        }
    }

    /**
     * Trigger goal setup for all registered goal handlers.
     * Called by {@code IChampion.Server#addAffix()} and on entity join after load.
     */
    public void setupGoals(
            Champion champion,
            AffixInstance instance,
            D data,
            GoalSelector goalSelector
    ) {
        for (GoalHandler<D> h : goalHandlers) {
            h.setup(champion, data, instance.strength(), goalSelector);
        }
    }

    /**
     * Trigger goal teardown for all registered goal handlers.
     * Called by {@code IChampion.Server#removeAffix()}.
     */
    public void teardownGoals(
            Champion champion,
            AffixInstance instance,
            D data,
            GoalSelector goalSelector
    ) {
        for (GoalHandler<D> h : goalHandlers) {
            h.teardown(champion, data, instance.strength(), goalSelector);
        }
    }

    @SuppressWarnings("unused")
    public boolean hasEventHandlers() {
        return !handlers.isEmpty();
    }

    @SuppressWarnings("unused")
    public boolean hasGoalHandlers() {
        return !goalHandlers.isEmpty();
    }

    /**
     * Returns an unmodifiable view of registered event types. For tooling/debugging only.
     */
    public Map<Class<?>, List<AffixHandler<D, ?>>> getHandlers() {
        return Collections.unmodifiableMap(handlers);
    }
}
