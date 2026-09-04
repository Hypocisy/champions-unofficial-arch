package top.theillusivec4.champions.common.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import top.theillusivec4.champions.common.utils.Utils;

/**
 * Entity-type tag keys used by the Champions mod.
 *
 * <p>JSON definitions live at
 * {@code data/champions/tags/entity_types/*.json} under the generated resources.</p>
 *
 * <p>Datapack authors can override these tags to control which mobs are eligible for
 * champion spawn ({@link #ALLOW_CHAMPIONS}) or are treated as ender-type entities
 * ({@link #IS_ENDER}).</p>
 */
public final class ChampionsTags {

    private ChampionsTags() {}

    /**
     * Mobs in this tag are eligible to become champions.
     *
     * <p>The default set mirrors the 37 vanilla hostile/neutral mobs from the original
     * Champions mod. Add entries via a datapack to enable champion spawning for modded
     * mobs; remove entries to prevent specific vanilla mobs from becoming champions.</p>
     */
    public static final TagKey<EntityType<?>> ALLOW_CHAMPIONS = entityTag("allow_champions");

    /**
     * Mobs in this tag are treated as ender-type entities.
     *
     * <p>Intended for use in archetype {@code entity_filter} blocks via
     * {@code "type": "champions:entity_tag", "tag": "champions:is_ender"}.
     * Defaults to: ender_dragon, endermite, enderman, shulker.</p>
     */
    public static final TagKey<EntityType<?>> IS_ENDER = entityTag("is_ender");

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static TagKey<EntityType<?>> entityTag(String path) {
        return TagKey.create(Registries.ENTITY_TYPE,
                Utils.key(path));
    }
}
