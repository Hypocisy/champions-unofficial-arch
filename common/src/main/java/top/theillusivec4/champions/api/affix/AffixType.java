package top.theillusivec4.champions.api.affix;

import top.theillusivec4.champions.api.affix.handler.HandlerRegistry;

import java.util.function.BiConsumer;

/**
 * Defines the behavior of a champion affix.
 *
 * <p>Subclass this to create a new affix type. One instance of each subclass exists in the
 * registry — it is a stateless singleton that defines behavior, not per-champion state.
 * Per-champion state lives in {@link IAffixData}, held by {@link AffixInstance}.</p>
 *
 * <p>The type parameter {@code D} is the affix's per-champion data type. Affix types that
 * carry no per-champion state should use {@code AffixType<EmptyAffixData>} and do not need
 * to override {@link #createData()}.</p>
 *
 * <h3>Implementing a new affix:</h3>
 * <pre>{@code
 * public class AdaptableAffix extends AffixType<AdaptableAffix.Data> {
 *
 *     @Override
 *     public Data createData() { return new Data(); }
 *
 *     @Override
 *     public void registerHandlers(HandlerRegistry<Data> registry) {
 *         registry.on(HurtEvent.class, (champion, data, strength, evt) -> {
 *             data.accumulatedDamage += evt.originalDamage();
 *             float reduction = Math.min(data.accumulatedDamage / (100f / strength), 0.8f);
 *             evt.setDamage(evt.currentDamage() * (1f - reduction));
 *         });
 *     }
 *
 *     public static class Data implements IAffixData {
 *         public float accumulatedDamage = 0f;
 *
 *         @Override public void write(CompoundTag tag) { tag.putFloat("dmg", accumulatedDamage); }
 *         @Override public void read(CompoundTag tag)  { accumulatedDamage = tag.getFloat("dmg"); }
 *     }
 * }
 * }</pre>
 *
 * @param <D> the per-champion data type for this affix
 */
public abstract class AffixType<D extends IAffixData> {

    private final HandlerRegistry<D> registry = new HandlerRegistry<>();
    private boolean handlersRegistered = false;

    // ── Data factory ──────────────────────────────────────────────────────────

    /**
     * Create a fresh per-champion data object for a new {@link AffixInstance}.
     *
     * <p>Override this if your affix carries per-champion state. The default returns
     * {@link EmptyAffixData#INSTANCE}, which is correct for stateless affixes.</p>
     */
    @SuppressWarnings("unchecked")
    public D createData() {
        return (D) EmptyAffixData.INSTANCE;
    }

    /**
     * Type-safe accessor for the data held by {@code instance}.
     *
     * <p>The cast is safe because only this affix type creates instances via
     * {@link #createData()}, and {@link AffixInstance} only accepts data from its owning type.
     * The cast is encapsulated here so callers never need to cast themselves.</p>
     */
    @SuppressWarnings("unchecked")
    public final D getData(AffixInstance instance) {
        return (D) instance.data();
    }

    // ── Handler registration ──────────────────────────────────────────────────

    /**
     * Register event handlers for this affix type.
     *
     * <p>Called once per affix type when it is added to the registry. Override this to declare
     * which events this affix responds to. Do not call this method directly.</p>
     *
     * <pre>{@code
     * @Override
     * public void registerHandlers(HandlerRegistry<Data> registry) {
     *     registry.on(HurtEvent.class, (champion, data, strength, evt) -> { ... });
     *     registry.on(DeathEvent.class, (champion, data, strength, evt) -> { ... });
     *     registry.onGoal(new MyGoalHandler());
     * }
     * }</pre>
     */
    public void registerHandlers(HandlerRegistry<D> registry) {
        // no handlers by default
    }

    /**
     * Called by the registry infrastructure after this type is registered.
     * Ensures {@link #registerHandlers} is called exactly once.
     * Not for direct use.
     */
    public final void initHandlers() {
        if (!handlersRegistered) {
            registerHandlers(registry);
            handlersRegistered = true;
        }
    }

    public final HandlerRegistry<D> getRegistry() {
        return registry;
    }

    public final void execute(AffixInstance instance, BiConsumer<HandlerRegistry<D>, D> action) {
        action.accept(registry, getData(instance)); // D는 내부에서만 쓰임
    }
}
