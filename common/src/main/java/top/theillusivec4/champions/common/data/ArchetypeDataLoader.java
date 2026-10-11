package top.theillusivec4.champions.common.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.theillusivec4.champions.common.archetype.ChampionArchetype;

import java.io.Reader;
import java.util.*;

/**
 * Loads {@link ChampionArchetype} definitions from
 * {@code data/<namespace>/champions/archetype/*.json}.
 *
 * <p>The file name (minus {@code .json}) becomes the archetype's id path.
 * The {@code id} field inside the JSON must match the derived id — if it doesn't,
 * a warning is logged and the file-path id takes precedence.</p>
 */
public final class ArchetypeDataLoader
        extends SimplePreparableReloadListener<Map<Identifier, ChampionArchetype>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArchetypeDataLoader.class);
    private static final String FOLDER = "champions/archetype";

    private volatile Map<Identifier, ChampionArchetype> archetypes = Map.of();

    /** ids backed by a jar (built-in) pack — used by the editor. */
    private volatile Set<Identifier> builtinIds = Set.of();
    private Set<Identifier> pendingBuiltinIds = new HashSet<>();

    // ── SimplePreparableReloadListener ────────────────────────────────────────

    @Override
    @NotNull
    public Map<Identifier, ChampionArchetype> prepare(
            ResourceManager manager, ProfilerFiller profiler
    ) {
        profiler.startTick();
        Map<Identifier, ChampionArchetype> result = new HashMap<>();
        Set<Identifier> builtins = new HashSet<>();

        for (Map.Entry<Identifier, Resource> entry :
                manager.listResources(FOLDER, p -> p.getPath().endsWith(".json")).entrySet()) {

            Identifier fileKey = entry.getKey();
            Identifier archetypeId = fileKeyToId(fileKey);

            try (Reader reader = entry.getValue().openAsReader()) {
                JsonElement json = JsonParser.parseReader(reader);
                if (DataLoaders.isDisabled(json)) {
                    continue; // override marks this archetype as deleted
                }
                boolean[] loaded = { false };
                ChampionArchetype.CODEC.parse(JsonOps.INSTANCE, json)
                        .resultOrPartial(err -> LOGGER.warn(
                                "[Champions] Failed to parse archetype '{}': {}", archetypeId, err))
                        .ifPresent(archetype -> {
                            if (!archetype.id().equals(archetypeId)) {
                                LOGGER.warn(
                                        "[Champions] Archetype id mismatch: file path '{}' vs JSON id '{}'. " +
                                                "Using file path id.",
                                        archetypeId, archetype.id()
                                );
                            }
                            result.put(archetypeId, fixId(archetype, archetypeId));
                            loaded[0] = true;
                        });
                if (loaded[0] && DataLoaders.isBuiltin(entry.getValue())) {
                    builtins.add(archetypeId);
                }
            } catch (Exception e) {
                LOGGER.error("[Champions] Error loading archetype '{}': {}", fileKey, e.getMessage());
            }
        }

        pendingBuiltinIds = builtins;
        profiler.endTick();
        return result;
    }

    @Override
    public void apply(
            @NotNull Map<Identifier, ChampionArchetype> prepared,
            @NotNull ResourceManager manager, @NotNull ProfilerFiller profiler
    ) {
        archetypes = Collections.unmodifiableMap(prepared);
        builtinIds = Set.copyOf(pendingBuiltinIds);
        pendingBuiltinIds = new HashSet<>();
        LOGGER.info("[Champions] Loaded {} archetype(s).", archetypes.size());
    }

    /** ids backed by a jar (built-in) pack — used by the editor to distinguish overridable entries. */
    public Set<Identifier> getBuiltinIds() {
        return builtinIds;
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    public Collection<ChampionArchetype> getAll() {
        return archetypes.values();
    }

    public boolean hasAny() {
        return !archetypes.isEmpty();
    }

    public java.util.Optional<ChampionArchetype> get(Identifier id) {
        return java.util.Optional.ofNullable(archetypes.get(id));
    }

    public Set<Identifier> getAllKeys() {
        return archetypes.keySet();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static Identifier fileKeyToId(Identifier fileKey) {
        String path = fileKey.getPath()
                .substring((FOLDER + "/").length())
                .replace(".json", "");
        return Identifier.fromNamespaceAndPath(fileKey.getNamespace(), path);
    }

    private static ChampionArchetype fixId(ChampionArchetype archetype, Identifier id) {
        if (archetype.id().equals(id)) return archetype;
        return new ChampionArchetype(
                id,
                archetype.tierRange(),
                archetype.weight(),
                archetype.entityFilter(),
                archetype.affixPools(),
                archetype.phases()
        );
    }
}
