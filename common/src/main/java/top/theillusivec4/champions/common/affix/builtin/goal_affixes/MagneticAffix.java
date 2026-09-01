package top.theillusivec4.champions.common.affix.builtin.goal_affixes;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import top.theillusivec4.champions.api.affix.AffixType;
import top.theillusivec4.champions.api.affix.EmptyAffixData;
import top.theillusivec4.champions.api.affix.handler.GoalHandler;
import top.theillusivec4.champions.api.affix.handler.HandlerRegistry;
import top.theillusivec4.champions.api.champion.Champion;
import top.theillusivec4.champions.common.config.ChampionsConfig;

/**
 * Periodically pulls the current target toward the champion.
 *
 * <p>Targets any {@link LivingEntity} (not just players), matching the original
 * behaviour. Pull strength scales with {@code strength}. Pull direction is a full
 * velocity override (not an additive nudge) so the effect is decisive.</p>
 *
 * <p>For {@link Player} targets, {@code hurtMarked = true} is set so the server
 * forces a position sync to the client, preventing the pull from being invisible
 * or rubber-banding due to client-side prediction.</p>
 */
public final class MagneticAffix extends AffixType<EmptyAffixData> {

    @Override
    public void registerHandlers(HandlerRegistry<EmptyAffixData> registry) {
        registry.onGoal(new GoalHandler<>() {
            @Override
            public void setup(Champion champion, EmptyAffixData data, int strength, GoalSelector gs) {
                if (champion.entity() instanceof Mob mob) {
                    gs.addGoal(1, new PullGoal(mob, strength));
                }
            }

            @Override
            public void teardown(Champion champion, EmptyAffixData data, int strength, GoalSelector gs) {
                gs.removeAllGoals(g -> g instanceof PullGoal);
            }
        });
    }

    static class PullGoal extends Goal {
        private final Mob mob;
        private final int strength;
        private LivingEntity target;

        PullGoal(Mob mob, int strength) {
            this.mob = mob;
            this.strength = strength;
        }

        @Override
        public boolean canUse() {
            LivingEntity t = mob.getTarget();
            // Any LivingEntity target, not just players — matches original
            return t != null
                    && t.isAlive()
                    && mob.tickCount % 40 == 0
                    && mob.getRandom().nextDouble() < 0.4;
        }

        @Override
        public boolean canContinueToUse() {
            // Keep running for a few ticks so the velocity impulse actually takes effect,
            // then stop — matching original's canContinueToUse logic.
            return mob.tickCount % 40 != 0 || mob.getRandom().nextDouble() > 0.7;
        }

        @Override
        public void start() {
            target = mob.getTarget();
        }

        @Override
        public void stop() {
            target = null;
        }

        @Override
        public void tick() {
            if (target == null || !target.isAlive()) return;
            if (mob.distanceTo(target) > ChampionsConfig.magneticPullRange) return;

            double pull = ChampionsConfig.magneticStrength * (0.5 + strength * 0.2);
            Vec3 dir = mob.position().subtract(target.position()).normalize();
            // Full velocity override so the pull is decisive, not just a tiny nudge
            target.setDeltaMovement(dir.scale(pull));

            // Force server→client position sync for players so the pull is visible
            // without rubber-banding from client-side prediction.
            if (target instanceof Player player) {
                player.hurtMarked = true;
            }
        }
    }
}
