package top.theillusivec4.champions.neoforge.registry;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import top.theillusivec4.champions.common.champion.ChampionData;
import top.theillusivec4.champions.common.item.ChampionEggItem;
import top.theillusivec4.champions.common.item.ChampionItems;

public final class ModItems {

    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(BuiltInRegistries.ITEM, "champions");

    private static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(BuiltInRegistries.DATA_COMPONENT_TYPE, "champions");

    public static final DeferredHolder<Item, ChampionEggItem> CHAMPION_EGG =
            ITEMS.register("egg", ChampionEggItem::new);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ResourceLocation>> ENTITY_TYPE_COMPONENT =
            DATA_COMPONENTS.register("egg_entity_type", () ->
                    DataComponentType.<ResourceLocation>builder()
                            .persistent(ResourceLocation.CODEC)
                            .networkSynchronized(ResourceLocation.STREAM_CODEC)
                            .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ChampionData>> PRESET_COMPONENT =
            DATA_COMPONENTS.register("egg_preset", () ->
                    DataComponentType.<ChampionData>builder()
                            .persistent(ChampionData.CODEC)
                            .networkSynchronized(ByteBufCodecs.fromCodec(ChampionData.CODEC))
                            .build());

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
        DATA_COMPONENTS.register(bus);
    }

    @SuppressWarnings("unchecked")
    public static void wireCommon() {
        ChampionItems.register(
                CHAMPION_EGG::get,
                ENTITY_TYPE_COMPONENT::get,
                PRESET_COMPONENT::get
        );
    }
}
