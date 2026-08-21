package top.theillusivec4.champions.fabric.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.resources.ResourceLocation;
import top.theillusivec4.champions.common.datagen.ArchetypeProvider;
import top.theillusivec4.champions.common.datagen.ChampionLanguageProvider;
import top.theillusivec4.champions.common.datagen.DamageTypeProvider;
import top.theillusivec4.champions.common.datagen.TierProvider;
import top.theillusivec4.champions.common.filter.EntityFilter;

import java.util.Set;

/**
 * Fabric datagen entrypoint for the Champions mod.
 *
 * <p>Run with {@code ./gradlew :fabric:runDatagen}.</p>
 *
 * <p>Registered in {@code fabric.mod.json} under {@code "fabric-datagen"}.</p>
 *
 * <p>Language translations are fully handled by the common
 * {@link ChampionLanguageProvider} base class — all locales are supported.</p>
 */
public final class ChampionsFabricDatagen implements DataGeneratorEntrypoint {

    private static final String[] LOCALES = {
            "en_us", "zh_cn", "ko_kr", "ru_ru", "tr_tr", "uk_ua", "pt_br"
    };

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        FabricDataGenerator.Pack pack = generator.createPack();

        // ── Server data ───────────────────────────────────────────────────────
        pack.addProvider((output, registries) ->
                new TierProvider(output).addDefaultTiers());

        pack.addProvider((output, registries) ->
                buildArchetypes(new ArchetypeProvider(output)));

        pack.addProvider(DamageTypeProvider::new);

        // ── Client assets — all locales ───────────────────────────────────────
        for (String locale : LOCALES) {
            final String loc = locale;
            pack.addProvider((output, registries) ->
                    new ChampionLanguageProvider(output, loc));
        }
    }

    // ── Archetypes ────────────────────────────────────────────────────────────

    private static ArchetypeProvider buildArchetypes(ArchetypeProvider p) {
        // default_monster — general-purpose archetype for all monster categories
        p.archetype("default_monster")
            .weight(10).tierRange(1, 5)
            .entityFilter(new EntityFilter.MobCategoryFilter(Set.of("monster")))
            .pool(pool -> pool
                .tierRange(1, 2)
                .candidate("champions:knocking",   12, 1, 1)
                .candidate("champions:dampening",  10, 1, 1)
                .candidate("champions:shielding",   8, 1, 1)
                .candidate("champions:lively",     10, 1, 1)
                .candidate("champions:hasty",      10, 1, 1)
                .candidate("champions:wounding",    8, 1, 1)
                .candidate("champions:paralyzing",  6, 1, 1)
                .count(1, 1))
            .pool(pool -> pool
                .tierRange(3, 4)
                .candidate("champions:knocking",   12, 1, 2)
                .candidate("champions:dampening",  10, 1, 2)
                .candidate("champions:shielding",   8, 1, 2)
                .candidate("champions:lively",     10, 1, 2)
                .candidate("champions:hasty",      10, 1, 2)
                .candidate("champions:wounding",    8, 1, 2)
                .candidate("champions:adaptable",   8, 1, 2)
                .candidate("champions:reflective",  6, 1, 2)
                .candidate("champions:plagued",     6, 1, 2)
                .count(1, 2))
            .pool(pool -> pool
                .tierRange(5, 5)
                .candidate("champions:knocking",   10, 2, 3)
                .candidate("champions:dampening",  10, 2, 3)
                .candidate("champions:shielding",   8, 2, 3)
                .candidate("champions:lively",     10, 2, 3)
                .candidate("champions:adaptable",   8, 2, 3)
                .candidate("champions:reflective",  6, 2, 3)
                .candidate("champions:molten",      6, 2, 3)
                .candidate("champions:arctic",      6, 2, 3)
                .candidate("champions:enkindling",  6, 2, 3)
                .candidate("champions:plagued",     6, 2, 3)
                .candidate("champions:infested",    4, 2, 3)
                .candidate("champions:desecrating", 4, 2, 3)
                .count(2, 3))
            .build();

        // undead_warrior — undead-specific archetype
        p.archetype("undead_warrior")
            .weight(8).tierRange(1, 5)
            .entityFilter(new EntityFilter.EntityTagFilter(
                    ResourceLocation.parse("minecraft:undead"), true))
            .pool(pool -> pool
                .tierRange(1, 5)
                .candidate("champions:wounding",    15, 1, 2)
                .candidate("champions:knocking",    12, 1, 2)
                .candidate("champions:dampening",   10, 1, 2)
                .candidate("champions:plagued",      8, 1, 2)
                .candidate("champions:desecrating",  8, 1, 2)
                .count(1, 2))
            .pool(pool -> pool
                .tierRange(3, 5)
                .candidate("champions:wounding",    15, 2, 4)
                .candidate("champions:reflective",   8, 1, 3)
                .candidate("champions:paralyzing",   8, 1, 3)
                .candidate("champions:desecrating", 10, 2, 3)
                .count(1, 1))
            .build();

        // elemental — elemental archetype with enrage phase
        p.archetype("elemental")
            .weight(5).tierRange(2, 5)
            .entityFilter(new EntityFilter.MobCategoryFilter(Set.of("monster")))
            .pool(pool -> pool
                .tierRange(2, 5)
                .candidate("champions:molten",     10, 1, 3)
                .candidate("champions:enkindling", 10, 1, 3)
                .candidate("champions:arctic",     10, 1, 3)
                .candidate("champions:magnetic",    6, 1, 2)
                .count(1, 1))
            .pool(pool -> pool
                .tierRange(4, 5)
                .candidate("champions:molten",  10, 2, 4)
                .candidate("champions:arctic",  10, 2, 4)
                .count(1, 1))
            .build();

        return p;
    }
}
