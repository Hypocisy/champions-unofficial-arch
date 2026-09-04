package top.theillusivec4.champions.neoforge.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import top.theillusivec4.champions.common.item.ChampionEggItem;
import top.theillusivec4.champions.common.item.ChampionItems;

public final class ModItems {

    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, "champions");

    public static final RegistryObject<ChampionEggItem> CHAMPION_EGG =
            ITEMS.register("egg", ChampionEggItem::new);

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }

    public static void wireCommon() {
        // Egg payload (entity type + preset) lives in stack NBT on 1.20.1
        ChampionItems.register(CHAMPION_EGG::get);
    }
}
