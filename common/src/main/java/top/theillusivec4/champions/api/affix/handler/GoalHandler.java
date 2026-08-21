package top.theillusivec4.champions.api.affix.handler;

import net.minecraft.world.entity.ai.goal.GoalSelector;
import top.theillusivec4.champions.api.affix.IAffixData;
import top.theillusivec4.champions.api.champion.Champion;

/**
 * A paired setup/teardown handler for affixes that modify mob AI goals.
 *
 * <p>Registered via {@link HandlerRegistry#onGoal(GoalHandler)}. The goal setup is called when
 * an {@link top.theillusivec4.champions.api.affix.AffixInstance} is added to a champion, and
 * teardown is called when it is removed. This ensures goal lifecycle is tied to instance
 * lifecycle — no goals leak on death or affix removal.</p>
 *
 * @param <D> the affix's per-champion data type
 */
public interface GoalHandler<D extends IAffixData> {

    /**
     * Called when this affix instance is added to a champion.
     * Add any custom goals to {@code goalSelector} here.
     */
    void setup(Champion champion, D data, int strength, GoalSelector goalSelector);

    /**
     * Called when this affix instance is removed from a champion.
     * Remove any goals that were added in {@link #setup}.
     */
    void teardown(Champion champion, D data, int strength, GoalSelector goalSelector);
}
