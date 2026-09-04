package top.theillusivec4.champions.common.loot;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
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

    // ── LootItemCondition ──────────────────────────────────────────────────────

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(target.getParam());
    }

    @Override
    public boolean test(LootContext context) {
        Entity entity = context.getParamOrNull(target.getParam());
        if (entity == null) return false;
        return matches(entity);
    }

    @Override
    public LootItemConditionType getType() {
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

    /** 1.20.1-style JSON serializer handed to {@link LootItemConditionType} by both platforms. */
    public static class ChampionConditionSerializer implements Serializer<ChampionPropertyCondition> {

        @Override
        public void serialize(JsonObject json, ChampionPropertyCondition value, JsonSerializationContext context) {
            json.add("entity", context.serialize(value.target()));
            value.tier().ifPresent(tier -> json.add("tier", tier.serializeToJson()));
        }

        @Override
        public ChampionPropertyCondition deserialize(JsonObject json, JsonDeserializationContext context) {
            JsonElement tierElement = json.get("tier");
            Optional<MinMaxBounds.Ints> tier = tierElement == null || tierElement.isJsonNull()
                    ? Optional.empty()
                    : Optional.of(MinMaxBounds.Ints.fromJson(tierElement));
            return new ChampionPropertyCondition(
                    GsonHelper.getAsObject(json, "entity", context, LootContext.EntityTarget.class),
                    tier);
        }
    }
}
