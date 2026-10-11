package top.theillusivec4.champions.fabric.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import top.theillusivec4.champions.common.datagen.*;
import top.theillusivec4.champions.common.filter.EntityFilter;
import top.theillusivec4.champions.common.phase.ChampionPhase;
import top.theillusivec4.champions.common.phase.PhaseCondition;
import top.theillusivec4.champions.common.phase.PhaseEffect;
import top.theillusivec4.champions.common.registry.ModDamageTypes;

import java.util.List;
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

        pack.addProvider(FabricDamageTypeProvider::new);
        pack.addProvider(ModDamageTypeTagsProvider::new);
        pack.addProvider(ModEntityTypeTagsProvider::new);
        // ── Client assets — all locales ───────────────────────────────────────
        for (String locale : LOCALES) {
            final String loc = locale;
            pack.addProvider((output, registries) ->
                    new ChampionLanguageProvider(output, loc));
        }
    }

    @Override
    public void buildRegistry(RegistrySetBuilder registryBuilder) {
        registryBuilder.add(
                Registries.DAMAGE_TYPE,
                ModDamageTypes::bootstrap
        );
    }

    // ── Archetypes ────────────────────────────────────────────────────────────

    private static ArchetypeProvider buildArchetypes(ArchetypeProvider p) {
        // ── Vanilla-themed archetypes — each mob belongs to exactly one ────────
        // A specific mob matches at most one archetype below, so there is no
        // double-roll for vanilla mobs. Modded mobs fall through to `modded_mob`.

        // zombie_line — the undead horde, tanky and relentless
        p.archetype("zombie_line")
            .weight(10)
            .tierRange(1, 5)
            .entityFilter(new EntityFilter.EntityTypeFilter(Set.of(
                    Identifier.withDefaultNamespace("zombie"),
                    Identifier.withDefaultNamespace("zombie_villager"),
                    Identifier.withDefaultNamespace("husk"),
                    Identifier.withDefaultNamespace("drowned"),
                    Identifier.withDefaultNamespace("zombified_piglin")), true))
            .pool(pool -> pool
                .tierRange(1, 3)
                .candidate("champions:wounding",    15, 1, 2)
                .candidate("champions:dampening",   10, 1, 2)
                .candidate("champions:lively",      10, 1, 2)
                .candidate("champions:knocking",    12, 1, 2)
                .candidate("champions:paralyzing",   8, 1, 2)
                .count(1, 2))
            .pool(pool -> pool
                .tierRange(4, 5)
                .candidate("champions:wounding",    15, 2, 3)
                .candidate("champions:plagued",     10, 2, 3)
                .candidate("champions:desecrating", 10, 2, 3)
                .candidate("champions:knocking",    12, 2, 3)
                .count(1, 2))
            .phase(new ChampionPhase(
                Identifier.fromNamespaceAndPath("champions", "zombie_line_second_wind"),
                new PhaseCondition.HealthPercent(0.4f),
                List.of(new PhaseEffect.AddMobEffect(
                        Identifier.withDefaultNamespace("strength"), 1, true, 0)),
                false))
            .build();

        // skeleton_line — backline archers that grow deadlier over time
        p.archetype("skeleton_line")
            .weight(10)
            .tierRange(1, 5)
            .entityFilter(new EntityFilter.EntityTypeFilter(Set.of(
                    Identifier.withDefaultNamespace("skeleton"),
                    Identifier.withDefaultNamespace("stray"),
                    Identifier.withDefaultNamespace("bogged"),
                    Identifier.withDefaultNamespace("wither_skeleton"),
                    Identifier.withDefaultNamespace("skeleton_horse")), true))
            .pool(pool -> pool
                .tierRange(1, 3)
                .candidate("champions:knocking",   12, 1, 2)
                .candidate("champions:hasty",      10, 1, 2)
                .candidate("champions:reflective",  8, 1, 2)
                .candidate("champions:shielding",   8, 1, 2)
                .count(1, 2))
            .pool(pool -> pool
                .tierRange(4, 5)
                .candidate("champions:enkindling",  10, 1, 3)
                .candidate("champions:desecrating",  8, 2, 3)
                .candidate("champions:reflective",  10, 2, 3)
                .candidate("champions:shielding",    8, 2, 3)
                .count(1, 2))
            .phase(new ChampionPhase(
                Identifier.fromNamespaceAndPath("champions", "skeleton_line_archer_rage"),
                new PhaseCondition.TimeElapsed(45),
                List.of(new PhaseEffect.AddAffix(
                        Identifier.fromNamespaceAndPath("champions", "enkindling"), 2)),
                false))
            .build();

        // creeper_arch — volatile, shields up and faster as it closes in
        p.archetype("creeper_arch")
            .weight(10)
            .tierRange(1, 5)
            .entityFilter(new EntityFilter.EntityTypeFilter(
                    Set.of(Identifier.withDefaultNamespace("creeper")), true))
            .pool(pool -> pool
                .tierRange(1, 3)
                .candidate("champions:shielding",  12, 1, 2)
                .candidate("champions:hasty",      10, 1, 2)
                .candidate("champions:lively",      8, 1, 2)
                .count(1, 2))
            .pool(pool -> pool
                .tierRange(4, 5)
                .candidate("champions:shielding",   12, 2, 3)
                .candidate("champions:reflective",  10, 2, 3)
                .candidate("champions:plagued",      8, 2, 3)
                .count(1, 2))
            .phase(new ChampionPhase(
                Identifier.fromNamespaceAndPath("champions", "creeper_arch_detonate"),
                new PhaseCondition.HealthPercent(0.35f),
                List.of(
                        new PhaseEffect.AddAttribute(
                                Identifier.withDefaultNamespace("generic.attack_damage"),
                                1.0, "add_multiplied_total"),
                        new PhaseEffect.AddMobEffect(
                                Identifier.withDefaultNamespace("speed"), 1, true, 0)),
                false))
            .build();

        // spider_arch — swarming, venomous, frenzies at low health
        p.archetype("spider_arch")
            .weight(10)
            .tierRange(1, 5)
            .entityFilter(new EntityFilter.EntityTypeFilter(Set.of(
                    Identifier.withDefaultNamespace("spider"),
                    Identifier.withDefaultNamespace("cave_spider"),
                    Identifier.withDefaultNamespace("silverfish"),
                    Identifier.withDefaultNamespace("endermite")), true))
            .pool(pool -> pool
                .tierRange(1, 3)
                .candidate("champions:hasty",      12, 1, 2)
                .candidate("champions:wounding",   12, 1, 2)
                .candidate("champions:knocking",   10, 1, 2)
                .count(1, 2))
            .pool(pool -> pool
                .tierRange(4, 5)
                .candidate("champions:plagued",    12, 2, 3)
                .candidate("champions:infested",   10, 2, 3)
                .candidate("champions:reflective",  8, 2, 3)
                .count(1, 2))
            .phase(new ChampionPhase(
                Identifier.fromNamespaceAndPath("champions", "spider_arch_frenzy"),
                new PhaseCondition.HealthPercent(0.35f),
                List.of(new PhaseEffect.AddMobEffect(
                        Identifier.withDefaultNamespace("speed"), 2, true, 0)),
                false))
            .build();

        // witch_arch — self-buffing alchemist, escapes pressure with speed
        p.archetype("witch_arch")
            .weight(10)
            .tierRange(1, 5)
            .entityFilter(new EntityFilter.EntityTypeFilter(
                    Set.of(Identifier.withDefaultNamespace("witch")), true))
            .pool(pool -> pool
                .tierRange(1, 5)
                .candidate("champions:dampening",  14, 1, 2)
                .candidate("champions:lively",     10, 1, 2)
                .candidate("champions:reflective", 10, 1, 2)
                .candidate("champions:plagued",     8, 1, 2)
                .count(1, 2))
            .phase(new ChampionPhase(
                Identifier.fromNamespaceAndPath("champions", "witch_arch_swift_drink"),
                new PhaseCondition.HealthPercent(0.5f),
                List.of(new PhaseEffect.AddAttribute(
                        Identifier.withDefaultNamespace("generic.movement_speed"),
                        0.30, "add_value")),
                false))
            .build();

        // endermen_line — teleporting, hits harder the longer it lives
        p.archetype("endermen_line")
            .weight(10)
            .tierRange(1, 5)
            .entityFilter(new EntityFilter.EntityTypeFilter(
                    Set.of(Identifier.withDefaultNamespace("enderman")), true))
            .pool(pool -> pool
                .tierRange(1, 3)
                .candidate("champions:knocking",   12, 1, 2)
                .candidate("champions:hasty",      10, 1, 2)
                .candidate("champions:shielding",   8, 1, 2)
                .count(1, 2))
            .pool(pool -> pool
                .tierRange(4, 5)
                .candidate("champions:shielding",  10, 2, 3)
                .candidate("champions:reflective", 10, 2, 3)
                .candidate("champions:knocking",   10, 2, 3)
                .count(1, 2))
            .phase(new ChampionPhase(
                Identifier.fromNamespaceAndPath("champions", "endermen_line_teleport_rage"),
                new PhaseCondition.TimeElapsed(30),
                List.of(new PhaseEffect.AddAttribute(
                        Identifier.withDefaultNamespace("generic.attack_damage"),
                        0.30, "add_multiplied_total")),
                false))
            .build();

        // boss_line — big bruisers, enrage at half health
        p.archetype("boss_line")
            .weight(5)
            .tierRange(1, 5)
            .entityFilter(new EntityFilter.EntityTypeFilter(Set.of(
                    Identifier.withDefaultNamespace("wither_skeleton"),
                    Identifier.withDefaultNamespace("zoglin"),
                    Identifier.withDefaultNamespace("hoglin"),
                    Identifier.withDefaultNamespace("ravager"),
                    Identifier.withDefaultNamespace("phantom"),
                    Identifier.withDefaultNamespace("slime"),
                    Identifier.withDefaultNamespace("magma_cube"),
                    Identifier.withDefaultNamespace("ghast"),
                    Identifier.withDefaultNamespace("blaze"),
                    Identifier.withDefaultNamespace("warden"),
                    Identifier.withDefaultNamespace("guardian"),
                    Identifier.withDefaultNamespace("elder_guardian")), true))
            .pool(pool -> pool
                .tierRange(1, 2)
                .candidate("champions:shielding",   10, 1, 1)
                .candidate("champions:reflective",   8, 1, 1)
                .candidate("champions:desecrating",  8, 1, 1)
                .count(1, 1))
            .pool(pool -> pool
                .tierRange(3, 4)
                .candidate("champions:wounding",     12, 1, 2)
                .candidate("champions:infested",     8, 1, 2)
                .candidate("champions:arctic",       8, 1, 2)
                .candidate("champions:shielding",   10, 1, 2)
                .candidate("champions:desecrating",  8, 1, 2)
                .count(1, 2))
            .pool(pool -> pool
                .tierRange(5, 5)
                .candidate("champions:molten",       8, 2, 3)
                .candidate("champions:paralyzing",   8, 2, 3)
                .candidate("champions:magnetic",      8, 2, 3)
                .candidate("champions:reflective",   10, 2, 3)
                .candidate("champions:shielding",    10, 2, 3)
                .count(2, 2))
            .phase(new ChampionPhase(
                Identifier.fromNamespaceAndPath("champions", "boss_line_enrage"),
                new PhaseCondition.HealthPercent(0.5f),
                List.of(new PhaseEffect.AddAttribute(
                        Identifier.withDefaultNamespace("generic.attack_damage"),
                        0.25, "add_multiplied_total")),
                false))
            .build();

        // ── Non-vanilla catch-all ───────────────────────────────────────────────
        // Any mob whose entity type is not from the "minecraft" namespace. This
        // automatically covers all modded mobs — no per-mod updates needed — and
        // vanilla mobs never reach it because each is covered by a themed archetype.

        p.archetype("modded_mob")
            .weight(10)
            .tierRange(1, 5)
            .entityFilter(new EntityFilter.ModIdFilter(Set.of("minecraft"), false))
            .pool(pool -> pool
                .tierRange(1, 2)
                .candidate("champions:knocking",   12, 1, 1)
                .candidate("champions:dampening",  10, 1, 1)
                .candidate("champions:lively",     10, 1, 1)
                .candidate("champions:hasty",      10, 1, 1)
                .candidate("champions:wounding",    8, 1, 1)
                .candidate("champions:paralyzing",  6, 1, 1)
                .count(1, 1))
            .pool(pool -> pool
                .tierRange(3, 4)
                .candidate("champions:shielding",   8, 1, 2)
                .candidate("champions:adaptable",   8, 1, 2)
                .candidate("champions:reflective",  6, 1, 2)
                .candidate("champions:knocking",   12, 1, 2)
                .candidate("champions:dampening",  10, 1, 2)
                .candidate("champions:lively",     10, 1, 2)
                .candidate("champions:hasty",      10, 1, 2)
                .candidate("champions:wounding",    8, 1, 2)
                .count(1, 2))
            .pool(pool -> pool
                .tierRange(5, 5)
                .candidate("champions:arctic",       6, 2, 3)
                .candidate("champions:enkindling",   6, 2, 3)
                .candidate("champions:molten",       6, 2, 3)
                .candidate("champions:plagued",      6, 2, 3)
                .candidate("champions:shielding",    8, 2, 3)
                .candidate("champions:adaptable",    8, 2, 3)
                .candidate("champions:reflective",   6, 2, 3)
                .candidate("champions:knocking",    10, 2, 3)
                .candidate("champions:dampening",   10, 2, 3)
                .candidate("champions:lively",      10, 2, 3)
                .count(2, 3))
            .build();

        return p;
    }
}
