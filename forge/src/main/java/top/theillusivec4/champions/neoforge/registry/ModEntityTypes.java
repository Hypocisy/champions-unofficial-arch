package top.theillusivec4.champions.neoforge.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import top.theillusivec4.champions.common.entity.ArcticBulletEntity;
import top.theillusivec4.champions.common.entity.ChampionEntityTypes;
import top.theillusivec4.champions.common.entity.EnkindlingBulletEntity;

public final class ModEntityTypes {

    private static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, "champions");

    public static final RegistryObject<EntityType<ArcticBulletEntity>> ARCTIC_BULLET =
            ENTITIES.register("arctic_bullet", ChampionEntityTypes::buildArcticType);

    public static final RegistryObject<EntityType<EnkindlingBulletEntity>> ENKINDLING_BULLET =
            ENTITIES.register("enkindling_bullet", ChampionEntityTypes::buildEnkindlingType);

    public static void register(IEventBus bus) {
        ENTITIES.register(bus);
    }

    /** Wire the common ChampionEntityTypes holder after registry freeze. */
    public static void wireCommon() {
        ChampionEntityTypes.register(ARCTIC_BULLET, ENKINDLING_BULLET);
    }
}
