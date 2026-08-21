package top.theillusivec4.champions.common.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Loads {@link ModifierSetting} definitions from datapack JSON.
 *
 * <p>Files live under {@code data/<namespace>/modifier_setting/*.json}. Each file describes
 * one attribute scaling rule applied to champions on spawn (e.g. max_health ×0.35).</p>
 *
 * <p>Cross-platform port of the old NeoForge-only loader. Uses SLF4J (aligned with
 * {@link TierDataLoader}) and follows the correct prepare/apply contract — {@code apply}
 * <em>replaces</em> the loaded data rather than accumulating into it.</p>
 */
public final class AttributesModifierDataLoader
        extends SimplePreparableReloadListener<Map<ResourceLocation, ModifierSetting>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AttributesModifierDataLoader.class);
    private static final String FOLDER = "modifier_setting";

    private volatile Map<ResourceLocation, ModifierSetting> loadedData = new HashMap<>();

    /** ids backed by a jar (built-in) pack — used by the editor. */
    private volatile Set<ResourceLocation> builtinIds = Set.of();
    private Set<ResourceLocation> pendingBuiltinIds = new HashSet<>();

    public static String getFolder() {
        return FOLDER;
    }

    @Override
    @NotNull
    public Map<ResourceLocation, ModifierSetting> prepare(
            @NotNull ResourceManager manager, @NotNull ProfilerFiller profiler
    ) {
        profiler.startTick();
        Map<ResourceLocation, ModifierSetting> result = new HashMap<>();
        Set<ResourceLocation> builtins = new HashSet<>();

        for (Map.Entry<ResourceLocation, Resource> entry :
                manager.listResources(FOLDER, p -> p.getPath().endsWith(".json")).entrySet()) {
            ResourceLocation fileKey = entry.getKey();
            try (Reader reader = entry.getValue().openAsReader()) {
                JsonElement element = JsonParser.parseReader(reader);
                if (DataLoaders.isDisabled(element)) {
                    continue; // override marks this modifier as deleted
                }
                boolean[] loaded = { false };
                ModifierSetting.MAP_CODEC.codec().parse(JsonOps.INSTANCE, element)
                        .resultOrPartial(error -> LOGGER.warn(
                                "[Champions] Failed to parse modifier_setting '{}': {}", fileKey, error))
                        .ifPresent(setting -> {
                            result.put(fileKey, setting);
                            loaded[0] = true;
                        });
                if (loaded[0] && DataLoaders.isBuiltin(entry.getValue())) {
                    builtins.add(fileKey);
                }
            } catch (Exception e) {
                LOGGER.error("[Champions] Error loading modifier_setting '{}': {}",
                        fileKey, e.getMessage());
            }
        }

        pendingBuiltinIds = builtins;
        profiler.endTick();
        return result;
    }

    @Override
    public void apply(
            @NotNull Map<ResourceLocation, ModifierSetting> prepared,
            @NotNull ResourceManager manager, @NotNull ProfilerFiller profiler
    ) {
        loadedData = prepared;
        builtinIds = Set.copyOf(pendingBuiltinIds);
        pendingBuiltinIds = new HashSet<>();
        LOGGER.info("[Champions] Loaded {} modifier_setting entrie(s) from datapacks.", prepared.size());
    }

    public Map<ResourceLocation, ModifierSetting> getLoadedData() {
        return loadedData;
    }

    /** ids backed by a jar (built-in) pack — used by the editor to distinguish overridable entries. */
    public Set<ResourceLocation> getBuiltinIds() {
        return builtinIds;
    }
}
