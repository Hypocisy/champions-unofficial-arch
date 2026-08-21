package top.theillusivec4.champions.common.affix.builtin.goal_affixes;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import top.theillusivec4.champions.api.affix.AffixType;
import top.theillusivec4.champions.api.affix.EmptyAffixData;
import top.theillusivec4.champions.api.affix.handler.GoalHandler;
import top.theillusivec4.champions.api.affix.handler.HandlerRegistry;
import top.theillusivec4.champions.api.affix.handler.event.HurtEvent;
import top.theillusivec4.champions.api.champion.Champion;
import top.theillusivec4.champions.common.affix.builtin.AffixDefaults;

/**
 * Periodically drops lingering area-of-effect HARM clouds at the target's position.
 *
 * <p>Key design points restored from the original:</p>
 * <ul>
 *   <li>Cloud spawns at the <em>target's</em> position (not the mob's).</li>
 *   <li>{@link AreaEffectCloud#setOwner(net.minecraft.world.entity.Entity)} is set to the mob,
 *       so damage from the cloud is attributed to the champion and does not re-trigger
 *       other damage-reaction affixes (e.g. Reflective) in a loop.</li>
 *   <li>Effect is instant {@code HARM} (1 tick, level 1) — not Wither DoT.</li>
 *   <li>Cloud radius shrinks over its lifetime via {@code setRadiusPerTick}.</li>
 *   <li>Timer has a small random jitter so repeated drops feel less mechanical.</li>
 * </ul>
 *
 * <p>The {@link HurtEvent} guard returns early when the damage source is an
 * {@link AreaEffectCloud} owned by this champion, preventing the cloud's own
 * HARM from triggering other champion reaction handlers.</p>
 */
public final class DesecratingAffix extends AffixType<EmptyAffixData> {

    @Override
    public void registerHandlers(HandlerRegistry<EmptyAffixData> registry) {

        // AI goal: drop a HARM cloud at the target's feet on a timer
        registry.onGoal(new GoalHandler<>() {
            @Override
            public void setup(Champion champion, EmptyAffixData data, int strength, GoalSelector gs) {
                if (champion.entity() instanceof Mob mob) {
                    gs.addGoal(2, new DesecrateGoal(mob, strength));
                }
            }

            @Override
            public void teardown(Champion champion, EmptyAffixData data, int strength, GoalSelector gs) {
                gs.removeAllGoals(g -> g instanceof DesecrateGoal);
            }
        });

        // Cancel incoming damage when it originates from our own desecrating cloud.
        // AreaEffectCloud damage sources set getEntity() (indirect) to the cloud's owner
        // and getDirectEntity() to the cloud itself — so we check the indirect source.
        // This prevents the champion from self-triggering reaction affixes (Reflective,
        // Knocking, Shielding, etc.) when they walk into their own cloud.
        registry.on(HurtEvent.class, (champion, data, strength, evt) -> {
            if (evt.source().getEntity() == champion.entity()) {
                evt.cancel();
            }
        });
    }

    // ── Goal ──────────────────────────────────────────────────────────────────

    static class DesecrateGoal extends Goal {
        private final Mob mob;
        private final int strength;
        private int timer;

        DesecrateGoal(Mob mob, int strength) {
            this.mob = mob;
            this.strength = strength;
            this.timer = AffixDefaults.DESECRATING_INTERVAL() * 20;
        }

        @Override
        public boolean canUse() {
            LivingEntity target = mob.getTarget();
            return target != null
                    && target.isAlive()
                    && mob.level().getDifficulty() != Difficulty.PEACEFUL;
        }

        @Override
        public void tick() {
            if (--timer > 0) return;
            // Add random jitter (0–4 extra ticks × 10) so drops feel less robotic
            timer = AffixDefaults.DESECRATING_INTERVAL() * 20
                    + mob.getRandom().nextInt(5) * 10;

            LivingEntity target = mob.getTarget();
            if (target == null || !(mob.level() instanceof ServerLevel)) return;

            // Spawn cloud at the TARGET's position (not the mob's)
            AreaEffectCloud cloud = new AreaEffectCloud(
                    mob.level(),
                    target.getX(), target.getY(), target.getZ());

            // Set owner so cloud damage is attributed to this mob and won't
            // re-trigger reaction affixes in a loop.
            cloud.setOwner(mob);

            float radius = (float) AffixDefaults.DESECRATING_RADIUS();
            int duration = 200 + strength * 40;

            cloud.setRadius(radius);
            cloud.setDuration(duration);
            cloud.setRadiusOnUse(-0.5f);
            // Wait time before the cloud activates (configurable in AffixDefaults)
            cloud.setWaitTime(10);
            // Shrink the cloud linearly so it disappears exactly when duration expires
            cloud.setRadiusPerTick(-radius / (float) duration);
            // Instant HARM (level 1) — matches original; Wither would be a different affix
            cloud.addEffect(new MobEffectInstance(MobEffects.HARM, 1, 1));

            mob.level().addFreshEntity(cloud);
        }
    }
}
