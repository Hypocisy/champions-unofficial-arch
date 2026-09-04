package top.theillusivec4.champions.neoforge.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import top.theillusivec4.champions.common.datagen.*;
import top.theillusivec4.champions.common.filter.EntityFilter;
import top.theillusivec4.champions.common.phase.ChampionPhase;
import top.theillusivec4.champions.common.phase.PhaseCondition;
import top.theillusivec4.champions.common.phase.PhaseEffect;
import top.theillusivec4.champions.neoforge.ChampionsNeoForge;

import java.util.List;
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

	private ChampionsDatagen() {
	}

	@SubscribeEvent
	public static void onGatherData(GatherDataEvent event) {
		var generator = event.getGenerator();
		var packOutput = generator.getPackOutput();
		boolean includeServer = event.includeServer();
		boolean includeClient = event.includeClient();
		var provider = event.getLookupProvider();

		// data
		var datapackProvider = generator.addProvider(event.includeServer(), new ModDatapackProvider(packOutput, provider));
		// ── Server data ───────────────────────────────────────────────────────

		generator.addProvider(includeServer,
				new TierProvider(packOutput).addDefaultTiers());
		generator.addProvider(includeServer,
				buildArchetypes(packOutput));
		generator.addProvider(includeServer,
				new AttributesModifierDataProvider(packOutput, provider));
		generator.addProvider(includeServer,
				new ModDamageTypeTagsProvider(packOutput, datapackProvider.getRegistryProvider()));

		generator.addProvider(includeServer, new ModEntityTypeTagsProvider(packOutput, provider));
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

		// ── Vanilla-themed archetypes — each mob belongs to exactly one ────────
		// A specific mob matches at most one archetype below, so there is no
		// double-roll for vanilla mobs. Modded mobs fall through to `modded_mob`.

		// zombie_line — the undead horde, tanky and relentless
		p.archetype("zombie_line")
				.weight(10)
				.tierRange(1, 5)
				.entityFilter(new EntityFilter.EntityTypeFilter(Set.of(
						ResourceLocation.withDefaultNamespace("zombie"),
						ResourceLocation.withDefaultNamespace("zombie_villager"),
						ResourceLocation.withDefaultNamespace("husk"),
						ResourceLocation.withDefaultNamespace("drowned"),
						ResourceLocation.withDefaultNamespace("zombified_piglin")), true))
				.pool(pool -> pool
						.tierRange(1, 3)
						.candidate("champions:wounding", 15, 1, 2)
						.candidate("champions:dampening", 10, 1, 2)
						.candidate("champions:lively", 10, 1, 2)
						.candidate("champions:knocking", 12, 1, 2)
						.candidate("champions:paralyzing", 8, 1, 2)
						.count(1, 2))
				.pool(pool -> pool
						.tierRange(4, 5)
						.candidate("champions:wounding", 15, 2, 3)
						.candidate("champions:plagued", 10, 2, 3)
						.candidate("champions:desecrating", 10, 2, 3)
						.candidate("champions:knocking", 12, 2, 3)
						.count(1, 2))
				.phase(new ChampionPhase(
						Utils.key("zombie_line_second_wind"),
						new PhaseCondition.HealthPercent(0.4f),
						List.of(new PhaseEffect.AddMobEffect(
								ResourceLocation.withDefaultNamespace("strength"), 1, true, 0)),
						false))
				.build();

		// skeleton_line — backline archers that grow deadlier over time
		p.archetype("skeleton_line")
				.weight(10)
				.tierRange(1, 5)
				.entityFilter(new EntityFilter.EntityTypeFilter(Set.of(
						ResourceLocation.withDefaultNamespace("skeleton"),
						ResourceLocation.withDefaultNamespace("stray"),
						ResourceLocation.withDefaultNamespace("bogged"),
						ResourceLocation.withDefaultNamespace("wither_skeleton"),
						ResourceLocation.withDefaultNamespace("skeleton_horse")), true))
				.pool(pool -> pool
						.tierRange(1, 3)
						.candidate("champions:knocking", 12, 1, 2)
						.candidate("champions:hasty", 10, 1, 2)
						.candidate("champions:reflective", 8, 1, 2)
						.candidate("champions:shielding", 8, 1, 2)
						.count(1, 2))
				.pool(pool -> pool
						.tierRange(4, 5)
						.candidate("champions:enkindling", 10, 1, 3)
						.candidate("champions:desecrating", 8, 2, 3)
						.candidate("champions:reflective", 10, 2, 3)
						.candidate("champions:shielding", 8, 2, 3)
						.count(1, 2))
				.phase(new ChampionPhase(
						Utils.key("skeleton_line_archer_rage"),
						new PhaseCondition.TimeElapsed(45),
						List.of(new PhaseEffect.AddAffix(
								Utils.key("enkindling"), 2)),
						false))
				.build();

		// creeper_arch — volatile, shields up and faster as it closes in
		p.archetype("creeper_arch")
				.weight(10)
				.tierRange(1, 5)
				.entityFilter(new EntityFilter.EntityTypeFilter(
						Set.of(ResourceLocation.withDefaultNamespace("creeper")), true))
				.pool(pool -> pool
						.tierRange(1, 3)
						.candidate("champions:shielding", 12, 1, 2)
						.candidate("champions:hasty", 10, 1, 2)
						.candidate("champions:lively", 8, 1, 2)
						.count(1, 2))
				.pool(pool -> pool
						.tierRange(4, 5)
						.candidate("champions:shielding", 12, 2, 3)
						.candidate("champions:reflective", 10, 2, 3)
						.candidate("champions:plagued", 8, 2, 3)
						.count(1, 2))
				.phase(new ChampionPhase(
						Utils.key("creeper_arch_detonate"),
						new PhaseCondition.HealthPercent(0.35f),
						List.of(
								new PhaseEffect.AddAttribute(
										ResourceLocation.withDefaultNamespace("generic.attack_damage"),
										1.0, "add_multiplied_total"),
								new PhaseEffect.AddMobEffect(
										ResourceLocation.withDefaultNamespace("speed"), 1, true, 0)),
						false))
				.build();

		// spider_arch — swarming, venomous, frenzies at low health
		p.archetype("spider_arch")
				.weight(10)
				.tierRange(1, 5)
				.entityFilter(new EntityFilter.EntityTypeFilter(Set.of(
						ResourceLocation.withDefaultNamespace("spider"),
						ResourceLocation.withDefaultNamespace("cave_spider"),
						ResourceLocation.withDefaultNamespace("silverfish"),
						ResourceLocation.withDefaultNamespace("endermite")), true))
				.pool(pool -> pool
						.tierRange(1, 3)
						.candidate("champions:hasty", 12, 1, 2)
						.candidate("champions:wounding", 12, 1, 2)
						.candidate("champions:knocking", 10, 1, 2)
						.count(1, 2))
				.pool(pool -> pool
						.tierRange(4, 5)
						.candidate("champions:plagued", 12, 2, 3)
						.candidate("champions:infested", 10, 2, 3)
						.candidate("champions:reflective", 8, 2, 3)
						.count(1, 2))
				.phase(new ChampionPhase(
						Utils.key("spider_arch_frenzy"),
						new PhaseCondition.HealthPercent(0.35f),
						List.of(new PhaseEffect.AddMobEffect(
								ResourceLocation.withDefaultNamespace("speed"), 2, true, 0)),
						false))
				.build();

		// witch_arch — self-buffing alchemist, escapes pressure with speed
		p.archetype("witch_arch")
				.weight(10)
				.tierRange(1, 5)
				.entityFilter(new EntityFilter.EntityTypeFilter(
						Set.of(ResourceLocation.withDefaultNamespace("witch")), true))
				.pool(pool -> pool
						.tierRange(1, 5)
						.candidate("champions:dampening", 14, 1, 2)
						.candidate("champions:lively", 10, 1, 2)
						.candidate("champions:reflective", 10, 1, 2)
						.candidate("champions:plagued", 8, 1, 2)
						.count(1, 2))
				.phase(new ChampionPhase(
						Utils.key("witch_arch_swift_drink"),
						new PhaseCondition.HealthPercent(0.5f),
						List.of(new PhaseEffect.AddAttribute(
								ResourceLocation.withDefaultNamespace("generic.movement_speed"),
								0.30, "add_value")),
						false))
				.build();

		// endermen_line — teleporting, hits harder the longer it lives
		p.archetype("endermen_line")
				.weight(10)
				.tierRange(1, 5)
				.entityFilter(new EntityFilter.EntityTypeFilter(
						Set.of(ResourceLocation.withDefaultNamespace("enderman")), true))
				.pool(pool -> pool
						.tierRange(1, 3)
						.candidate("champions:knocking", 12, 1, 2)
						.candidate("champions:hasty", 10, 1, 2)
						.candidate("champions:shielding", 8, 1, 2)
						.count(1, 2))
				.pool(pool -> pool
						.tierRange(4, 5)
						.candidate("champions:shielding", 10, 2, 3)
						.candidate("champions:reflective", 10, 2, 3)
						.candidate("champions:knocking", 10, 2, 3)
						.count(1, 2))
				.phase(new ChampionPhase(
						Utils.key("endermen_line_teleport_rage"),
						new PhaseCondition.TimeElapsed(30),
						List.of(new PhaseEffect.AddAttribute(
								ResourceLocation.withDefaultNamespace("generic.attack_damage"),
								0.30, "add_multiplied_total")),
						false))
				.build();

		// boss_line — big bruisers, enrage at half health
		p.archetype("boss_line")
				.weight(5)
				.tierRange(1, 5)
				.entityFilter(new EntityFilter.EntityTypeFilter(Set.of(
						ResourceLocation.withDefaultNamespace("wither_skeleton"),
						ResourceLocation.withDefaultNamespace("zoglin"),
						ResourceLocation.withDefaultNamespace("hoglin"),
						ResourceLocation.withDefaultNamespace("ravager"),
						ResourceLocation.withDefaultNamespace("phantom"),
						ResourceLocation.withDefaultNamespace("slime"),
						ResourceLocation.withDefaultNamespace("magma_cube"),
						ResourceLocation.withDefaultNamespace("ghast"),
						ResourceLocation.withDefaultNamespace("blaze"),
						ResourceLocation.withDefaultNamespace("warden"),
						ResourceLocation.withDefaultNamespace("guardian"),
						ResourceLocation.withDefaultNamespace("elder_guardian")), true))
				.pool(pool -> pool
						.tierRange(1, 2)
						.candidate("champions:shielding", 10, 1, 1)
						.candidate("champions:reflective", 8, 1, 1)
						.candidate("champions:desecrating", 8, 1, 1)
						.count(1, 1))
				.pool(pool -> pool
						.tierRange(3, 4)
						.candidate("champions:wounding", 12, 1, 2)
						.candidate("champions:infested", 8, 1, 2)
						.candidate("champions:arctic", 8, 1, 2)
						.candidate("champions:shielding", 10, 1, 2)
						.candidate("champions:desecrating", 8, 1, 2)
						.count(1, 2))
				.pool(pool -> pool
						.tierRange(5, 5)
						.candidate("champions:molten", 8, 2, 3)
						.candidate("champions:paralyzing", 8, 2, 3)
						.candidate("champions:magnetic", 8, 2, 3)
						.candidate("champions:reflective", 10, 2, 3)
						.candidate("champions:shielding", 10, 2, 3)
						.count(2, 2))
				.phase(new ChampionPhase(
						Utils.key("boss_line_enrage"),
						new PhaseCondition.HealthPercent(0.5f),
						List.of(new PhaseEffect.AddAttribute(
								ResourceLocation.withDefaultNamespace("generic.attack_damage"),
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
						.candidate("champions:knocking", 12, 1, 1)
						.candidate("champions:dampening", 10, 1, 1)
						.candidate("champions:lively", 10, 1, 1)
						.candidate("champions:hasty", 10, 1, 1)
						.candidate("champions:wounding", 8, 1, 1)
						.candidate("champions:paralyzing", 6, 1, 1)
						.count(1, 1))
				.pool(pool -> pool
						.tierRange(3, 4)
						.candidate("champions:shielding", 8, 1, 2)
						.candidate("champions:adaptable", 8, 1, 2)
						.candidate("champions:reflective", 6, 1, 2)
						.candidate("champions:knocking", 12, 1, 2)
						.candidate("champions:dampening", 10, 1, 2)
						.candidate("champions:lively", 10, 1, 2)
						.candidate("champions:hasty", 10, 1, 2)
						.candidate("champions:wounding", 8, 1, 2)
						.count(1, 2))
				.pool(pool -> pool
						.tierRange(5, 5)
						.candidate("champions:arctic", 6, 2, 3)
						.candidate("champions:enkindling", 6, 2, 3)
						.candidate("champions:molten", 6, 2, 3)
						.candidate("champions:plagued", 6, 2, 3)
						.candidate("champions:shielding", 8, 2, 3)
						.candidate("champions:adaptable", 8, 2, 3)
						.candidate("champions:reflective", 6, 2, 3)
						.candidate("champions:knocking", 10, 2, 3)
						.candidate("champions:dampening", 10, 2, 3)
						.candidate("champions:lively", 10, 2, 3)
						.count(2, 3))
				.build();

		return p;
	}

	// ── Language ──────────────────────────────────────────────────────────────

	private static ChampionLanguageProvider buildLanguage(PackOutput output, String locale) {
		return new NeoForgeLanguageProvider(output, locale);
	}
}
