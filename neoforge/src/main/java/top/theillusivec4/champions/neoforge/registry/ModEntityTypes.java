package top.theillusivec4.champions.neoforge.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import top.theillusivec4.champions.common.entity.ArcticBulletEntity;
import top.theillusivec4.champions.common.entity.ChampionEntityTypes;
import top.theillusivec4.champions.common.entity.EnkindlingBulletEntity;

public final class ModEntityTypes {

    private static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, "champions");

    public static final DeferredHolder<EntityType<?>, EntityType<ArcticBulletEntity>> ARCTIC_BULLET =
            ENTITIES.register("arctic_bullet", ChampionEntityTypes::buildArcticType);

    public static final DeferredHolder<EntityType<?>, EntityType<EnkindlingBulletEntity>> ENKINDLING_BULLET =
            ENTITIES.register("enkindling_bullet", ChampionEntityTypes::buildEnkindlingType);

    public static void register(IEventBus bus) {
        ENTITIES.register(bus);
    }

    /** Wire the common ChampionEntityTypes holder after registry freeze. */
    public static void wireCommon() {
        ChampionEntityTypes.register(ARCTIC_BULLET, ENKINDLING_BULLET);
    }
}
