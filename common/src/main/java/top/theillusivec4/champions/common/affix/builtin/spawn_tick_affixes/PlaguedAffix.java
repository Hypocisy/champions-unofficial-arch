package top.theillusivec4.champions.common.affix.builtin.spawn_tick_affixes;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import top.theillusivec4.champions.api.affix.AffixType;
import top.theillusivec4.champions.api.affix.EmptyAffixData;
import top.theillusivec4.champions.api.affix.handler.HandlerRegistry;
import top.theillusivec4.champions.api.affix.handler.event.TickEvent;
import top.theillusivec4.champions.common.config.ChampionsConfig;

import java.util.List;

/**
 * Area-of-effect debuff aura. Applies a configurable mob effect to nearby entities.
 * Client side: emits coloured cloud particles.
 * Strength scales range.
 */
public final class PlaguedAffix extends AffixType<EmptyAffixData> {

    @Override
    public void registerHandlers(HandlerRegistry<EmptyAffixData> registry) {

        // Server: apply effect to nearby entities every 10 ticks
        registry.on(TickEvent.class, (champion, data, strength, evt) -> {
            if (!evt.every(10)) return;
            if (champion.entity().level().isClientSide()) return;

            LivingEntity entity = champion.entity();
            double range = ChampionsConfig.plaguedRange * (0.5 + strength * 0.2);

            List<Entity> nearby = entity.level().getEntities(entity,
                    entity.getBoundingBox().inflate(range),
                    e -> e instanceof LivingEntity le && le != entity && !le.isAlliedTo(entity)
            );
            nearby.forEach(e ->
                    ((LivingEntity) e).addEffect(new MobEffectInstance(MobEffects.POISON, 80, strength - 1))
            );
        });

        // Client: emit cloud particles
        registry.on(TickEvent.class, (champion, data, strength, evt) -> {
            if (!champion.entity().level().isClientSide()) return;
            LivingEntity entity = champion.entity();
            float radius = (float) (ChampionsConfig.plaguedRange * 0.6);
            float circle = (float) Math.PI * radius * radius;
            for (int i = 0; i < (int) circle; i++) {
                float angle = entity.getRandom().nextFloat() * 2f * (float) Math.PI;
                float r = Mth.sqrt(entity.getRandom().nextFloat()) * radius;
                entity.level().addParticle(
                        ParticleTypes.ENTITY_EFFECT,
                        entity.getX() + Mth.cos(angle) * r,
                        entity.getY(),
                        entity.getZ() + Mth.sin(angle) * r,
                        0.3, 1.0, 0.3
                );
            }
        });
    }
}
