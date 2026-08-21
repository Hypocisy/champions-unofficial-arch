package top.theillusivec4.champions.common.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.theillusivec4.champions.api.champion.ChampionTier;
import top.theillusivec4.champions.common.registry.TierRegistry;

import java.io.Reader;
import java.util.*;

/**
 * Loads {@link ChampionTier} definitions from datapack JSON and implements {@link TierRegistry}.
 *
 * <p>Tier files live under {@code data/<namespace>/champions/tier/}.
 * The file name (minus {@code .json}) becomes the tier's registry id path.</p>
 *
 * <h3>Example file — {@code data/champions/champions/tier/elite.json}:</h3>
 * <pre>{@code
 * {
 *   "level": 3,
 *   "display": {
 *     "color": 16736256,
 *     "icon": "champions:textures/gui/tier_elite.png"
 *   }
 * }
 * }</pre>
 *
 * <p>If no {@code display} block is provided, a sensible default is used based on level.
 * If no tier files exist at all, a set of built-in fallback tiers (1–5) is used so the
 * mod remains functional without a datapack.</p>
 */
public final class TierDataLoader
        extends SimplePreparableReloadListener<Map<ResourceLocation, ChampionTier>>
        implements TierRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(TierDataLoader.class);
    private static final String FOLDER = "champions/tier";

    // ── Codecs ────────────────────────────────────────────────────────────────

    private static final Codec<ChampionTier.TierDisplay> DISPLAY_CODEC =
            RecordCodecBuilder.create(inst -> inst.group(
                    Codec.INT.optionalFieldOf("color", 0xFFFFFFFF)
                            .forGetter(ChampionTier.TierDisplay::color),
                    ResourceLocation.CODEC
                            .optionalFieldOf("icon",
                                    ResourceLocation.withDefaultNamespace("textures/gui/icons.png"))
                            .forGetter(ChampionTier.TierDisplay::icon)
            ).apply(inst, ChampionTier.TierDisplay::new));

    /**
     * Codec for the file body. The {@code id} field is injected after parsing
     * from the file path, so it is not part of the JSON schema.
     */
    private static final Codec<TierSpec> TIER_SPEC_CODEC =
            RecordCodecBuilder.create(inst -> inst.group(
                    Codec.INT.fieldOf("level").forGetter(TierSpec::level),
                    DISPLAY_CODEC.optionalFieldOf("display")
                            .forGetter(spec -> Optional.of(spec.display()))
            ).apply(inst, (level, display) ->
                    new TierSpec(level, display.orElse(null))
            ));

    // ── Internal spec ─────────────────────────────────────────────────────────

    /**
     * Intermediate parsed form before the ResourceLocation id is injected.
     */
    private record TierSpec(int level, ChampionTier.TierDisplay display) {

        ChampionTier toTier(ResourceLocation id) {
            ChampionTier.TierDisplay resolved = display != null
                    ? display
                    : ChampionTier.TierDisplay.defaultFor(level);
            return new ChampionTier(id, level, resolved);
        }
    }

    // ── Runtime state ─────────────────────────────────────────────────────────

    /**
     * id → tier, rebuilt on every datapack reload.
     */
    private volatile Map<ResourceLocation, ChampionTier> tiers = buildFallbackTiers();

    /**
     * level → tier, rebuilt alongside {@link #tiers}.
     */
    private volatile Map<Integer, ChampionTier> tiersByLevel = indexByLevel(tiers);

    /** ids whose backing file comes from a jar (built-in) pack, not a {@code file/} pack. */
    private volatile Set<ResourceLocation> builtinIds = Set.of();
    /** staging area written during {@link #prepare}, promoted in {@link #apply}. */
    private Set<ResourceLocation> pendingBuiltinIds = new HashSet<>();

    // ── SimplePreparableReloadListener ────────────────────────────────────────

    @Override
    public @NotNull Map<ResourceLocation, ChampionTier> prepare(
            ResourceManager manager, ProfilerFiller profiler
    ) {
        profiler.startTick();
        Map<ResourceLocation, ChampionTier> result = new HashMap<>();
        Set<ResourceLocation> builtins = new HashSet<>();

        for (Map.Entry<ResourceLocation, Resource> entry :
                manager.listResources(FOLDER, path -> path.getPath().endsWith(".json")).entrySet()) {

            ResourceLocation fileKey = entry.getKey();
            ResourceLocation tierId = fileKeyToTierId(fileKey);

            try (Reader reader = entry.getValue().openAsReader()) {
                JsonElement json = JsonParser.parseReader(reader);
                if (DataLoaders.isDisabled(json)) {
                    continue; // override marks this tier as deleted
                }
                boolean loaded = TIER_SPEC_CODEC.parse(JsonOps.INSTANCE, json)
                        .resultOrPartial(err -> LOGGER.warn(
                                "[Champions] Failed to parse tier '{}': {}", tierId, err))
                        .map(spec -> spec.toTier(tierId))
                        .map(tier -> { result.put(tierId, tier); return true; })
                        .orElse(false);
                if (loaded && DataLoaders.isBuiltin(entry.getValue())) {
                    builtins.add(tierId);
                }
            } catch (Exception e) {
                LOGGER.error("[Champions] Error loading tier file '{}': {}", fileKey, e.getMessage());
            }
        }

        pendingBuiltinIds = builtins;
        profiler.endTick();
        return result;
    }

    @Override
    public void apply(
            Map<ResourceLocation, ChampionTier> prepared,
            @NotNull ResourceManager manager, @NotNull ProfilerFiller profiler
    ) {
        if (prepared.isEmpty()) {
            LOGGER.info("[Champions] No tier datapacks found, using built-in fallback tiers (1–5).");
            tiers = buildFallbackTiers();
            builtinIds = Set.copyOf(tiers.keySet());
        } else {
            LOGGER.info("[Champions] Loaded {} tier(s) from datapacks.", prepared.size());
            tiers = Collections.unmodifiableMap(prepared);
            builtinIds = Set.copyOf(pendingBuiltinIds);
        }
        pendingBuiltinIds = new HashSet<>();
        tiersByLevel = indexByLevel(tiers);
    }

    /** ids backed by a jar (built-in) pack — used by the editor to distinguish overridable entries. */
    public Set<ResourceLocation> getBuiltinIds() {
        return builtinIds;
    }

    // ── TierRegistry ──────────────────────────────────────────────────────────

    @Override
    public Optional<ChampionTier> get(ResourceLocation id) {
        return Optional.ofNullable(tiers.get(id));
    }

    @Override
    public Optional<ChampionTier> getByLevel(int level) {
        return Optional.ofNullable(tiersByLevel.get(level));
    }

    @Override
    public Collection<ChampionTier> getAll() {
        return tiers.values().stream()
                .sorted(Comparator.comparingInt(ChampionTier::level))
                .toList();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Convert a datapack file path to a tier id.
     *
     * <p>{@code data/champions/champions/tier/elite.json}
     * → {@code champions:elite}</p>
     */
    private static ResourceLocation fileKeyToTierId(ResourceLocation fileKey) {
        String path = fileKey.getPath();
        // strip "champions/tier/" prefix and ".json" suffix
        String name = path
                .substring((FOLDER + "/").length())
                .replace(".json", "");
        return ResourceLocation.fromNamespaceAndPath(fileKey.getNamespace(), name);
    }

    private static Map<Integer, ChampionTier> indexByLevel(Map<ResourceLocation, ChampionTier> src) {
        Map<Integer, ChampionTier> index = new HashMap<>();
        for (ChampionTier tier : src.values()) {
            ChampionTier existing = index.put(tier.level(), tier);
            if (existing != null) {
                LOGGER.warn(
                        "[Champions] Duplicate tier level {}: '{}' and '{}'. '{}' wins.",
                        tier.level(), existing.id(), tier.id(), tier.id()
                );
            }
        }
        return Collections.unmodifiableMap(index);
    }

    /**
     * Built-in fallback tiers used when no datapack defines any tiers.
     * Mirrors the legacy 1–5 tier system so the mod works out of the box.
     */
    private static Map<ResourceLocation, ChampionTier> buildFallbackTiers() {
        Map<ResourceLocation, ChampionTier> map = new HashMap<>();
        for (int level = 1; level <= 5; level++) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath("champions",
                    "tier_" + level);
            map.put(id, new ChampionTier(id, level, ChampionTier.TierDisplay.defaultFor(level)));
        }
        return Collections.unmodifiableMap(map);
    }
}
