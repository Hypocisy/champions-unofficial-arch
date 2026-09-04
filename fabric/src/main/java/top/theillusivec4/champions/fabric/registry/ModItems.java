package top.theillusivec4.champions.fabric.registry;
import top.theillusivec4.champions.common.utils.Utils;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import top.theillusivec4.champions.common.item.ChampionEggItem;
import top.theillusivec4.champions.common.item.ChampionItems;

public final class ModItems {

    public static final ChampionEggItem CHAMPION_EGG = new ChampionEggItem();

    public static void register() {
        Registry.register(BuiltInRegistries.ITEM,
                Utils.key("egg"),
                CHAMPION_EGG);

        // Egg payload (entity type + preset) lives in stack NBT on 1.20.1
        ChampionItems.register(() -> CHAMPION_EGG);
    }
}
