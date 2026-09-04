package top.theillusivec4.champions.neoforge.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import top.theillusivec4.champions.common.loot.ChampionLootConditions;
import top.theillusivec4.champions.common.loot.ChampionPropertyCondition;

/**
 * Registers custom loot condition types via Forge DeferredRegister.
 * Must be registered to the mod bus during constructor time (before the registry freezes).
 */
public final class ModLootConditions {

    private static final DeferredRegister<LootItemConditionType> TYPES =
            DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, "champions");

    public static final RegistryObject<LootItemConditionType> CHAMPION_PROPERTIES =
            TYPES.register("champion_properties",
                    () -> new LootItemConditionType(new ChampionPropertyCondition.ChampionConditionSerializer()));

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
    }

    /** Wire the holder into the common static field after registry freeze. */
    public static void wireCommon() {
        ChampionLootConditions.CHAMPION_PROPERTIES = CHAMPION_PROPERTIES.get();
    }
}
