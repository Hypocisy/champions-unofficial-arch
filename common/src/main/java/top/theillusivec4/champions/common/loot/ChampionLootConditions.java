package top.theillusivec4.champions.common.loot;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * Holds the static {@link LootItemConditionType} reference for the champion-properties condition.
 *
 * <p>The type must be registered per-platform before the registry is frozen:
 * <ul>
 *   <li>NeoForge: via {@code ModLootConditions.register(modBus)} (DeferredRegister).</li>
 *   <li>Fabric: via {@code ChampionLootConditions.registerFabric()} during {@code onInitialize()}.</li>
 * </ul>
 * Affix/loot code reads {@link #CHAMPION_PROPERTIES} after startup.</p>
 */
public final class ChampionLootConditions {

    private ChampionLootConditions() {}

    /** Set by the platform registration helper after the type is registered. */
    public static MapCodec<? extends LootItemCondition> CHAMPION_PROPERTIES = ChampionPropertyCondition.CODEC;

    /**
     * Fabric-only: call during {@code onInitialize()} before the registry is frozen.
     * On NeoForge use {@code ModLootConditions} with a DeferredRegister instead.
     */
    public static void registerFabric() {
        CHAMPION_PROPERTIES = Registry.register(
                BuiltInRegistries.LOOT_CONDITION_TYPE,
                Identifier.fromNamespaceAndPath(
                        "champions", "champion_properties"),
                ChampionPropertyCondition.CODEC
        );
    }
}
