package top.theillusivec4.champions.fabric.registry;
import top.theillusivec4.champions.common.utils.Utils;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import top.theillusivec4.champions.common.entity.ArcticBulletEntity;
import top.theillusivec4.champions.common.entity.ChampionEntityTypes;
import top.theillusivec4.champions.common.entity.EnkindlingBulletEntity;

public final class ModEntityTypes {

    public static final EntityType<ArcticBulletEntity> ARCTIC_BULLET =
            EntityType.Builder.<ArcticBulletEntity>of(
                    ArcticBulletEntity::new,
                            MobCategory.MISC)
                    .sized(0.3125f, 0.3125f)
                    .clientTrackingRange(8)
                    .build("arctic_bullet");

    public static final EntityType<EnkindlingBulletEntity> ENKINDLING_BULLET =
            EntityType.Builder.<EnkindlingBulletEntity>of(
                            EnkindlingBulletEntity::new,MobCategory.MISC)
                    .sized(0.3125f, 0.3125f)
                    .clientTrackingRange(8)
                    .build("enkindling_bullet");

    public static void register() {
        Registry.register(BuiltInRegistries.ENTITY_TYPE,
                Utils.key("arctic_bullet"),
                ARCTIC_BULLET);
        Registry.register(BuiltInRegistries.ENTITY_TYPE,
                Utils.key("enkindling_bullet"),
                ENKINDLING_BULLET);
        ChampionEntityTypes.register(() -> ARCTIC_BULLET, () -> ENKINDLING_BULLET);
    }
}
