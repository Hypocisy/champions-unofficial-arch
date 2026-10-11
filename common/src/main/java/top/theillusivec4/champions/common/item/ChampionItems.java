package top.theillusivec4.champions.common.item;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.Identifier;
import top.theillusivec4.champions.common.champion.ChampionData;

import java.util.function.Supplier;

/**
 * Holds the champion-egg item and its data component types.
 * Platform modules call {@link #register} at startup.
 */
public final class ChampionItems {

    private static Supplier<ChampionEggItem> eggSupplier = null;
    private static Supplier<DataComponentType<Identifier>> entityTypeComponentSupplier = null;
    private static Supplier<DataComponentType<ChampionData>> presetComponentSupplier = null;

    private ChampionItems() {}

    public static void register(
            Supplier<ChampionEggItem> egg,
            Supplier<DataComponentType<Identifier>> entityTypeComponent,
            Supplier<DataComponentType<ChampionData>> presetComponent
    ) {
        eggSupplier = egg;
        entityTypeComponentSupplier = entityTypeComponent;
        presetComponentSupplier = presetComponent;
    }

    public static ChampionEggItem egg() {
        return eggSupplier.get();
    }

    public static DataComponentType<Identifier> entityTypeComponent() {
        return entityTypeComponentSupplier.get();
    }

    public static DataComponentType<ChampionData> presetComponent() {
        return presetComponentSupplier.get();
    }
}
