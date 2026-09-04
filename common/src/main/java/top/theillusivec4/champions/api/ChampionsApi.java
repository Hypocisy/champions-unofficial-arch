package top.theillusivec4.champions.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import top.theillusivec4.champions.api.affix.AffixType;
import top.theillusivec4.champions.api.champion.Champion;
import top.theillusivec4.champions.api.champion.ChampionTier;

import java.util.Collection;
import java.util.Optional;

/**
 * Primary entry point for the Champions Unofficial public API.
 *
 * <p>Access via the static {@link #get()} method. The implementation is injected by the
 * mod at startup — calling {@link #get()} before the mod is loaded will throw.</p>
 *
 * <h3>Typical usage:</h3>
 * <pre>{@code
 * // Check if an entity is a champion
 * ChampionsApi.get().getChampion(entity).ifPresent(champion -> {
 *     int tier = champion.tier().level();
 *     boolean hasLively = champion.hasAffix(MyAffixTypes.LIVELY);
 * });
 *
 * // Look up an affix type by id
 * ChampionsApi.get().getAffixType(new ResourceLocation("champions:adaptable"))
 *     .ifPresent(type -> { ... });
 * }</pre>
 */
public interface ChampionsApi {

    // ── Champion queries ──────────────────────────────────────────────────────

    /**
     * Returns the {@link Champion} view of {@code entity}, if it is currently a champion.
     *
     * <p>Returns empty if the entity has no champion attachment, or if the attachment exists
     * but has not been fully initialised (e.g. during early spawn events).</p>
     */
    Optional<Champion> getChampion(LivingEntity entity);

    /**
     * Returns true if {@code entity} is currently a champion.
     * Equivalent to {@code getChampion(entity).isPresent()} but avoids allocating an Optional.
     */
    boolean isChampion(LivingEntity entity);

    // ── Affix type registry ───────────────────────────────────────────────────

    /**
     * Look up a registered affix type by its registry id.
     *
     * <pre>{@code
     * ChampionsApi.get().getAffixType(new ResourceLocation("champions:lively"))
     *     .ifPresent(type -> champion.hasAffix(type));
     * }</pre>
     */
    Optional<AffixType<?>> getAffixType(ResourceLocation id);

    /**
     * Returns the registry id for {@code type}, if it is registered.
     * Returns empty for affix types that were not registered through the registry.
     */
    Optional<ResourceLocation> getAffixTypeId(AffixType<?> type);

    /**
     * Returns all registered affix types.
     * The returned collection is unmodifiable and reflects the state at call time.
     */
    Collection<AffixType<?>> getAffixTypes();

    // ── Tier registry ─────────────────────────────────────────────────────────

    /**
     * Look up a registered tier by its registry id.
     */
    Optional<ChampionTier> getTier(ResourceLocation id);

    /**
     * Look up a registered tier by its numeric level.
     * Returns the first tier whose {@link ChampionTier#level()} equals {@code level}.
     */
    Optional<ChampionTier> getTierByLevel(int level);

    /**
     * Returns all registered tiers, ordered by {@link ChampionTier#level()} ascending.
     */
    Collection<ChampionTier> getTiers();

    // ── Static accessor ───────────────────────────────────────────────────────

    /**
     * Returns the active API implementation.
     *
     * @throws IllegalStateException if called before the mod has finished loading
     */
    static ChampionsApi get() {
        ChampionsApi instance = Holder.INSTANCE;
        if (instance == null) {
            throw new IllegalStateException(
                    "ChampionsApi has not been initialised yet. " +
                            "Do not call ChampionsApi.get() before the mod is loaded."
            );
        }
        return instance;
    }

    /**
     * Register the API implementation. Called once by the mod at startup.
     * Not for use by third-party mods.
     *
     * @throws IllegalStateException if an implementation is already registered
     */
    static void register(ChampionsApi impl) {
        if (Holder.INSTANCE != null) {
            throw new IllegalStateException("ChampionsApi implementation already registered.");
        }
        Holder.INSTANCE = impl;
    }

    /**
     * Holder for the singleton instance. Separate class defers initialisation.
     */
    final class Holder {
        private Holder() {
        }

        private static ChampionsApi INSTANCE = null;
    }
}
