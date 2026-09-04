package top.theillusivec4.champions.common.item;

import java.util.function.Supplier;

/**
 * Holds the champion-egg item.
 * Platform modules call {@link #register} at startup.
 *
 * <p>Egg data (target entity type + optional champion preset) lives directly in the
 * item stack NBT — 1.20.1 has no data components. See {@link ChampionEggItem}
 * data accessors.</p>
 */
public final class ChampionItems {

    private static Supplier<ChampionEggItem> eggSupplier = null;

    private ChampionItems() {}

    public static void register(Supplier<ChampionEggItem> egg) {
        eggSupplier = egg;
    }

    public static ChampionEggItem egg() {
        return eggSupplier.get();
    }
}
