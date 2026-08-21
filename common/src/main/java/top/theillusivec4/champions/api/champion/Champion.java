package top.theillusivec4.champions.api.champion;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import top.theillusivec4.champions.api.affix.AffixInstance;
import top.theillusivec4.champions.api.affix.AffixType;

import java.util.List;
import java.util.Optional;

/**
 * Read-only view of a champion entity.
 *
 * <p>This is the primary API surface for third-party mods. Obtain an instance via
 * {@code ChampionsApi.getChampion(entity)}. Mutation is intentionally not exposed here —
 * use {@link Server} (internal) for server-side mutation, or fire events to interact
 * with champions indirectly.</p>
 */
public interface Champion {

    /**
     * The underlying mob. Always non-null while this champion exists.
     */
    LivingEntity entity();

    /**
     * The tier this champion was assigned at spawn.
     */
    ChampionTier tier();

    /**
     * The current live affix list, including any affixes added by phase effects.
     * Order is not guaranteed to be stable across phase transitions.
     */
    List<AffixInstance> affixes();

    /**
     * Returns true if this champion currently has an affix of the given type.
     */
    default boolean hasAffix(AffixType<?> type) {
        return affixes().stream().anyMatch(i -> i.type() == type);
    }

    /**
     * Returns the first affix instance of the given type, if present.
     */
    default Optional<AffixInstance> findAffix(AffixType<?> type) {
        return affixes().stream().filter(i -> i.type() == type).findFirst();
    }

    // ── Server ────────────────────────────────────────────────────────────────

    /**
     * Server-side champion view. Adds mutation operations that must only run on the server.
     *
     * <p>Internal use only — not part of the public API contract. Third-party mods should
     * not depend on this interface directly; interact via events instead.</p>
     */
    interface Server extends Champion {

        /**
         * Add an affix instance to this champion.
         * Triggers goal setup and syncs the change to the client.
         */
        void addAffix(AffixInstance instance);

        /**
         * Remove an affix instance from this champion.
         * Triggers goal teardown and syncs the change to the client.
         */
        void removeAffix(AffixInstance instance);

        /**
         * Remove all affix instances of the given type.
         * Useful for phase teardown where the original instance reference is not available.
         */
        void removeAffixByType(AffixType<?> type);

        /**
         * The base affix list — affixes assigned at spawn, before any phase additions.
         * This is what gets serialized to disk.
         */
        List<AffixInstance> baseAffixes();

        /**
         * The archetype ID this champion was generated from, if any.
         */
        Optional<ResourceLocation> archetypeId();
    }

    // ── Client ────────────────────────────────────────────────────────────────

    /**
     * Client-side champion view.
     * Affix data objects are default-initialised — the client only tracks type and strength.
     */
    interface Client extends Champion {
        // No additional methods for now.
        // Rendering helpers (particle color, display name) are on ChampionTier.
    }
}
