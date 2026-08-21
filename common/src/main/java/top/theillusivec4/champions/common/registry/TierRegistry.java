package top.theillusivec4.champions.common.registry;

import net.minecraft.resources.ResourceLocation;
import top.theillusivec4.champions.api.champion.ChampionTier;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Query interface for the champion tier registry.
 *
 * <p>Tiers are loaded from datapack at startup and are immutable during a session.
 * The registry is rebuilt on datapack reload.</p>
 */
public interface TierRegistry {

    Optional<ChampionTier> get(ResourceLocation id);

    Optional<ChampionTier> getByLevel(int level);

    /**
     * All registered tiers ordered by {@link ChampionTier#level()} ascending.
     * Unmodifiable.
     */
    Collection<ChampionTier> getAll();

    /**
     * The highest-level tier available.
     * Returns empty only if no tiers are registered (should never happen in practice).
     */
    default Optional<ChampionTier> getMax() {
        return getAll().stream().max(Comparator.comparingInt(ChampionTier::level));
    }

    /**
     * The lowest-level tier available.
     */
    default Optional<ChampionTier> getMin() {
        return getAll().stream().min(Comparator.comparingInt(ChampionTier::level));
    }

    /**
     * Returns all tiers within the inclusive range [{@code minLevel}, {@code maxLevel}],
     * ordered by level ascending.
     */
    default List<ChampionTier> getRange(int minLevel, int maxLevel) {
        return getAll().stream()
                .filter(t -> t.level() >= minLevel && t.level() <= maxLevel)
                .sorted(Comparator.comparingInt(ChampionTier::level))
                .toList();
    }
}
