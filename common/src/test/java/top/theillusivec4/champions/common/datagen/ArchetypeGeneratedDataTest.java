package top.theillusivec4.champions.common.datagen;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import top.theillusivec4.champions.common.archetype.AffixPool;
import top.theillusivec4.champions.common.archetype.ChampionArchetype;
import top.theillusivec4.champions.common.archetype.TierRange;
import top.theillusivec4.champions.common.archetype.WeightedAffix;
import top.theillusivec4.champions.common.filter.EntityFilter;
import top.theillusivec4.champions.common.phase.ChampionPhase;
import top.theillusivec4.champions.common.phase.PhaseCondition;
import top.theillusivec4.champions.common.phase.PhaseEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Validates the generated {@code data/champions/champions/archetype/*.json} files
 * (produced by {@code :neoforge:runData} / {@code :fabric:runDatagen}) against the
 * intended 8-archetype roster.
 *
 * <p>Checks, for both platforms independently:</p>
 * <ol>
 *   <li>The roster is exactly the 8 intended archetypes — no leftover
 *       {@code default_monster}/{@code undead_warrior}/{@code elemental} files.</li>
 *   <li>Each archetype's entity filter, tier range, affix pools and phases match
 *       the design table (vanilla-specific themed sets + modded catch-all).</li>
 *   <li>Every affix/entity reference in the pools is a registered id.</li>
 *   <li>Each vanilla mob is covered by at most one themed archetype
 *       (specific-first, no double-roll).</li>
 *   <li>The two platforms emit byte-equivalent data (after parse+re-encode).</li>
 * </ol>
 */
class ArchetypeGeneratedDataTest {

    // ── Expected roster ──────────────────────────────────────────────────────

    private static final List<String> EXPECTED_IDS = List.of(
            "zombie_line", "skeleton_line", "creeper_arch", "spider_arch",
            "witch_arch", "endermen_line", "boss_line", "modded_mob");

    /** One record per archetype — mirrors {@code buildArchetypes} in both datagen files. */
    private record Spec(
            String id,
            int weight,
            TierRange tierRange,
            String filterType,
            Set<String> entityRefs,
            boolean filterWhitelist,
            int minPools,
            List<PhaseSpec> phases
    ) {
    }

    private record PhaseSpec(
            String id,
            String conditionType,
            String effectTypes,
            boolean repeatable
    ) {
    }

    // ── Tests ─────────────────────────────────────────────────────────────────

    @Test
    void rosterIsExactlyTheEightIntendedArchetypesForBothPlatforms() throws Exception {
        for (String platform : List.of("neoforge", "fabric")) {
            List<ChampionArchetype> loaded = GeneratedDataTestSupport.loadArchetypes(platform);
            List<String> ids = loaded.stream()
                    .map(a -> a.id().getPath())
                    .sorted()
                    .toList();
            assertEquals(EXPECTED_IDS.stream().sorted().toList(), ids,
                    platform + " roster must be exactly the 8 intended archetypes");
        }
    }

    @Test
    void everyArchetypeMatchesItsDesignSpecForBothPlatforms() throws Exception {
        List<Spec> specs = designSpecs();
        for (String platform : List.of("neoforge", "fabric")) {
            Map<String, ChampionArchetype> byId = GeneratedDataTestSupport.loadArchetypes(platform)
                    .stream()
                    .collect(Collectors.toMap(a -> a.id().getPath(), Function.identity()));
            for (Spec spec : specs) {
                assertArchetypeMatches(platform, byId.get(spec.id()), spec);
            }
        }
    }

    @Test
    void everyAffixAndEntityReferenceIsRegistered() throws Exception {
        // Hard-coded: the built-in affix ids both platforms register (NeoForgeAffixBootstrap
        // and FabricAffixBootStrap). The archetype JSON must never reference anything else.
        Set<String> knownAffixes = Set.of(
                "adaptable", "arctic", "dampening", "desecrating", "enkindling", "hasty",
                "infested", "knocking", "lively", "magnetic", "molten", "paralyzing",
                "plagued", "reflective", "shielding", "wounding");
        Set<String> knownEntities = Set.of(
                // vanilla entity ids referenced by the themed archetypes
                "zombie", "zombie_villager", "husk", "drowned", "zombified_piglin",
                "skeleton", "stray", "bogged", "wither_skeleton", "skeleton_horse",
                "creeper", "spider", "cave_spider", "silverfish", "endermite",
                "witch", "enderman", "phantom", "magma_cube", "elder_guardian",
                "hoglin", "ghast", "zoglin", "blaze", "warden", "slime", "guardian", "ravager");

        for (String platform : List.of("neoforge", "fabric")) {
            for (ChampionArchetype a : GeneratedDataTestSupport.loadArchetypes(platform)) {
                String context = platform + "/" + a.id().getPath();
                for (AffixPool pool : a.affixPools()) {
                    for (WeightedAffix w : pool.candidates()) {
                        assertTrue(knownAffixes.contains(w.affixId().getPath()),
                                context + " references unregistered affix " + w.affixId());
                    }
                    assertTrue(pool.minCount() >= 1 && pool.maxCount() >= pool.minCount(),
                            context + " pool has invalid count range " + pool.minCount() + ".." + pool.maxCount());
                }
                for (ChampionPhase phase : a.phases()) {
                    for (PhaseEffect effect : phase.effects()) {
                        if (effect instanceof PhaseEffect.AddAffix add) {
                            assertTrue(knownAffixes.contains(add.affixId().getPath()),
                                    context + " phase adds unregistered affix " + add.affixId());
                        }
                    }
                }
                // every entity referenced by the filter must be a real vanilla id
                EntityFilter f = a.entityFilter();
                if (f instanceof EntityFilter.EntityTypeFilter etf) {
                    for (ResourceLocation id : etf.typeIds()) {
                        assertTrue(knownEntities.contains(id.getPath()),
                                context + " filters unknown entity " + id);
                    }
                }
            }
        }
    }

    @Test
    void eachVanillaMobBelongsToAtMostOneThemedArchetype() throws Exception {
        for (String platform : List.of("neoforge", "fabric")) {
            // id -> set of entity ids this archetype claims
            Map<String, Set<String>> claims = GeneratedDataTestSupport.loadArchetypes(platform)
                    .stream()
                    .filter(a -> !a.id().getPath().equals("modded_mob"))
                    .collect(Collectors.toMap(
                            a -> a.id().getPath(),
                            a -> a.entityFilter() instanceof EntityFilter.EntityTypeFilter etf
                                    ? etf.typeIds().stream().map(ResourceLocation::getPath).collect(Collectors.toSet())
                                    : Set.of()));
            // union of all claimed vanilla entities
            Set<String> union = claims.values().stream().flatMap(Set::stream).collect(Collectors.toSet());
            for (String entity : union) {
                List<String> owners = claims.entrySet().stream()
                        .filter(e -> e.getValue().contains(entity))
                        .map(Map.Entry::getKey)
                        .toList();
                assertEquals(1, owners.size(),
                        platform + ": entity " + entity + " claimed by " + owners);
            }
        }
    }

    @Test
    void moddedMobArchetypeIsANonMinecraftNamespaceCatchAll() throws Exception {
        for (String platform : List.of("neoforge", "fabric")) {
            ChampionArchetype modded = byId(platform, "modded_mob");
            assertTrue(modded.entityFilter() instanceof EntityFilter.ModIdFilter,
                    platform + ": modded_mob must use ModIdFilter");
            var filter = (EntityFilter.ModIdFilter) modded.entityFilter();
            assertEquals(Set.of("minecraft"), filter.modIds(), platform + ": excludes only minecraft");
            assertFalse(filter.whitelist(), platform + ": must be a whitelist=false catch-all");
            assertEquals(List.of(), modded.phases(),
                    platform + ": modded_mob must have no phases");
        }
    }

    @Test
    void generatedDataIsIdenticalAcrossPlatforms() throws Exception {
        List<ChampionArchetype> neo = GeneratedDataTestSupport.loadArchetypes("neoforge");
        List<ChampionArchetype> fab = GeneratedDataTestSupport.loadArchetypes("fabric");
        Map<String, ChampionArchetype> neoById = neo.stream()
                .collect(Collectors.toMap(a -> a.id().getPath(), Function.identity()));
        Map<String, ChampionArchetype> fabById = fab.stream()
                .collect(Collectors.toMap(a -> a.id().getPath(), Function.identity()));
        assertEquals(neoById.keySet(), fabById.keySet(),
                "neoforge and fabric must generate the same archetype set");

        for (String id : neoById.keySet()) {
            ChampionArchetype n = neoById.get(id);
            ChampionArchetype f = fabById.get(id);
            assertEquals(n, f, "archetype " + id + " differs between neoforge and fabric");
        }
    }

    // ── Per-archetype assertion ───────────────────────────────────────────────

    private void assertArchetypeMatches(String platform, ChampionArchetype a, Spec spec) {
        assertNotNull(a, platform + " is missing archetype " + spec.id());
        String ctx = platform + "/" + spec.id();

        assertEquals(spec.weight(), a.weight(), ctx + " weight");
        assertEquals(spec.tierRange().min(), a.tierRange().min(), ctx + " tierRange.min");
        assertEquals(spec.tierRange().max(), a.tierRange().max(), ctx + " tierRange.max");

        // Filter
        EntityFilter f = a.entityFilter();
        assertEquals(spec.filterType(), filterTypeName(f), ctx + " filter type");
        if (f instanceof EntityFilter.EntityTypeFilter etf) {
            assertEquals(spec.filterWhitelist(), etf.whitelist(), ctx + " filter whitelist");
            assertEquals(spec.entityRefs(),
                    etf.typeIds().stream().map(ResourceLocation::getPath).collect(Collectors.toSet()),
                    ctx + " entity filter refs");
        } else if (f instanceof EntityFilter.ModIdFilter mif) {
            assertEquals(spec.entityRefs(), mif.modIds(), ctx + " mod id filter refs");
            assertEquals(spec.filterWhitelist(), mif.whitelist(), ctx + " mod id filter whitelist");
        }

        // Pools
        assertTrue(a.affixPools().size() >= spec.minPools(), ctx + " needs >= " + spec.minPools() + " pools");

        // Phases
        assertEquals(spec.phases().size(), a.phases().size(), ctx + " phase count");
        for (int i = 0; i < spec.phases().size(); i++) {
            PhaseSpec expected = spec.phases().get(i);
            ChampionPhase actual = a.phases().get(i);
            String phaseCtx = ctx + " phase#" + i;
            assertEquals(expected.id(), actual.id().getPath(), phaseCtx + " id");
            assertEquals(expected.conditionType(), conditionTypeName(actual.condition()), phaseCtx + " condition");
            assertEquals(expected.effectTypes(), effectTypeNames(actual.effects()), phaseCtx + " effects");
            assertEquals(expected.repeatable(), actual.repeatable(), phaseCtx + " repeatable");
        }
    }

    private static ChampionArchetype byId(String platform, String id) throws Exception {
        return GeneratedDataTestSupport.loadArchetypes(platform).stream()
                .filter(a -> a.id().getPath().equals(id))
                .findFirst()
                .orElseThrow(() -> new AssertionError(platform + " missing " + id));
    }

    private static String filterTypeName(EntityFilter f) {
        if (f instanceof EntityFilter.EntityTypeFilter) return "entity_type";
        if (f instanceof EntityFilter.ModIdFilter) return "mod_id";
        return f.getClass().getSimpleName();
    }

    private static String conditionTypeName(PhaseCondition c) {
        if (c instanceof PhaseCondition.HealthPercent) return "health_percent";
        if (c instanceof PhaseCondition.TimeElapsed) return "time_elapsed";
        return c.getClass().getSimpleName();
    }

    private static String effectTypeNames(List<PhaseEffect> effects) {
        return effects.stream()
                .map(e -> {
                    if (e instanceof PhaseEffect.AddAffix) return "add_affix";
                    if (e instanceof PhaseEffect.AddAttribute) return "add_attribute";
                    if (e instanceof PhaseEffect.AddMobEffect) return "add_mob_effect";
                    return e.getClass().getSimpleName();
                })
                .sorted()
                .collect(Collectors.joining(","));
    }

    // ── The intended design, mirrored from both datagen files ────────────────

    private static List<Spec> designSpecs() {
        TierRange t15 = new TierRange(1, 5);
        return List.of(
                new Spec("zombie_line", 10, t15, "entity_type",
                        Set.of("zombie", "zombie_villager", "husk", "drowned", "zombified_piglin"),
                        true, 2,
                        List.of(new PhaseSpec("zombie_line_second_wind", "health_percent",
                                "add_mob_effect", false))),

                new Spec("skeleton_line", 10, t15, "entity_type",
                        Set.of("skeleton", "stray", "bogged", "skeleton_horse"),
                        true, 2,
                        List.of(new PhaseSpec("skeleton_line_archer_rage", "time_elapsed",
                                "add_affix", false))),

                new Spec("creeper_arch", 10, t15, "entity_type",
                        Set.of("creeper"),
                        true, 2,
                        List.of(new PhaseSpec("creeper_arch_detonate", "health_percent",
                                "add_attribute,add_mob_effect", false))),

                new Spec("spider_arch", 10, t15, "entity_type",
                        Set.of("spider", "cave_spider", "silverfish", "endermite"),
                        true, 2,
                        List.of(new PhaseSpec("spider_arch_frenzy", "health_percent",
                                "add_mob_effect", false))),

                new Spec("witch_arch", 10, t15, "entity_type",
                        Set.of("witch"),
                        true, 1,
                        List.of(new PhaseSpec("witch_arch_swift_drink", "health_percent",
                                "add_attribute", false))),

                new Spec("endermen_line", 10, t15, "entity_type",
                        Set.of("enderman"),
                        true, 2,
                        List.of(new PhaseSpec("endermen_line_teleport_rage", "time_elapsed",
                                "add_attribute", false))),

                new Spec("boss_line", 5, t15, "entity_type",
                        Set.of("wither_skeleton", "zoglin", "hoglin", "ravager", "phantom",
                                "slime", "magma_cube", "ghast", "blaze", "warden", "guardian",
                                "elder_guardian"),
                        true, 3,
                        List.of(new PhaseSpec("boss_line_enrage", "health_percent",
                                "add_attribute", false))),

                new Spec("modded_mob", 10, t15, "mod_id",
                        Set.of("minecraft"),
                        false, 3,
                        List.of()));
    }
}
