package top.theillusivec4.champions.common.affix.builtin.goal_affixes;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import top.theillusivec4.champions.api.affix.AffixType;
import top.theillusivec4.champions.api.affix.EmptyAffixData;
import top.theillusivec4.champions.api.affix.handler.GoalHandler;
import top.theillusivec4.champions.api.affix.handler.HandlerRegistry;
import top.theillusivec4.champions.api.champion.Champion;
import top.theillusivec4.champions.common.affix.builtin.AffixDefaults;
import top.theillusivec4.champions.common.entity.EnkindlingBulletEntity;

import java.util.EnumSet;

/**
 * Fires homing fire bullets at the current target.
 *
 * <p>Bullet behaviour: shulker-bullet-style homing projectile that sets the target
 * on fire for 8 seconds on hit (see {@link EnkindlingBulletEntity}).
 * Only activates within 20 blocks — clears target beyond that range.</p>
 */
public final class EnkindlingAffix extends AffixType<EmptyAffixData> {

    @Override
    public void registerHandlers(HandlerRegistry<EmptyAffixData> registry) {
        registry.onGoal(new GoalHandler<>() {
            @Override
            public void setup(Champion champion, EmptyAffixData data, int strength, GoalSelector gs) {
                if (champion.entity() instanceof Mob mob) {
                    gs.addGoal(2, new FireGoal(mob, AffixDefaults.ENKINDLING_ATTACK_INTERVAL()));
                }
            }

            @Override
            public void teardown(Champion champion, EmptyAffixData data, int strength, GoalSelector gs) {
                gs.removeAllGoals(g -> g instanceof FireGoal);
            }
        });
    }

    // ── Goal ──────────────────────────────────────────────────────────────────

    static class FireGoal extends Goal {
        /** Maximum engagement distance — matches original's sqrDistance < 400 check. */
        private static final double MAX_RANGE_SQR = 400.0;

        private final Mob mob;
        private final int baseInterval;
        private int attackTime;

        FireGoal(Mob mob, int intervalSeconds) {
            this.mob = mob;
            this.baseInterval = intervalSeconds * 20;
            // LOOK only — never MOVE, which would evict pathfinding goal
            setFlags(EnumSet.of(Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = mob.getTarget();
            return target != null && target.isAlive()
                    && mob.level().getDifficulty() != Difficulty.PEACEFUL;
        }

        @Override
        public void start() {
            attackTime = baseInterval;
        }

        @Override
        public void tick() {
            if (mob.level().getDifficulty() == Difficulty.PEACEFUL) return;

            LivingEntity target = mob.getTarget();
            if (target == null) return;

            mob.getLookControl().setLookAt(target, 180f, 180f);

            // Clear target beyond 20 blocks — matches original behaviour
            if (mob.distanceToSqr(target) > MAX_RANGE_SQR) {
                mob.setTarget(null);
                return;
            }

            if (--attackTime <= 0) {
                // Random jitter: 0–9 extra 0.5-second windows
                attackTime = baseInterval + mob.getRandom().nextInt(10) * 10;

                mob.level().addFreshEntity(
                        new EnkindlingBulletEntity(mob.level(), mob, target,
                                mob.getMotionDirection().getAxis()));

                mob.playSound(SoundEvents.SHULKER_SHOOT, 2f,
                        (mob.getRandom().nextFloat() - mob.getRandom().nextFloat()) * 0.2f + 1f);
            }
        }
    }
}
