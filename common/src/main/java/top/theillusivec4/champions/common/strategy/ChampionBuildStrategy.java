package top.theillusivec4.champions.common.strategy;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.champions.api.affix.AffixInstance;
import top.theillusivec4.champions.api.champion.ChampionTier;

import java.util.List;
import java.util.Random;

/**
 * Strategy that assembles the affix list for a newly spawned champion.
 *
 * <p>One implementation ships with the mod:</p>
 * <ul>
 *   <li>{@link ArchetypeStrategy} — uses archetype datapacks</li>
 * </ul>
 *
 * <p>The active strategy is selected in {@link top.theillusivec4.champions.common.champion.ChampionBuilder}
 * at server startup.</p>
 */
public interface ChampionBuildStrategy {

    /**
     * Build the affix list for a new champion.
     *
     * @param entity the mob being made into a champion
     * @param tier   the tier already assigned to this champion
     * @param random dedicated random instance for this spawn
     * @return the list of affix instances to attach (maybe empty, never null)
     */
    BuildResult build(LivingEntity entity, ChampionTier tier, RandomSource random);

    // ── BuildResult ───────────────────────────────────────────────────────────

    /**
     * Return value of {@link #build(LivingEntity, ChampionTier, Random)}.
     *
     * <p>{@code archetypeId} is non-null when {@link ArchetypeStrategy} selected an archetype,
     * and null when no archetype matched.
     * It is stored in {@link top.theillusivec4.champions.common.champion.ChampionData} so
     * that {@link top.theillusivec4.champions.common.phase.PhaseProcessor} can look up the
     * archetype's phase list on every tick.</p>
     */
    record BuildResult(
            List<AffixInstance> affixes,
            @Nullable ResourceLocation archetypeId
    ) {
        /** Convenience constructor for strategies that carry no archetype context. */
        public static BuildResult of(List<AffixInstance> affixes) {
            return new BuildResult(affixes, null);
        }
    }
}
