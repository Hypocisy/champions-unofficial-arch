package top.theillusivec4.champions.neoforge.kubejs;

import top.theillusivec4.champions.api.affix.AffixType;
import top.theillusivec4.champions.api.affix.EmptyAffixData;
import top.theillusivec4.champions.api.affix.handler.HandlerRegistry;
import top.theillusivec4.champions.api.affix.handler.event.*;
import top.theillusivec4.champions.api.champion.Champion;

/**
 * An {@link AffixType} whose handlers are set by KubeJS script callbacks.
 *
 * <p>Scripts build a {@link Builder} (fluent API), then the registry instantiates
 * this type from the builder. Callbacks are stored as fields and registered into
 * the {@link HandlerRegistry} exactly once on registry add.</p>
 *
 * <h3>KubeJS usage (JS):</h3>
 * <pre>{@code
 * StartupEvents.registry('champions:affix_type', event => {
 *     event.create('myscript:shock_wave')
 *         .onAttack((champion, strength, evt) => {
 *             evt.target().knockback(strength * 0.5, 0, 0);
 *         })
 *         .onHurt((champion, strength, evt) => {
 *             evt.setDamage(evt.currentDamage() * (1 - strength * 0.1));
 *         });
 * });
 * }</pre>
 */
public final class ScriptableAffixType extends AffixType<EmptyAffixData> {

    // All fields are nullable — only non-null callbacks are registered as handlers.

    private final TriConsumer<Champion, Integer, SpawnEvent> onSpawn;
    private final TriConsumer<Champion, Integer, TickEvent> onTick;
    private final TriConsumer<Champion, Integer, AttackEvent> onAttack;
    private final TriConsumer<Champion, Integer, HurtEvent> onHurt;
    private final TriConsumer<Champion, Integer, DamageEvent> onDamage;
    private final TriConsumer<Champion, Integer, HealEvent> onHeal;
    private final TriConsumer<Champion, Integer, DeathEvent> onDeath;

    private ScriptableAffixType(Builder builder) {
        this.onSpawn = builder.onSpawn;
        this.onTick = builder.onTick;
        this.onAttack = builder.onAttack;
        this.onHurt = builder.onHurt;
        this.onDamage = builder.onDamage;
        this.onHeal = builder.onHeal;
        this.onDeath = builder.onDeath;
    }

    @Override
    public void registerHandlers(HandlerRegistry<EmptyAffixData> registry) {
        if (onSpawn != null) registry.on(SpawnEvent.class, (c, d, s, e) -> onSpawn.accept(c, s, e));
        if (onTick != null) registry.on(TickEvent.class, (c, d, s, e) -> onTick.accept(c, s, e));
        if (onAttack != null) registry.on(AttackEvent.class, (c, d, s, e) -> onAttack.accept(c, s, e));
        if (onHurt != null) registry.on(HurtEvent.class, (c, d, s, e) -> onHurt.accept(c, s, e));
        if (onDamage != null) registry.on(DamageEvent.class, (c, d, s, e) -> onDamage.accept(c, s, e));
        if (onHeal != null) registry.on(HealEvent.class, (c, d, s, e) -> onHeal.accept(c, s, e));
        if (onDeath != null) registry.on(DeathEvent.class, (c, d, s, e) -> onDeath.accept(c, s, e));
    }

    // ── Functional interface ──────────────────────────────────────────────────

    /**
     * Three-argument consumer — champion, strength, event. Used for all script callbacks.
     */
    @FunctionalInterface
    public interface TriConsumer<A, B, C> {
        void accept(A a, B b, C c);
    }

    // ── Builder ───────────────────────────────────────────────────────────────

    /**
     * Fluent builder — KubeJS scripts call methods on this, then the registry
     * creates a {@link ScriptableAffixType} from it.
     */
    public static final class Builder {

        private TriConsumer<Champion, Integer, SpawnEvent> onSpawn;
        private TriConsumer<Champion, Integer, TickEvent> onTick;
        private TriConsumer<Champion, Integer, AttackEvent> onAttack;
        private TriConsumer<Champion, Integer, HurtEvent> onHurt;
        private TriConsumer<Champion, Integer, DamageEvent> onDamage;
        private TriConsumer<Champion, Integer, HealEvent> onHeal;
        private TriConsumer<Champion, Integer, DeathEvent> onDeath;

        // Fluent setters — each returns `this` for chaining in JS

        /**
         * Called when the champion is first assigned this affix.
         */
        public Builder onSpawn(TriConsumer<Champion, Integer, SpawnEvent> cb) {
            onSpawn = cb;
            return this;
        }

        /**
         * Called every server tick. Use {@code evt.every(n)} to throttle.
         */
        public Builder onTick(TriConsumer<Champion, Integer, TickEvent> cb) {
            onTick = cb;
            return this;
        }

        /**
         * Called when the champion attacks another entity.
         */
        public Builder onAttack(TriConsumer<Champion, Integer, AttackEvent> cb) {
            onAttack = cb;
            return this;
        }

        /**
         * Called when the champion is about to take damage (pre-reduction).
         */
        public Builder onHurt(TriConsumer<Champion, Integer, HurtEvent> cb) {
            onHurt = cb;
            return this;
        }

        /**
         * Called after damage has been reduced but before it is applied.
         */
        public Builder onDamage(TriConsumer<Champion, Integer, DamageEvent> cb) {
            onDamage = cb;
            return this;
        }

        /**
         * Called when the champion is about to be healed.
         */
        public Builder onHeal(TriConsumer<Champion, Integer, HealEvent> cb) {
            onHeal = cb;
            return this;
        }

        /**
         * Called when the champion is about to die. Cancel to prevent death.
         */
        public Builder onDeath(TriConsumer<Champion, Integer, DeathEvent> cb) {
            onDeath = cb;
            return this;
        }

        /**
         * Build the final affix type. Called by the registry infrastructure.
         */
        public ScriptableAffixType build() {
            return new ScriptableAffixType(this);
        }
    }
}
