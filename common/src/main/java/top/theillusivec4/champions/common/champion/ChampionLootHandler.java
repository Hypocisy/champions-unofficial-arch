package top.theillusivec4.champions.common.champion;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import top.theillusivec4.champions.api.champion.Champion;
import top.theillusivec4.champions.common.config.ChampionsConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Handles champion loot drops on death.
 *
 * <p>Two loot sources are combined:</p>
 * <ol>
 *   <li><b>Loot table</b> — {@code champions:loot_tables/champion_loot.json} is rolled once.
 *       The table uses {@code champions:champion_properties} conditions to gate entries by tier.</li>
 *   <li><b>Config drops</b> — {@link ChampionsConfig#lootDrops} entries are drawn
 *       ({@link ChampionsConfig#lootScaling} × tier draws) and dropped as items directly.</li>
 * </ol>
 */
public final class ChampionLootHandler {

    private ChampionLootHandler() {}

    public static final ResourceKey<LootTable> CHAMPION_LOOT_TABLE =
            ResourceKey.create(Registries.LOOT_TABLE,
                    ResourceLocation.fromNamespaceAndPath("champions", "champion_loot"));

    // Re-entrancy guard: loot table rolling itself triggers entity loot contexts —
    // without this guard we'd recursively enter dropLoot for the same entity.
    private static final ThreadLocal<Boolean> IS_PROCESSING =
            ThreadLocal.withInitial(() -> false);

    /**
     * Drop loot for a champion that just died.
     *
     * @param entity   the dying entity
     * @param champion the champion data
     * @param level    the server level
     * @param source   the damage source that killed the entity (used for loot context)
     */
    public static void dropLoot(LivingEntity entity, Champion champion,
                                ServerLevel level, DamageSource source) {
        if (IS_PROCESSING.get()) return;
        if (!level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) return;

        IS_PROCESSING.set(true);
        try {
            // ── 1. Roll loot table ────────────────────────────────────────────
            rollLootTable(entity, champion, level, source);

            // ── 2. Config-string drops ────────────────────────────────────────
            rollConfigDrops(entity, champion, level);
        } finally {
            IS_PROCESSING.set(false);
        }
    }

    // ── Loot table ────────────────────────────────────────────────────────────

    private static void rollLootTable(LivingEntity entity, Champion champion,
                                      ServerLevel level, DamageSource source) {
        LootTable table = level.getServer().reloadableRegistries()
                .getLootTable(CHAMPION_LOOT_TABLE);
        if (table == LootTable.EMPTY) return;

        LootParams.Builder params = new LootParams.Builder(level)
                .withParameter(LootContextParams.THIS_ENTITY, entity)
                .withParameter(LootContextParams.ORIGIN, entity.position())
                .withParameter(LootContextParams.DAMAGE_SOURCE, source)
                .withOptionalParameter(LootContextParams.ATTACKING_ENTITY, source.getEntity())
                .withOptionalParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, source.getDirectEntity());

        LivingEntity killCredit = entity.getKillCredit();
        if (killCredit instanceof Player player) {
            params = params
                    .withParameter(LootContextParams.LAST_DAMAGE_PLAYER, player)
                    .withLuck(player.getLuck());
        }

        table.getRandomItems(params.create(LootContextParamSets.ENTITY),
                stack -> spawnDrop(entity, level, stack));
    }

    // ── Config drops ──────────────────────────────────────────────────────────

    private static void rollConfigDrops(LivingEntity entity, Champion champion,
                                        ServerLevel level) {
        int tierLevel = champion.tier().level();
        List<LootEntry> eligible = buildEligibleList(tierLevel);
        if (eligible.isEmpty()) return;

        int draws = ChampionsConfig.lootScaling ? tierLevel : 1;
        for (int i = 0; i < draws; i++) {
            LootEntry pick = weightedPick(eligible, level.random.nextFloat());
            if (pick == null) continue;

            ItemStack stack = new ItemStack(pick.item, pick.amount);
            if (pick.enchanted) {
                EnchantmentHelper.enchantItem(
                        level.random, stack, 30,
                        level.registryAccess(), Optional.empty());
            }
            spawnDrop(entity, level, stack);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static void spawnDrop(LivingEntity entity, ServerLevel level, ItemStack stack) {
        if (stack.isEmpty()) return;
        ItemEntity drop = new ItemEntity(level,
                entity.getX(), entity.getY() + entity.getBbHeight() / 2.0, entity.getZ(),
                stack);
        drop.setDefaultPickUpDelay();
        level.addFreshEntity(drop);
    }

    private static List<LootEntry> buildEligibleList(int tierLevel) {
        List<LootEntry> list = new ArrayList<>();
        for (String raw : ChampionsConfig.lootDrops) {
            LootEntry entry = parse(raw);
            if (entry != null && entry.tier <= tierLevel) list.add(entry);
        }
        return list;
    }

    private static LootEntry weightedPick(List<LootEntry> entries, float roll) {
        int total = entries.stream().mapToInt(e -> e.weight).sum();
        if (total <= 0) return null;
        int target = (int) (roll * total);
        int cumulative = 0;
        for (LootEntry e : entries) {
            cumulative += e.weight;
            if (target < cumulative) return e;
        }
        return entries.getLast();
    }

    private static LootEntry parse(String raw) {
        String[] parts = raw.split(";");
        if (parts.length != 5) return null;
        try {
            int tier     = Integer.parseInt(parts[0].trim());
            var item     = BuiltInRegistries.ITEM.get(ResourceLocation.parse(parts[1].trim()));
            int amount   = Integer.parseInt(parts[2].trim());
            boolean ench = Boolean.parseBoolean(parts[3].trim());
            int weight   = Integer.parseInt(parts[4].trim());
            if (item == null) return null;
            return new LootEntry(tier, item, amount, ench, weight);
        } catch (Exception e) {
            return null;
        }
    }

    record LootEntry(int tier, Item item, int amount, boolean enchanted, int weight) {}
}
