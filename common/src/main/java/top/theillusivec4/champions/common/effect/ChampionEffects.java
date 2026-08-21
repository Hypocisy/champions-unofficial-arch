package top.theillusivec4.champions.common.effect;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Holds the two custom mob effects used by Champions.
 *
 * <p>Platform modules register their own {@code MobEffect} instances and call
 * {@link #register(Supplier, Supplier)} once at startup. All affix code then reads
 * through this holder rather than referencing platform-specific registries.</p>
 */
public final class ChampionEffects {

    private static Supplier<MobEffect> woundSupplier     = null;
    private static Supplier<MobEffect> paralysisSupplier = null;

    private ChampionEffects() {}

    // ── Registration (called by platform module) ──────────────────────────────

    public static void register(Supplier<MobEffect> wound, Supplier<MobEffect> paralysis) {
        woundSupplier     = wound;
        paralysisSupplier = paralysis;
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public static Optional<MobEffect> wound()     { return opt(woundSupplier); }
    public static Optional<MobEffect> paralysis() { return opt(paralysisSupplier); }

    private static Optional<MobEffect> opt(Supplier<MobEffect> s) {
        return Optional.ofNullable(s).map(Supplier::get);
    }

    // ── Convenience helpers ───────────────────────────────────────────────────

    /**
     * Apply the Wound effect to {@code entity} if the effect is registered.
     * Does nothing if not yet registered (safe to call before mod load is done).
     */
    public static void applyWound(LivingEntity entity, int durationTicks) {
        wound().ifPresent(effect ->
                entity.addEffect(new MobEffectInstance(
                        holdFor(effect), durationTicks, 0, false, true)));
    }

    /**
     * Apply the Paralysis effect to {@code entity} if the effect is registered.
     */
    public static void applyParalysis(LivingEntity entity, int durationTicks) {
        paralysis().ifPresent(effect ->
                entity.addEffect(new MobEffectInstance(
                        holdFor(effect), durationTicks, 0, false, true)));
    }

    public static boolean hasWound(LivingEntity entity) {
        return wound().map(e -> entity.hasEffect(holdFor(e))).orElse(false);
    }

    public static boolean hasParalysis(LivingEntity entity) {
        return paralysis().map(e -> entity.hasEffect(holdFor(e))).orElse(false);
    }

    /** Wrap a bare MobEffect in the holder type expected by the effect API. */
    private static Holder<MobEffect> holdFor(MobEffect effect) {
        return BuiltInRegistries.MOB_EFFECT
                .wrapAsHolder(effect);
    }
}
