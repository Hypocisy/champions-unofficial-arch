package top.theillusivec4.champions.common.affix.builtin.goal_affixes;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import top.theillusivec4.champions.api.affix.AffixType;
import top.theillusivec4.champions.api.affix.EmptyAffixData;
import top.theillusivec4.champions.api.affix.handler.GoalHandler;
import top.theillusivec4.champions.api.affix.handler.HandlerRegistry;
import top.theillusivec4.champions.api.affix.handler.event.DeathEvent;
import top.theillusivec4.champions.api.champion.Champion;
import top.theillusivec4.champions.common.config.ChampionsConfig;
import top.theillusivec4.champions.common.entity.ChampionEntityTypes;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Periodically spawns parasite entities while alive; bursts more on death.
 *
 * <p>Spawn count scales with strength. On death the burst size is
 * {@code INFESTED_SPAWN_COUNT * strength * 2} to reward killing the champion.</p>
 *
 * <p>The parasite entity type is read from {@link ChampionsConfig#infestedParasite}
 * (config key {@code affixes.infested.parasite}, default {@code minecraft:silverfish}).</p>
 *
 * <p>Spawned parasites target the entity that last hurt the champion.</p>
 */
public final class InfestedAffix extends AffixType<EmptyAffixData> {

    /** Base ticks between periodic spawns (10 seconds). */
    private static final int SPAWN_INTERVAL = 200;

    @Override
    public void registerHandlers(HandlerRegistry<EmptyAffixData> registry) {

        // AI goal: periodic parasite spawn while a target exists
        registry.onGoal(new GoalHandler<>() {
            @Override
            public void setup(Champion champion, EmptyAffixData data, int strength, GoalSelector gs) {
                if (champion.entity() instanceof Mob mob) {
                    gs.addGoal(3, new SpawnGoal(mob, strength));
                }
            }

            @Override
            public void teardown(Champion champion, EmptyAffixData data, int strength, GoalSelector gs) {
                gs.removeAllGoals(g -> g instanceof SpawnGoal);
            }
        });

        // Death burst
        registry.on(DeathEvent.class, (champion, data, strength, evt) -> {
            LivingEntity entity = champion.entity();
            if (!(entity.level() instanceof ServerLevel level)) return;
            int count = ChampionsConfig.infestedAmount * strength * 2;
	        LivingEntity target = entity.getLastHurtByMob();
            spawnParasites(entity, count, target, level);

        });
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    static void spawnParasites(LivingEntity host, int count,
                               @Nullable LivingEntity target, ServerLevel level) {
        if (target == null) return;
        // Resolve configurable parasite entity type; fall back to silverfish on bad id
        boolean isEnder = target.is(ChampionEntityTypes.Tags.IS_ENDER);
        String id = isEnder ? ChampionsConfig.infestedEnderParasite : ChampionsConfig.infestedParasite;

        EntityType<?> parasiteType = Optional.ofNullable(Identifier.tryParse(id))
                .flatMap(BuiltInRegistries.ENTITY_TYPE::getOptional)
                .orElse(EntityTypes.SILVERFISH);

        for (int i = 0; i < count; i++) {
            var entity = parasiteType
                    .create(level, null, host.blockPosition(), EntitySpawnReason.MOB_SUMMONED,
                            false, false);

            if (!(entity instanceof Mob parasite)) continue;
            double dx = (level.getRandom().nextDouble() - 0.5) * 2.0;
            double dz = (level.getRandom().nextDouble() - 0.5) * 2.0;
            parasite.snapTo(host.getX() + dx, host.getY(), host.getZ() + dz,
                    host.getYRot(), 0);
            level.addFreshEntityWithPassengers(parasite);
	        parasite.setTarget(target);
        }
    }

    // ── Goal ──────────────────────────────────────────────────────────────────

    static class SpawnGoal extends Goal {
        private final Mob mob;
        private final int strength;
        private int timer = SPAWN_INTERVAL;

        SpawnGoal(Mob mob, int strength) {
            this.mob = mob;
            this.strength = strength;
        }

        @Override
        public boolean canUse() {
            return mob.getTarget() != null;
        }

        @Override
        public void tick() {
            if (--timer > 0) return;
            timer = SPAWN_INTERVAL;
            if (mob.level() instanceof ServerLevel level) {
                int count = ChampionsConfig.infestedAmount + strength - 1;
                spawnParasites(mob, count, mob.getTarget(), level);
            }
        }
    }
}
