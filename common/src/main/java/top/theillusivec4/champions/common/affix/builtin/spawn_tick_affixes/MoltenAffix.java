package top.theillusivec4.champions.common.affix.builtin.spawn_tick_affixes;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.FleeSunGoal;
import net.minecraft.world.entity.ai.goal.RestrictSunGoal;
import net.minecraft.world.level.pathfinder.PathType;
import top.theillusivec4.champions.api.affix.AffixType;
import top.theillusivec4.champions.api.affix.EmptyAffixData;
import top.theillusivec4.champions.api.affix.handler.HandlerRegistry;
import top.theillusivec4.champions.api.affix.handler.event.AttackEvent;
import top.theillusivec4.champions.api.affix.handler.event.HurtEvent;
import top.theillusivec4.champions.api.affix.handler.event.SpawnEvent;
import top.theillusivec4.champions.api.affix.handler.event.TickEvent;
import top.theillusivec4.champions.common.affix.builtin.AffixDefaults;

/**
 * Fire immunity, lava pathfinding, fire damage aura, and dual fire-on-contact.
 *
 * <ul>
 *   <li><b>Spawn</b> — grants infinite fire resistance, adjusts pathfinding costs,
 *       removes sun-avoidance AI goals.</li>
 *   <li><b>Attack (active)</b> — sets the target on fire when the champion hits them,
 *       plus deals a small burst of fire damage immediately.</li>
 *   <li><b>Hurt (reactive)</b> — sets the attacker on fire when they hit the champion
 *       (unless the incoming damage is already fire-type).</li>
 *   <li><b>Tick aura</b> — deals fire damage to all nearby hostile entities every 20 ticks.</li>
 * </ul>
 */
public final class MoltenAffix extends AffixType<EmptyAffixData> {

    @Override
    public void registerHandlers(HandlerRegistry<EmptyAffixData> registry) {

        // ── Spawn: fire resistance + pathfinding + remove sun-flee goals ──────
        registry.on(SpawnEvent.class, (champion, data, strength, evt) -> {
            LivingEntity entity = champion.entity();
            entity.addEffect(new MobEffectInstance(
                    MobEffects.FIRE_RESISTANCE, MobEffectInstance.INFINITE_DURATION, 0, true, false));

            if (entity instanceof Mob mob) {
                mob.setPathfindingMalus(PathType.WATER, -1f);
                mob.setPathfindingMalus(PathType.LAVA, 8f);
                mob.setPathfindingMalus(PathType.DANGER_FIRE, 0f);
                mob.setPathfindingMalus(PathType.DAMAGE_FIRE, 0f);
                mob.goalSelector.getAvailableGoals().removeIf(g ->
                        g.getGoal() instanceof RestrictSunGoal || g.getGoal() instanceof FleeSunGoal);
            }
        });

        // ── Active: champion attacks → set target on fire ─────────────────────
        registry.on(AttackEvent.class, (champion, data, strength, evt) -> {
            evt.target().setRemainingFireTicks(AffixDefaults.MOLTEN_FIRE_TICKS() * strength);
            // Immediate burst fire damage so hits feel impactful
            float burst = (float) (AffixDefaults.MOLTEN_AURA_DAMAGE() * strength * 0.5);
            if (burst > 0) {
                evt.target().hurt(champion.entity().damageSources().onFire(), burst);
            }
        });

        // ── Reactive: attacker hits champion → attacker catches fire ──────────
        registry.on(HurtEvent.class, (champion, data, strength, evt) -> {
            // Don't apply fire from fire-type damage (already burning source)
            if (evt.source().is(DamageTypeTags.IS_FIRE) || evt.source().is(DamageTypes.LAVA)) return;
            if (evt.source().getEntity() instanceof LivingEntity attacker) {
                attacker.setRemainingFireTicks(AffixDefaults.MOLTEN_FIRE_TICKS() * strength);
            }
        });

        // ── Aura tick: deal fire damage to nearby hostile mobs every 20 ticks ─
        registry.on(TickEvent.class, (champion, data, strength, evt) -> {
            if (!evt.every(20)) return;
            LivingEntity entity = champion.entity();
            if (entity.level().isClientSide()) return;
            double range = AffixDefaults.MOLTEN_AURA_RANGE() * (0.5 + strength * 0.2);
            float damage = (float) (AffixDefaults.MOLTEN_AURA_DAMAGE() * strength);

            entity.level().getEntitiesOfClass(LivingEntity.class,
                    entity.getBoundingBox().inflate(range),
                    e -> e != entity && !e.isAlliedTo(entity)
            ).forEach(target -> target.hurt(entity.damageSources().onFire(), damage));
        });
    }
}
