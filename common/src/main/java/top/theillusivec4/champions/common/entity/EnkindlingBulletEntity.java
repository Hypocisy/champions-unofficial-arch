package top.theillusivec4.champions.common.entity;

import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

/**
 * Homing fire bullet fired by {@link top.theillusivec4.champions.common.affix.builtin.goal_affixes.EnkindlingAffix}.
 *
 * <p>On hit: sets target on fire for 8 seconds.</p>
 * <p>Trail particle: {@link ParticleTypes#FLAME}.</p>
 */
public final class EnkindlingBulletEntity extends BaseBulletEntity {

    public EnkindlingBulletEntity(Level level, LivingEntity shooter,
                                  @Nonnull Entity target, Direction.Axis axis) {
        super(ChampionEntityTypes.ENKINDLING_BULLET.get(), level, shooter, target, axis);
    }

    /** Deserialization constructor required by EntityType. */
    public EnkindlingBulletEntity(EntityType<EnkindlingBulletEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void bulletEffect(LivingEntity target) {
        // 8 seconds of fire (matches original)
        target.setRemainingFireTicks(8 * 20);
    }

    @Override
    protected ParticleOptions getParticle() {
        return ParticleTypes.FLAME;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        // no extra synced data
    }
}
