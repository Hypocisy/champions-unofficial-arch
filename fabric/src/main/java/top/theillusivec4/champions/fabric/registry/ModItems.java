package top.theillusivec4.champions.fabric.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import top.theillusivec4.champions.common.champion.ChampionData;
import top.theillusivec4.champions.common.item.ChampionEggItem;
import top.theillusivec4.champions.common.item.ChampionItems;

public final class ModItems {

    public static final ChampionEggItem CHAMPION_EGG = new ChampionEggItem();

    public static final DataComponentType<ResourceLocation> ENTITY_TYPE_COMPONENT =
            DataComponentType.<ResourceLocation>builder()
                    .persistent(ResourceLocation.CODEC)
                    .networkSynchronized(ResourceLocation.STREAM_CODEC)
                    .build();

    public static final DataComponentType<ChampionData> PRESET_COMPONENT =
            DataComponentType.<ChampionData>builder()
                    .persistent(ChampionData.CODEC)
                    .networkSynchronized(ByteBufCodecs.fromCodec(ChampionData.CODEC))
                    .build();

    public static void register() {
        Registry.register(BuiltInRegistries.ITEM,
                ResourceLocation.fromNamespaceAndPath("champions", "egg"),
                CHAMPION_EGG);
        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE,
                ResourceLocation.fromNamespaceAndPath("champions", "egg_entity_type"),
                ENTITY_TYPE_COMPONENT);
        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE,
                ResourceLocation.fromNamespaceAndPath("champions", "egg_preset"),
                PRESET_COMPONENT);

        ChampionItems.register(
                () -> CHAMPION_EGG,
                () -> ENTITY_TYPE_COMPONENT,
                () -> PRESET_COMPONENT
        );
    }
}
