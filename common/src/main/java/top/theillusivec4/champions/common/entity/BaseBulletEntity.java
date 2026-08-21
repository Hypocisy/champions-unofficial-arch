package top.theillusivec4.champions.common.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Homing bullet base class — shulker-bullet-style pathfinding toward a target entity.
 *
 * <p>Ported from the old project's {@code BaseBulletEntity}. The NeoForge-specific
 * {@code EventHooks.onProjectileImpact} call has been removed so this class compiles
 * on both platforms. Platform-specific subclasses can override {@link #onHit} to
 * restore that hook if desired.</p>
 *
 * <p>Subclasses must implement:</p>
 * <ul>
 *   <li>{@link #bulletEffect(LivingEntity)} — what happens when the bullet hits a living target.</li>
 *   <li>{@link #getParticle()} — the particle emitted along the flight trail.</li>
 * </ul>
 */
public abstract class BaseBulletEntity extends Projectile {

    @Nullable private Entity finalTarget;
    @Nullable private Direction currentMoveDirection;
    private int flightSteps;
    private double targetDeltaX;
    private double targetDeltaY;
    private double targetDeltaZ;
    @Nullable
    private UUID targetId;

    protected BaseBulletEntity(EntityType<? extends Projectile> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    protected BaseBulletEntity(EntityType<? extends Projectile> type, Level level,
                               LivingEntity shooter, @Nonnull Entity target, Direction.Axis axis) {
        this(type, level);
        this.setOwner(shooter);
        BlockPos bp = shooter.blockPosition();
        this.moveTo(bp.getX() + 0.5, bp.getY() + 0.5, bp.getZ() + 0.5,
                this.getYRot(), this.getXRot());
        this.finalTarget = target;
        this.currentMoveDirection = Direction.UP;
        this.selectNextMoveDirection(axis);
    }

    // ── NBT ───────────────────────────────────────────────────────────────────

    @Override
    protected void addAdditionalSaveData(@Nonnull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (finalTarget != null) tag.putUUID("Target", finalTarget.getUUID());
        if (currentMoveDirection != null) tag.putInt("Dir", currentMoveDirection.get3DDataValue());
        tag.putInt("Steps", flightSteps);
        tag.putDouble("TXD", targetDeltaX);
        tag.putDouble("TYD", targetDeltaY);
        tag.putDouble("TZD", targetDeltaZ);
    }

    @Override
    protected void readAdditionalSaveData(@Nonnull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        flightSteps = tag.getInt("Steps");
        targetDeltaX = tag.getDouble("TXD");
        targetDeltaY = tag.getDouble("TYD");
        targetDeltaZ = tag.getDouble("TZD");
        if (tag.contains("Dir", 99)) currentMoveDirection = Direction.from3DDataValue(tag.getInt("Dir"));
        if (tag.hasUUID("Target")) targetId = tag.getUUID("Target");
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    private void selectNextMoveDirection(@Nullable Direction.Axis axis) {
        double targetY = 0.5;
        BlockPos targetBlock;

        if (finalTarget == null) {
            targetBlock = this.blockPosition().below();
        } else {
            targetY = finalTarget.getBbHeight() * 0.5;
            targetBlock = new BlockPos(finalTarget.getBlockX(),
                    (int) (finalTarget.getBlockY() + targetY),
                    finalTarget.getBlockZ());
        }

        double tx = targetBlock.getX() + 0.5;
        double ty = targetBlock.getY() + targetY;
        double tz = targetBlock.getZ() + 0.5;

        Direction direction = null;
        if (!closerThan(targetBlock, this.position(), 2.0)) {
            BlockPos here = this.blockPosition();
            List<Direction> options = new ArrayList<>();
            if (axis != Direction.Axis.X) {
                if (here.getX() < targetBlock.getX() && level().isEmptyBlock(here.east()))   options.add(Direction.EAST);
                else if (here.getX() > targetBlock.getX() && level().isEmptyBlock(here.west())) options.add(Direction.WEST);
            }
            if (axis != Direction.Axis.Y) {
                if (here.getY() < targetBlock.getY() && level().isEmptyBlock(here.above())) options.add(Direction.UP);
                else if (here.getY() > targetBlock.getY() && level().isEmptyBlock(here.below())) options.add(Direction.DOWN);
            }
            if (axis != Direction.Axis.Z) {
                if (here.getZ() < targetBlock.getZ() && level().isEmptyBlock(here.south())) options.add(Direction.SOUTH);
                else if (here.getZ() > targetBlock.getZ() && level().isEmptyBlock(here.north())) options.add(Direction.NORTH);
            }
            direction = Direction.getRandom(random);
            if (!options.isEmpty()) {
                direction = options.get(random.nextInt(options.size()));
            } else {
                for (int i = 5; !level().isEmptyBlock(here.relative(direction)) && i > 0; i--) {
                    direction = Direction.getRandom(random);
                }
            }
            tx = this.getX() + direction.getStepX();
            ty = this.getY() + direction.getStepY();
            tz = this.getZ() + direction.getStepZ();
        }

        currentMoveDirection = direction;
        double dx = tx - this.getX();
        double dy = ty - this.getY();
        double dz = tz - this.getZ();
        double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len == 0) {
            targetDeltaX = targetDeltaY = targetDeltaZ = 0;
        } else {
            targetDeltaX = dx / len * 0.15;
            targetDeltaY = dy / len * 0.15;
            targetDeltaZ = dz / len * 0.15;
        }
        hasImpulse = true;
        flightSteps = 10 + random.nextInt(5) * 10;
    }

    private boolean closerThan(BlockPos pos, Position position, double dist) {
        double dx = pos.getX() + 0.5 - position.x();
        double dy = pos.getY() + 0.5 - position.y();
        double dz = pos.getZ() + 0.5 - position.z();
        return dx * dx + dy * dy + dz * dz < Mth.square(dist);
    }

    // ── Tick ──────────────────────────────────────────────────────────────────

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide) {
            // Resolve UUID → entity after load
            if (finalTarget == null && targetId != null) {
                finalTarget = ((ServerLevel) level()).getEntity(targetId);
                if (finalTarget == null) targetId = null;
            }

            if (finalTarget == null || !finalTarget.isAlive()
                    || (finalTarget instanceof Player p && p.isSpectator())) {
                if (!isNoGravity()) setDeltaMovement(getDeltaMovement().add(0, -0.04, 0));
            } else {
                targetDeltaX = Mth.clamp(targetDeltaX * 1.025, -1.0, 1.0);
                targetDeltaY = Mth.clamp(targetDeltaY * 1.025, -1.0, 1.0);
                targetDeltaZ = Mth.clamp(targetDeltaZ * 1.025, -1.0, 1.0);
                Vec3 v = getDeltaMovement();
                setDeltaMovement(v.add(
                        (targetDeltaX - v.x) * 0.2,
                        (targetDeltaY - v.y) * 0.2,
                        (targetDeltaZ - v.z) * 0.2));
            }

            HitResult hit = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
            if (hit.getType() != HitResult.Type.MISS) {
                onHit(hit);
            }
        }

        checkInsideBlocks();
        Vec3 v = getDeltaMovement();
        setPos(getX() + v.x, getY() + v.y, getZ() + v.z);
        ProjectileUtil.rotateTowardsMovement(this, 0.5f);

        if (level().isClientSide) {
            level().addParticle(getParticle(),
                    getX() - v.x, getY() - v.y + 0.15, getZ() - v.z, 0, 0, 0);
        } else if (finalTarget != null && !finalTarget.isRemoved()) {
            if (flightSteps > 0 && --flightSteps == 0) {
                selectNextMoveDirection(currentMoveDirection == null
                        ? null : currentMoveDirection.getAxis());
            }
            if (currentMoveDirection != null) {
                BlockPos here = blockPosition();
                Direction.Axis dAxis = currentMoveDirection.getAxis();
                if (level().loadedAndEntityCanStandOn(here.relative(currentMoveDirection), this)) {
                    selectNextMoveDirection(dAxis);
                } else {
                    BlockPos tb = finalTarget.blockPosition();
                    if ((dAxis == Direction.Axis.X && here.getX() == tb.getX())
                            || (dAxis == Direction.Axis.Z && here.getZ() == tb.getZ())
                            || (dAxis == Direction.Axis.Y && here.getY() == tb.getY())) {
                        selectNextMoveDirection(dAxis);
                    }
                }
            }
        }
    }

    // ── Hit handling ──────────────────────────────────────────────────────────

    @Override
    protected void onHitEntity(@Nonnull EntityHitResult result) {
        super.onHitEntity(result);
        Entity entity = result.getEntity();
        if (entity != getOwner() && entity instanceof LivingEntity target) {
            bulletEffect(target);
        }
    }

    @Override
    protected void onHitBlock(@Nonnull BlockHitResult result) {
        super.onHitBlock(result);
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.EXPLOSION,
                    getX(), getY(), getZ(), 2, 0.2, 0.2, 0.2, 0.0);
            playSound(SoundEvents.SHULKER_BULLET_HIT, 1f, 1f);
        }
    }

    @Override
    protected void onHit(@Nonnull HitResult result) {
        super.onHit(result);
        discard();
    }

    @Override
    protected boolean canHitEntity(@Nonnull Entity target) {
        return super.canHitEntity(target) && !target.noPhysics;
    }

    // ── Misc ──────────────────────────────────────────────────────────────────

    @Override
    public boolean isOnFire() { return false; }

    @Override
    public boolean isPickable() { return true; }

    @Override
    public boolean hurt(@Nonnull DamageSource source, float amount) {
        if (level() instanceof ServerLevel srv) {
            playSound(SoundEvents.SHULKER_BULLET_HURT, 1f, 1f);
            srv.sendParticles(ParticleTypes.CRIT, getX(), getY(), getZ(), 15, 0.2, 0.2, 0.2, 0.0);
            discard();
        }
        return true;
    }

    @Override
    public @NotNull SoundSource getSoundSource() { return SoundSource.HOSTILE; }

    // ── Abstract interface ────────────────────────────────────────────────────

    /** Apply the bullet's on-hit effect to the target. */
    protected abstract void bulletEffect(LivingEntity target);

    /** Trail particle shown on the client. */
    protected abstract ParticleOptions getParticle();
}
