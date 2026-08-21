package top.theillusivec4.champions.common.strategy;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.theillusivec4.champions.api.ChampionsApi;
import top.theillusivec4.champions.api.affix.AffixInstance;
import top.theillusivec4.champions.api.champion.ChampionTier;
import top.theillusivec4.champions.common.archetype.AffixPool;
import top.theillusivec4.champions.common.archetype.ChampionArchetype;
import top.theillusivec4.champions.common.archetype.WeightedAffix;
import top.theillusivec4.champions.common.data.ArchetypeDataLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Default build strategy — selects an archetype by weighted random, then
 * draws affixes from its active pools.
 *
 * <p>Selection steps:</p>
 * <ol>
 *   <li>Collect all archetypes whose {@code tierRange} and {@code entityFilter} match.</li>
 *   <li>Pick one by weight.</li>
 *   <li>For each active pool, draw between {@code minCount} and {@code maxCount} affixes
 *       by weighted random without replacement.</li>
 *   <li>For each drawn affix, sample strength in [{@code minStrength}, {@code maxStrength}].</li>
 * </ol>
 */
public final class ArchetypeStrategy implements ChampionBuildStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArchetypeStrategy.class);

    private final ArchetypeDataLoader loader;

    public ArchetypeStrategy(ArchetypeDataLoader loader) {
        this.loader = loader;
    }

    @Override
    public BuildResult build(
            LivingEntity entity, ChampionTier tier, RandomSource random
    ) {
        int level = tier.level();

        // 1. Collect matching archetypes
        List<ChampionArchetype> candidates = loader.getAll().stream()
                .filter(a -> a.matches(entity, level))
                .toList();

        if (candidates.isEmpty()) {
            LOGGER.debug("[Champions] No matching archetype for {} at tier {}",
                    entity.getType(), level);
            return BuildResult.of(new ArrayList<>());
        }

        // 2. Weighted pick
        ChampionArchetype archetype = weightedPick(candidates, random);

        // 3. Draw from active pools
        List<AffixInstance> affixes = new ArrayList<>();
        for (AffixPool pool : archetype.getActivePools(level)) {
            affixes.addAll(drawFromPool(pool, random));
        }

        return new BuildResult(affixes, archetype.id());
    }

    // ── Weighted selection ────────────────────────────────────────────────────

    private static ChampionArchetype weightedPick(
            List<ChampionArchetype> candidates, RandomSource random
    ) {
        int totalWeight = candidates.stream()
                .mapToInt(a -> Math.max(a.weight(), 0))
                .sum();
        // A datapack may declare every weight as 0 (or negative). nextInt(0) throws,
        // so fall back to a uniform pick rather than crashing the spawn.
        if (totalWeight <= 0) return candidates.get(random.nextInt(candidates.size()));

        int roll = random.nextInt(totalWeight);
        int cumulative = 0;
        for (ChampionArchetype candidate : candidates) {
            cumulative += Math.max(candidate.weight(), 0);
            if (roll < cumulative) return candidate;
        }
        return candidates.getLast(); // fallback (floating-point safety)
    }

    // ── Pool drawing ──────────────────────────────────────────────────────────

    /**
     * Draw affixes from a pool without replacement.
     * The pool's candidate list is shuffled by weight, then the first {@code count}
     * entries that resolve successfully are taken.
     */
    private List<AffixInstance> drawFromPool(AffixPool pool, RandomSource random) {
        if (pool.candidates().isEmpty()) return List.of();

        int count = pool.minCount() == pool.maxCount()
                ? pool.minCount()
                : pool.minCount() + random.nextInt(pool.maxCount() - pool.minCount() + 1);

        // Weighted shuffle: build a mutable copy, draw without replacement
        List<WeightedAffix> remaining = new ArrayList<>(pool.candidates());
        List<AffixInstance> drawn = new ArrayList<>();

        while (!remaining.isEmpty() && drawn.size() < count) {
            WeightedAffix picked = weightedPickAffix(remaining, random);
            remaining.remove(picked);

            ChampionsApi.get().getAffixType(picked.affixId()).ifPresent(type -> {
                int strength = picked.minStrength() == picked.maxStrength()
                        ? picked.minStrength()
                        : picked.minStrength() + random.nextInt(
                        picked.maxStrength() - picked.minStrength() + 1);
                drawn.add(new AffixInstance(type, strength));
            });
        }

        return drawn;
    }

    private static WeightedAffix weightedPickAffix(
            List<WeightedAffix> candidates, RandomSource random
    ) {
        int total = candidates.stream()
                .mapToInt(c -> Math.max(c.weight(), 0))
                .sum();
        // All-zero weights previously made every `roll < cumulative` test fail, so the
        // last candidate was always returned. Pick uniformly instead.
        if (total <= 0) return candidates.get(random.nextInt(candidates.size()));

        int roll = random.nextInt(total);
        int cumulative = 0;
        for (WeightedAffix candidate : candidates) {
            cumulative += Math.max(candidate.weight(), 0);
            if (roll < cumulative) return candidate;
        }
        return candidates.getLast();
    }
}
