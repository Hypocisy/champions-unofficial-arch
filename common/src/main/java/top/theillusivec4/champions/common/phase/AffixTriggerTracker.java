package top.theillusivec4.champions.common.phase;

import top.theillusivec4.champions.api.affix.IAffixData;

/**
 * Optional marker for {@link IAffixData} implementations that want to support
 * the {@link PhaseCondition.AffixTriggered} condition.
 *
 * <p>Implement this on your affix's data class and increment {@code triggerCount}
 * inside a handler lambda whenever the relevant event fires:</p>
 *
 * <pre>{@code
 * public static class Data implements IAffixData, AffixTriggerTracker {
 *     public int triggers = 0;
 *     public float accumulatedDamage = 0f;
 *
 *     @Override public int triggerCount() { return triggers; }
 *
 *     @Override public void write(CompoundTag tag) {
 *         tag.putInt("triggers", triggers);
 *         tag.putFloat("dmg", accumulatedDamage);
 *     }
 *     @Override public void read(CompoundTag tag) {
 *         triggers = tag.getInt("triggers");
 *         accumulatedDamage = tag.getFloat("dmg");
 *     }
 * }
 * }</pre>
 */
public interface AffixTriggerTracker {

    /**
     * How many times this affix's handler has been invoked since spawn.
     */
    int triggerCount();
}
