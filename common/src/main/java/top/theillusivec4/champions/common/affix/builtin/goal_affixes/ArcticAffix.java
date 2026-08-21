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
import top.theillusivec4.champions.common.entity.ArcticBulletEntity;

import java.util.EnumSet;

/**
 * Fires homing ice bullets at the current target.
 *
 * <p>Bullet behaviour: shulker-bullet-style homing projectile that applies
 * Slowdown III + Mining Fatigue III on hit (see {@link ArcticBulletEntity}).
 * Only activates within 20 blocks — beyond that the goal idles.</p>
 *
 * <p>Fire interval has a small random jitter so attacks feel less mechanical.</p>
 */
public final class ArcticAffix extends AffixType<EmptyAffixData> {

    @Override
    public void registerHandlers(HandlerRegistry<EmptyAffixData> registry) {
        registry.onGoal(new GoalHandler<>() {
            @Override
            public void setup(Champion champion, EmptyAffixData data, int strength, GoalSelector gs) {
                if (champion.entity() instanceof Mob mob) {
                    gs.addGoal(2, new ShootGoal(mob, AffixDefaults.ARCTIC_ATTACK_INTERVAL()));
                }
            }

            @Override
            public void teardown(Champion champion, EmptyAffixData data, int strength, GoalSelector gs) {
                gs.removeAllGoals(g -> g instanceof ShootGoal);
            }
        });
    }

    // ── Goal ──────────────────────────────────────────────────────────────────

    static class ShootGoal extends Goal {
        /** Maximum distance (blocks) — matches original's sqrDistance < 400 check. */
        private static final double MAX_RANGE_SQR = 400.0;

        private final Mob mob;
        private final int baseInterval;
        private int attackTime;

        ShootGoal(Mob mob, int intervalSeconds) {
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

            // Only fire within 20 blocks — clear target beyond range (original behaviour)
            if (mob.distanceToSqr(target) > MAX_RANGE_SQR) {
                mob.setTarget(null);
                return;
            }

            if (--attackTime <= 0) {
                // Random jitter: 0–9 extra 0.5-second windows (matches original)
                attackTime = baseInterval + mob.getRandom().nextInt(10) * 10;

                mob.level().addFreshEntity(
                        new ArcticBulletEntity(mob.level(), mob, target,
                                mob.getDirection().getAxis()));

                mob.playSound(SoundEvents.SHULKER_SHOOT, 2f,
                        (mob.getRandom().nextFloat() - mob.getRandom().nextFloat()) * 0.2f + 1f);
            }
        }
    }
}
