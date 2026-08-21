package top.theillusivec4.champions.common.entity;

import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

/**
 * Homing ice bullet fired by {@link top.theillusivec4.champions.common.affix.builtin.goal_affixes.ArcticAffix}.
 *
 * <p>On hit: applies Slowdown III + Mining Fatigue III for 5 seconds.</p>
 * <p>Trail particle: {@link ParticleTypes#ITEM_SNOWBALL}.</p>
 */
public final class ArcticBulletEntity extends BaseBulletEntity {

    public ArcticBulletEntity(Level level, LivingEntity shooter,
                              @Nonnull Entity target, Direction.Axis axis) {
        super(ChampionEntityTypes.ARCTIC_BULLET.get(), level, shooter, target, axis);
    }

    /** Deserialization constructor required by EntityType. */
    public ArcticBulletEntity(EntityType<ArcticBulletEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void bulletEffect(LivingEntity target) {
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2));
        target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN,      100, 2));
    }

    @Override
    protected ParticleOptions getParticle() {
        return ParticleTypes.ITEM_SNOWBALL;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        // no extra synced data
    }
}
