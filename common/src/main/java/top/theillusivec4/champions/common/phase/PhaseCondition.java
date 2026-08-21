package top.theillusivec4.champions.common.phase;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import top.theillusivec4.champions.api.ChampionsApi;
import top.theillusivec4.champions.api.champion.Champion;

/**
 * Condition that determines when a {@link ChampionPhase} triggers.
 *
 * <p>Evaluated every 10 ticks per champion. Should be stateless and cheap — any
 * state needed for evaluation should come from the {@link Champion} argument.</p>
 */
public interface PhaseCondition {

    boolean test(Champion champion);

    // ── Codec dispatch ────────────────────────────────────────────────────────

    Codec<PhaseCondition> CODEC = Codec.STRING.dispatch(
            PhaseCondition::typeKey,
            PhaseCondition::codecFor
    );

    private static String typeKey(PhaseCondition condition) {
        if (condition instanceof HealthPercent) return "health_percent";
        if (condition instanceof TimeElapsed) return "time_elapsed";
        if (condition instanceof AffixTriggered) return "affix_triggered";
        throw new IllegalArgumentException("Unknown PhaseCondition: " + condition.getClass());
    }

    private static MapCodec<? extends PhaseCondition> codecFor(String type) {
        return switch (type) {
            case "health_percent" -> HealthPercent.CODEC;
            case "time_elapsed" -> TimeElapsed.CODEC;
            case "affix_triggered" -> AffixTriggered.CODEC;
            default -> throw new IllegalArgumentException("Unknown phase condition type: " + type);
        };
    }

    // ── Built-in implementations ──────────────────────────────────────────────

    /**
     * Triggers when the champion's current health falls below a percentage of max health.
     *
     * <pre>{@code { "type": "health_percent", "below": 0.5 } }</pre>
     */
    record HealthPercent(float below) implements PhaseCondition {

        public static final MapCodec<HealthPercent> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Codec.FLOAT.fieldOf("below").forGetter(HealthPercent::below)
        ).apply(inst, HealthPercent::new));

        @Override
        public boolean test(Champion champion) {
            float max = champion.entity().getMaxHealth();
            float current = champion.entity().getHealth();
            return max > 0 && (current / max) < below;
        }
    }

    /**
     * Triggers after the champion has been alive for at least {@code seconds} seconds.
     * Uses the entity's tick count — resets if the entity is unloaded and reloaded.
     *
     * <pre>{@code { "type": "time_elapsed", "seconds": 30 } }</pre>
     */
    record TimeElapsed(int seconds) implements PhaseCondition {

        public static final MapCodec<TimeElapsed> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Codec.INT.fieldOf("seconds").forGetter(TimeElapsed::seconds)
        ).apply(inst, TimeElapsed::new));

        @Override
        public boolean test(Champion champion) {
            return champion.entity().tickCount >= seconds * 20;
        }
    }

    /**
     * Triggers after a specific affix on this champion has had its handler called
     * at least {@code count} times since spawn.
     *
     * <p>Requires the target affix to implement
     * {@link AffixTriggerTracker}
     * on its data class to report the trigger count.</p>
     *
     * <pre>{@code { "type": "affix_triggered", "affix": "champions:adaptable", "count": 3 } }</pre>
     */
    record AffixTriggered(ResourceLocation affixId, int count) implements PhaseCondition {

        public static final MapCodec<AffixTriggered> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                ResourceLocation.CODEC.fieldOf("affix").forGetter(AffixTriggered::affixId),
                Codec.INT.fieldOf("count").forGetter(AffixTriggered::count)
        ).apply(inst, AffixTriggered::new));

        @Override
        public boolean test(Champion champion) {
            return champion.findAffix(
                    ChampionsApi.get()
                            .getAffixType(affixId).orElse(null)
            ).map(instance -> {
                if (instance.data() instanceof AffixTriggerTracker tracker) {
                    return tracker.triggerCount() >= count;
                }
                return false;
            }).orElse(false);
        }
    }
}
