package top.theillusivec4.champions.neoforge.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import top.theillusivec4.champions.common.datagen.*;
import top.theillusivec4.champions.common.filter.EntityFilter;
import top.theillusivec4.champions.neoforge.ChampionsNeoForge;

import java.util.Set;

/**
 * Wires all Champions data generators into the NeoForge data generation pipeline.
 *
 * <p>Run with {@code ./gradlew :neoforge:runData}.</p>
 *
 * <p>Providers registered here:</p>
 * <ul>
 *   <li>{@link TierProvider} — {@code data/champions/champions/tier/*.json}</li>
 *   <li>{@link ArchetypeProvider} — {@code data/champions/champions/archetype/*.json}</li>
 *   <li>{@link AttributesModifierDataProvider} — {@code data/<namespace>/modifier_setting/*.json}</li>
 *   <li>{@link ChampionLanguageProvider} — {@code assets/champions/lang/en_us.json} (+ zh_cn)</li>
 * </ul>
 */
@EventBusSubscriber(modid = ChampionsNeoForge.MOD_ID)
public final class ChampionsDatagen {

    private ChampionsDatagen() {}

    @SubscribeEvent
    public static void onGatherData(GatherDataEvent event) {
        var generator = event.getGenerator();
        var packOutput = generator.getPackOutput();
        boolean includeServer  = event.includeServer();
        boolean includeClient  = event.includeClient();
        // ── Server data ───────────────────────────────────────────────────────

        generator.addProvider(includeServer,
                new TierProvider(packOutput).addDefaultTiers());
        generator.addProvider(includeServer,
                buildArchetypes(packOutput));
        generator.addProvider(includeServer,
                new DamageTypeProvider(packOutput, event.getLookupProvider()));
        generator.addProvider(includeServer,
                new AttributesModifierDataProvider(packOutput, event.getLookupProvider()));

        // ── Client assets ─────────────────────────────────────────────────────

        generator.addProvider(includeClient, buildLanguage(packOutput, "en_us"));
        generator.addProvider(includeClient, buildLanguage(packOutput, "zh_cn"));
        // add more translate to data generation
        generator.addProvider(includeClient, buildLanguage(packOutput, "ko_kr"));
        generator.addProvider(includeClient, buildLanguage(packOutput, "ru_ru"));
        generator.addProvider(includeClient, buildLanguage(packOutput, "tr_tr"));
        generator.addProvider(includeClient, buildLanguage(packOutput, "uk_ua"));
        generator.addProvider(includeClient, buildLanguage(packOutput, "pt_br"));
    }

    // ── Archetypes ────────────────────────────────────────────────────────────

    private static ArchetypeProvider buildArchetypes(PackOutput output) {
        ArchetypeProvider p = new ArchetypeProvider(output);

        // default_monster — general-purpose archetype for all monster categories
        p.archetype("default_monster")
            .weight(10)
            .tierRange(1, 5)
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
                .candidate("champions:knocking",    10, 2, 3)
                .candidate("champions:dampening",   10, 2, 3)
                .candidate("champions:shielding",    8, 2, 3)
                .candidate("champions:lively",      10, 2, 3)
                .candidate("champions:adaptable",    8, 2, 3)
                .candidate("champions:reflective",   6, 2, 3)
                .candidate("champions:molten",       6, 2, 3)
                .candidate("champions:arctic",       6, 2, 3)
                .candidate("champions:enkindling",   6, 2, 3)
                .candidate("champions:plagued",      6, 2, 3)
                .candidate("champions:infested",     4, 2, 3)
                .candidate("champions:desecrating",  4, 2, 3)
                .count(2, 3))
            .build();

        // undead_warrior — undead-specific archetype
        p.archetype("undead_warrior")
            .weight(8)
            .tierRange(1, 5)
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
            .weight(5)
            .tierRange(2, 5)
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

    // ── Language ──────────────────────────────────────────────────────────────

    private static ChampionLanguageProvider buildLanguage(PackOutput output, String locale) {
        return new NeoForgeLanguageProvider(output, locale);
    }
}
