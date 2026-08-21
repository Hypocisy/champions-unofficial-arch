package top.theillusivec4.champions.common.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import java.util.function.Supplier;

/**
 * Holds the two custom entity types used by Champions bullets.
 *
 * <p>Platform modules call {@link #register(Supplier, Supplier)} at startup.
 * Affix code references these suppliers instead of platform registries directly.</p>
 */
public final class ChampionEntityTypes {

    private static Supplier<EntityType<ArcticBulletEntity>>     arcticBulletSupplier     = null;
    private static Supplier<EntityType<EnkindlingBulletEntity>> enkindlingBulletSupplier = null;

    private ChampionEntityTypes() {}

    public static void register(
            Supplier<EntityType<ArcticBulletEntity>> arctic,
            Supplier<EntityType<EnkindlingBulletEntity>> enkindling) {
        arcticBulletSupplier     = arctic;
        enkindlingBulletSupplier = enkindling;
    }

    public static Supplier<EntityType<ArcticBulletEntity>> ARCTIC_BULLET =
            () -> arcticBulletSupplier.get();

    public static Supplier<EntityType<EnkindlingBulletEntity>> ENKINDLING_BULLET =
            () -> enkindlingBulletSupplier.get();

    // ── Default EntityType specs (shared by both platforms) ───────────────────

    public static EntityType<ArcticBulletEntity> buildArcticType() {
        return EntityType.Builder.<ArcticBulletEntity>of(
                        ArcticBulletEntity::new, MobCategory.MISC)
                .sized(0.3125f, 0.3125f)
                .clientTrackingRange(8)
                .build("arctic_bullet");
    }

    public static EntityType<EnkindlingBulletEntity> buildEnkindlingType() {
        return EntityType.Builder.<EnkindlingBulletEntity>of(
                        EnkindlingBulletEntity::new, MobCategory.MISC)
                .sized(0.3125f, 0.3125f)
                .clientTrackingRange(8)
                .build("enkindling_bullet");
    }
}
