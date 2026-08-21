package top.theillusivec4.champions.common.loot;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
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

    /** Codec shared by both platforms — registered once per platform registry call. */
    public static final MapCodec<ChampionPropertyCondition> CODEC =
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
    public @NotNull Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(target.getParam());
    }

    @Override
    public boolean test(LootContext context) {
        Entity entity = context.getParamOrNull(target.getParam());
        if (entity == null) return false;
        return matches(entity);
    }

    @Override
    public @NotNull LootItemConditionType getType() {
        return ChampionLootConditions.CHAMPION_PROPERTIES;
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private boolean matches(Entity entity) {
        if (!(entity instanceof LivingEntity living)) return false;
        return ChampionsApi.get().getChampion(living).map(champion -> {
            if (tier.isEmpty()) return true; // no tier filter — any champion passes
            return tier.get().matches(champion.tier().level());
        }).orElse(false);
    }
}
