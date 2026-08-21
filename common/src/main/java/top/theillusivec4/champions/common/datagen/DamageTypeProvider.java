package top.theillusivec4.champions.common.datagen;

import com.google.gson.JsonObject;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageEffects;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageType;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.champions.common.registry.ModDamageTypes;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Generates {@code data/champions/damage_type/*.json}.
 *
 * <p>Mirrors the old project's {@code ModDamageTypes.bootstrap()} call but as a
 * plain {@link DataProvider} so it works from {@code common/} on both platforms.</p>
 */
public final class DamageTypeProvider implements DataProvider {

    private final PackOutput output;
    private final Map<ResourceKey<DamageType>, Entry> entries = new LinkedHashMap<>();

    /** Both NeoForge and Fabric datagen pass a registries future — we accept but don't use it. */
    public DamageTypeProvider(PackOutput output,
                               CompletableFuture<HolderLookup.Provider> registriesUnused) {
        this.output = output;
        addDefaults();
    }

    private void addDefaults() {
        add(ModDamageTypes.REFLECTION,
                "reflection",
                DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER,
                0.1f,
                DamageEffects.HURT);

        add(ModDamageTypes.ENKINDLING_BULLET,
                "enkindling_bullet",
                DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER,
                0.1f,
                DamageEffects.BURNING);
    }

    private void add(ResourceKey<DamageType> key, String messageId,
                     DamageScaling scaling, float exhaustion, DamageEffects effects) {
        entries.put(key, new Entry(messageId, scaling, exhaustion, effects));
    }

    // ── DataProvider ──────────────────────────────────────────────────────────

    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput cache) {
        List<CompletableFuture<?>> futures = new ArrayList<>();
        for (var e : entries.entrySet()) {
            Path path = output.getOutputFolder()
                    .resolve("data")
                    .resolve(e.getKey().location().getNamespace())
                    .resolve("damage_type")
                    .resolve(e.getKey().location().getPath() + ".json");
            futures.add(DataProvider.saveStable(cache, buildJson(e.getValue()), path));
        }
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public @NotNull String getName() { return "Champions Damage Types"; }

    // ── JSON ──────────────────────────────────────────────────────────────────

    private static JsonObject buildJson(Entry entry) {
        JsonObject obj = new JsonObject();
        obj.addProperty("message_id", entry.messageId);
        obj.addProperty("scaling",    scalingId(entry.scaling));
        obj.addProperty("exhaustion", entry.exhaustion);
        if (entry.effects != DamageEffects.HURT) {
            obj.addProperty("effects", effectsId(entry.effects));
        }
        return obj;
    }

    private static String scalingId(DamageScaling s) {
        return switch (s) {
            case NEVER                             -> "never";
            case WHEN_CAUSED_BY_LIVING_NON_PLAYER  -> "when_caused_by_living_non_player";
            case ALWAYS                            -> "always";
        };
    }

    private static String effectsId(DamageEffects e) {
        return switch (e) {
            case HURT     -> "hurt";
            case THORNS   -> "thorns";
            case DROWNING -> "drowning";
            case BURNING  -> "burning";
            case POKING   -> "poking";
            case FREEZING -> "freezing";
        };
    }

    private record Entry(String messageId, DamageScaling scaling,
                         float exhaustion, DamageEffects effects) {}
}
