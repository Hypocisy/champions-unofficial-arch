package top.theillusivec4.champions.common.config;

/**
 * Which loot sources are rolled when a champion dies.
 *
 * <ul>
 *   <li>{@link #CONFIG} — only the config-string drops ({@code lootDrops} entries)</li>
 *   <li>{@link #LOOT_TABLE} — only the {@code champions:champion_loot} loot table</li>
 *   <li>{@link #CONFIG_AND_LOOT_TABLE} — both (legacy behaviour)</li>
 * </ul>
 */
public enum LootSource {
    CONFIG,
    LOOT_TABLE,
    CONFIG_AND_LOOT_TABLE;

    public boolean rollsConfig() {
        return this == CONFIG || this == CONFIG_AND_LOOT_TABLE;
    }

    public boolean rollsLootTable() {
        return this == LOOT_TABLE || this == CONFIG_AND_LOOT_TABLE;
    }
}
