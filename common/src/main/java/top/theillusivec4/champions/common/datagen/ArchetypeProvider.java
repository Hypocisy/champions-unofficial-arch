package top.theillusivec4.champions.common.datagen;
import top.theillusivec4.champions.common.utils.Utils;

import com.mojang.serialization.JsonOps;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.champions.common.archetype.AffixPool;
import top.theillusivec4.champions.common.archetype.ChampionArchetype;
import top.theillusivec4.champions.common.archetype.TierRange;
import top.theillusivec4.champions.common.archetype.WeightedAffix;
import top.theillusivec4.champions.common.filter.EntityFilter;
import top.theillusivec4.champions.common.phase.ChampionPhase;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Generates {@code data/<namespace>/champions/archetype/*.json} archetype definition files.
 *
 * <p>Use the fluent {@link Builder} returned by {@link #archetype(String, String)} to
 * configure each archetype, then call {@link Builder#build()} to register it.</p>
 *
 * <h3>Example:</h3>
 * <pre>{@code
 * provider.archetype("champions", "default_monster")
 *     .weight(10)
 *     .tierRange(1, 5)
 *     .entityFilter(EntityFilter.MobCategoryFilter.of("monster"))
 *     .pool(pool -> pool
 *         .tierRange(1, 2)
 *         .candidate("champions:lively", 10, 1, 1)
 *         .candidate("champions:knocking", 12, 1, 1)
 *         .count(1, 1))
 *     .build();
 * }</pre>
 */
public class ArchetypeProvider implements DataProvider {

    private final PackOutput output;
    private final List<ChampionArchetype> archetypes = new ArrayList<>();

    public ArchetypeProvider(PackOutput output) {
        this.output = output;
    }

    // ── Builder API ───────────────────────────────────────────────────────────

    /** Start building an archetype with the given namespace and name. */
    public Builder archetype(String namespace, String name) {
        return new Builder(this, Utils.key(namespace, name));
    }

    /** Convenience — namespace defaults to {@code "champions"}. */
    public Builder archetype(String name) {
        return archetype("champions", name);
    }

    void addArchetype(ChampionArchetype archetype) {
        archetypes.add(archetype);
    }

    // ── DataProvider ──────────────────────────────────────────────────────────

    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput cache) {
        List<CompletableFuture<?>> futures = new ArrayList<>();
        for (ChampionArchetype archetype : archetypes) {
            Path path = output.getOutputFolder()
                    .resolve("data")
                    .resolve(archetype.id().getNamespace())
                    .resolve("champions/archetype")
                    .resolve(archetype.id().getPath() + ".json");

            futures.add(ChampionArchetype.CODEC.encodeStart(JsonOps.INSTANCE, archetype)
                    .resultOrPartial(System.err::println)
                    .map(json -> DataProvider.saveStable(cache, json, path))
                    .orElseGet(() -> CompletableFuture.completedFuture(null)));
        }
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public @NotNull String getName() {
        return "Champions Archetypes";
    }

    // ── Fluent builder ────────────────────────────────────────────────────────

    public static final class Builder {

        private final ArchetypeProvider parent;
        private final ResourceLocation id;
        private int weight = 10;
        private TierRange tierRange = TierRange.ANY;
        private EntityFilter entityFilter = EntityFilter.ANY;
        private final List<AffixPool> pools = new ArrayList<>();
        private final List<ChampionPhase> phases = new ArrayList<>();

        private Builder(ArchetypeProvider parent, ResourceLocation id) {
            this.parent = parent;
            this.id = id;
        }

        public Builder weight(int weight) {
            this.weight = weight;
            return this;
        }

        public Builder tierRange(int min, int max) {
            this.tierRange = new TierRange(min, max);
            return this;
        }

        public Builder entityFilter(EntityFilter filter) {
            this.entityFilter = filter;
            return this;
        }

        /** Add a pool using a {@link PoolBuilder} lambda. */
        public Builder pool(Consumer<PoolBuilder> configurator) {
            PoolBuilder pb = new PoolBuilder();
            configurator.accept(pb);
            pools.add(pb.build());
            return this;
        }

        public Builder phase(ChampionPhase phase) {
            phases.add(phase);
            return this;
        }

        /** Finalize and register this archetype with the parent provider. */
        public ArchetypeProvider build() {
            parent.addArchetype(new ChampionArchetype(
                    id, tierRange, weight, entityFilter, pools, phases));
            return parent;
        }
    }

    // ── Pool builder ──────────────────────────────────────────────────────────

    public static final class PoolBuilder {

        private TierRange tierRange = TierRange.ANY;
        private final List<WeightedAffix> candidates = new ArrayList<>();
        private int minCount = 1;
        private int maxCount = 1;

        public PoolBuilder tierRange(int min, int max) {
            this.tierRange = new TierRange(min, max);
            return this;
        }

        /**
         * Add a candidate affix.
         *
         * @param affixId      full resource location, e.g. {@code "champions:lively"}
         * @param weight       relative draw weight
         * @param minStrength  minimum strength (inclusive)
         * @param maxStrength  maximum strength (inclusive)
         */
        public PoolBuilder candidate(String affixId, int weight, int minStrength, int maxStrength) {
            candidates.add(new WeightedAffix(
                    new ResourceLocation(affixId), weight, minStrength, maxStrength));
            return this;
        }

        /** Shorthand — fixed strength. */
        public PoolBuilder candidate(String affixId, int weight, int strength) {
            return candidate(affixId, weight, strength, strength);
        }

        public PoolBuilder count(int min, int max) {
            this.minCount = min;
            this.maxCount = max;
            return this;
        }

        /** Exactly {@code n} draws. */
        public PoolBuilder count(int n) {
            return count(n, n);
        }

        private AffixPool build() {
            return new AffixPool(tierRange, candidates, minCount, maxCount);
        }
    }
}
