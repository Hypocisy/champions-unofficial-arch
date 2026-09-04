package top.theillusivec4.champions.common.datagen;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.champions.common.data.AttributesModifierDataLoader;
import top.theillusivec4.champions.common.data.ChampionModifierCondition;
import top.theillusivec4.champions.common.data.ModifierSetting;
import top.theillusivec4.champions.common.filter.EntityFilter;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Generates {@code data/<namespace>/modifier_setting/<path>.json} for every registered attribute.
 *
 * <p>Only five attributes are enabled by default:</p>
 * <ul>
 *   <li>max_health    — ×0.35 add_multiplied_total</li>
 *   <li>attack_damage — ×0.50 add_multiplied_total</li>
 *   <li>armor         — +2.0  add_value</li>
 *   <li>armor_toughness — +1.0 add_value</li>
 *   <li>knockback_resistance — +0.05 add_value</li>
 * </ul>
 *
 * <p>All enabled entries carry a creeper <em>blacklist</em> condition
 * (expressed via {@link EntityFilter.EntityTypeFilter} with {@code whitelist=false})
 * so the creeper's already-large health pool is not multiplied further.</p>
 *
 * <p>Enabled entries express their entity restrictions through the
 * {@link EntityFilter} system used throughout the new project (mob category,
 * entity type, entity tag, mod id, attribute filters, and composition).</p>
 */
public class AttributesModifierDataProvider implements DataProvider {

    private final PackOutput packOutput;
    private final CompletableFuture<HolderLookup.Provider> lookupProvider;

    /** Creeper blacklist — all champions are scaled except creepers. */
    private static final ChampionModifierCondition CREEPER_BLACKLIST =
            new ChampionModifierCondition(
                    new EntityFilter.EntityTypeFilter(
                            Set.of(new ResourceLocation("minecraft:creeper")),
                            false  // whitelist=false → blacklist
                    ),
                    Optional.empty(),
                    Optional.empty()
            );

    public AttributesModifierDataProvider(
            PackOutput packOutput,
            CompletableFuture<HolderLookup.Provider> lookupProvider
    ) {
        this.packOutput = packOutput;
        this.lookupProvider = lookupProvider;
    }

    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput cache) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        BuiltInRegistries.ATTRIBUTE.asLookup().listElements().forEach(attributeRef -> {
            ResourceLocation attrId = attributeRef.key().location();
            ModifierSetting setting = buildSetting(attributeRef.value(), attrId);

            Path outputPath = packOutput.getOutputFolder()
                    .resolve("data")
                    .resolve(attrId.getNamespace())
                    .resolve(AttributesModifierDataLoader.getFolder())
                    .resolve(attrId.getPath() + ".json");

            futures.add(lookupProvider.thenCompose(provider ->
                    encodeAndSave(cache, setting, outputPath)));
        });

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    /** 1.20.1 DataProvider has no codec-aware saveStable — encode with JsonOps first. */
    private static CompletableFuture<?> encodeAndSave(
            CachedOutput cache, ModifierSetting setting, Path path) {
        return CompletableFuture.supplyAsync(
                        () -> ModifierSetting.MAP_CODEC.codec()
                                .encodeStart(com.mojang.serialization.JsonOps.INSTANCE, setting)
                                .getOrThrow(false, s -> {}),
                        net.minecraft.Util.backgroundExecutor())
                .thenCompose(json -> DataProvider.saveStable(cache, json, path));
    }

    @Override
    public @NotNull String getName() {
        return "Champions Modifier Settings";
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private static ModifierSetting buildSetting(
            Attribute attribute,
            ResourceLocation attrId
    ) {
        if (attribute == Attributes.MAX_HEALTH) {
            return enabled(attrId, 0.35, AttributeModifier.Operation.MULTIPLY_TOTAL);
        } else if (attribute == Attributes.ATTACK_DAMAGE) {
            return enabled(attrId, 0.50, AttributeModifier.Operation.MULTIPLY_TOTAL);
        } else if (attribute == Attributes.ARMOR) {
            return enabled(attrId, 2.0, AttributeModifier.Operation.ADDITION);
        } else if (attribute == Attributes.ARMOR_TOUGHNESS) {
            return enabled(attrId, 1.0, AttributeModifier.Operation.ADDITION);
        } else if (attribute == Attributes.KNOCKBACK_RESISTANCE) {
            return enabled(attrId, 0.05, AttributeModifier.Operation.ADDITION);
        } else {
            return new ModifierSetting(attrId, false,
                    Pair.of(0.0, AttributeModifier.Operation.ADDITION),
                    Optional.empty());
        }
    }

    /** Create an enabled setting with the creeper blacklist condition. */
    private static ModifierSetting enabled(
            ResourceLocation attrId,
            double value,
            AttributeModifier.Operation operation
    ) {
        return new ModifierSetting(
                attrId,
                true,
                Pair.of(value, operation),
                Optional.of(CREEPER_BLACKLIST)
        );
    }
}
