package top.theillusivec4.champions.common.loot;


import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.predicates.MinMaxBounds;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.champions.api.ChampionsApi;

import java.util.Optional;
import java.util.Set;

/**
 * Loot condition that passes only when the target entity is a champion,
 * optionally gating on a tier range.
 *
 * <p>JSON format:</p>
 * <pre>{@code
 * {
 *   "condition": "champions:champion_properties",
 *   "entity": "this",
 *   "tier": { "min": 3, "max": 5 }   // optional
 * }
 * }</pre>
 *
 * <p>The {@code "tier"} field is a {@link MinMaxBounds.Ints} — both {@code min}/{@code max}
 * are optional individually (omit the field entirely to allow any tier).</p>
 */
public record ChampionPropertyCondition(
		LootContext.EntityTarget target,
		Optional<MinMaxBounds.Ints> tier
) implements LootItemCondition {

	/**
	 * Codec shared by both platforms — registered once per platform registry call.
	 */
	public static final MapCodec<? extends ChampionPropertyCondition> CODEC =
			RecordCodecBuilder.mapCodec(inst -> inst.group(
					LootContext.EntityTarget.CODEC
							.fieldOf("entity")
							.forGetter(ChampionPropertyCondition::target),
					MinMaxBounds.Ints.CODEC
							.optionalFieldOf("tier")
							.forGetter(ChampionPropertyCondition::tier)
			).apply(inst, ChampionPropertyCondition::new));

	// ── LootItemCondition ──────────────────────────────────────────────────────

	@Override
	public Set<ContextKey<?>> getReferencedContextParams() {
		return ImmutableSet.of(target.contextParam());
	}

	@Override
	public boolean test(LootContext context) {
		Entity entity = context.getOptionalParameter(target.contextParam());
		if (entity == null) return false;
		return matches(entity);
	}

	@Override
	public @NotNull MapCodec<? extends LootItemCondition> codec() {
		return ChampionLootConditions.CHAMPION_PROPERTIES;
	}

	// ── Internal ──────────────────────────────────────────────────────────────

	private boolean matches(Entity entity) {
		if (!(entity instanceof LivingEntity living)) return false;
		return ChampionsApi.get().getChampion(living).map(champion -> {
			// no tier filter — any champion passes
			return tier.map(ints -> ints.matches(champion.tier().level())).orElse(true);
		}).orElse(false);
	}
}
