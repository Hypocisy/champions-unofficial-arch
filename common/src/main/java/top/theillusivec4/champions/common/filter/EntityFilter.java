package top.theillusivec4.champions.common.filter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;
import java.util.Set;

/**
 * Composable filter that decides whether a {@link LivingEntity} can match an archetype.
 *
 * <p>Implementations are registered in a Codec dispatch map so they can be expressed
 * directly in datapack JSON:</p>
 *
 * <pre>{@code
 * // Match all undead monsters except zombie_villager
 * {
 *   "type": "all_of",
 *   "filters": [
 *     { "type": "entity_tag",  "tag": "minecraft:undead", "whitelist": true },
 *     { "type": "entity_type", "types": ["minecraft:zombie_villager"], "whitelist": false }
 *   ]
 * }
 * }</pre>
 */
public interface EntityFilter {

    boolean matches(LivingEntity entity);

    /**
     * Matches every entity. Used as the default when no filter is specified.
     */
    EntityFilter ANY = entity -> true;

    // ── Codec dispatch ────────────────────────────────────────────────────────

    Codec<EntityFilter> CODEC = Codec.STRING.dispatch(
            EntityFilter::filterTypeKey,
            type -> EntityFilter.codecFor(type).codec()
    );

    private static String filterTypeKey(EntityFilter filter) {
        if (filter == ANY) return "any";
        if (filter instanceof AllOfFilter) return "all_of";
        if (filter instanceof AnyOfFilter) return "any_of";
        if (filter instanceof EntityTypeFilter) return "entity_type";
        if (filter instanceof EntityTagFilter) return "entity_tag";
        if (filter instanceof ModIdFilter) return "mod_id";
        if (filter instanceof MobCategoryFilter) return "mob_category";
        if (filter instanceof AttributeFilter) return "attribute";
        throw new IllegalArgumentException("Unknown EntityFilter type: " + filter.getClass());
    }

    private static MapCodec<? extends EntityFilter> codecFor(String type) {
        return switch (type) {
            case "any" -> MapCodec.unit(ANY);
            case "all_of" -> AllOfFilter.CODEC;
            case "any_of" -> AnyOfFilter.CODEC;
            case "entity_type" -> EntityTypeFilter.CODEC;
            case "entity_tag" -> EntityTagFilter.CODEC;
            case "mod_id" -> ModIdFilter.CODEC;
            case "mob_category" -> MobCategoryFilter.CODEC;
            case "attribute" -> AttributeFilter.CODEC;
            default -> throw new IllegalArgumentException("Unknown entity filter type: " + type);
        };
    }

    // ── Built-in implementations ──────────────────────────────────────────────

    record AllOfFilter(List<EntityFilter> filters) implements EntityFilter {
        public static final MapCodec<AllOfFilter> CODEC =
                EntityFilter.CODEC.listOf().fieldOf("filters")
                        .xmap(AllOfFilter::new, AllOfFilter::filters);

        @Override
        public boolean matches(LivingEntity entity) {
            return filters.stream().allMatch(f -> f.matches(entity));
        }
    }

    record AnyOfFilter(List<EntityFilter> filters) implements EntityFilter {
        public static final MapCodec<AnyOfFilter> CODEC =
                EntityFilter.CODEC.listOf().fieldOf("filters")
                        .xmap(AnyOfFilter::new, AnyOfFilter::filters);

        @Override
        public boolean matches(LivingEntity entity) {
            return filters.stream().anyMatch(f -> f.matches(entity));
        }
    }

    record EntityTypeFilter(Set<ResourceLocation> typeIds, boolean whitelist) implements EntityFilter {
        public static final MapCodec<EntityTypeFilter> CODEC =
                RecordCodecBuilder.mapCodec(inst -> inst.group(
                        ResourceLocation.CODEC.listOf()
                                .xmap(Set::copyOf, List::copyOf)
                                .fieldOf("types").forGetter(EntityTypeFilter::typeIds),
                        Codec.BOOL.optionalFieldOf("whitelist", true).forGetter(EntityTypeFilter::whitelist)
                ).apply(inst, EntityTypeFilter::new));

        @Override
        public boolean matches(LivingEntity entity) {
            ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
            return whitelist == typeIds.contains(id);
        }
    }

    record EntityTagFilter(ResourceLocation tagId, boolean whitelist) implements EntityFilter {
        public static final MapCodec<EntityTagFilter> CODEC =
                RecordCodecBuilder.mapCodec(inst -> inst.group(
                        ResourceLocation.CODEC.fieldOf("tag").forGetter(EntityTagFilter::tagId),
                        Codec.BOOL.optionalFieldOf("whitelist", true).forGetter(EntityTagFilter::whitelist)
                ).apply(inst, EntityTagFilter::new));

        @Override
        public boolean matches(LivingEntity entity) {
            TagKey<EntityType<?>> tag = TagKey.create(
                    Registries.ENTITY_TYPE, tagId);
            return whitelist == entity.getType().is(tag);
        }
    }

    record ModIdFilter(Set<String> modIds, boolean whitelist) implements EntityFilter {
        public static final MapCodec<ModIdFilter> CODEC =
                RecordCodecBuilder.mapCodec(inst -> inst.group(
                        Codec.STRING.listOf()
                                .xmap(Set::copyOf, List::copyOf)
                                .fieldOf("mod_ids").forGetter(ModIdFilter::modIds),
                        Codec.BOOL.optionalFieldOf("whitelist", true).forGetter(ModIdFilter::whitelist)
                ).apply(inst, ModIdFilter::new));

        @Override
        public boolean matches(LivingEntity entity) {
            String namespace = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getNamespace();
            return whitelist == modIds.contains(namespace);
        }
    }

    record MobCategoryFilter(Set<String> categories) implements EntityFilter {
        public static final MapCodec<MobCategoryFilter> CODEC =
                Codec.STRING.listOf()
                        .xmap(l -> new MobCategoryFilter(Set.copyOf(l)), f -> List.copyOf(f.categories))
                        .fieldOf("categories");

        @Override
        public boolean matches(LivingEntity entity) {
            String cat = entity.getType().getCategory().getName();
            return categories.contains(cat);
        }
    }

    record AttributeFilter(ResourceLocation attribute, double min, double max) implements EntityFilter {
        public static final MapCodec<AttributeFilter> CODEC =
                RecordCodecBuilder.mapCodec(inst -> inst.group(
                        ResourceLocation.CODEC.fieldOf("attribute").forGetter(AttributeFilter::attribute),
                        Codec.DOUBLE.optionalFieldOf("min", 0.0).forGetter(AttributeFilter::min),
                        Codec.DOUBLE.optionalFieldOf("max", Double.MAX_VALUE).forGetter(AttributeFilter::max)
                ).apply(inst, AttributeFilter::new));

        @Override
        public boolean matches(LivingEntity entity) {
            var attr = BuiltInRegistries.ATTRIBUTE.getHolder(
                    ResourceKey.create(BuiltInRegistries.ATTRIBUTE.key(), attribute));
            if (attr.isEmpty()) return false;
            var inst = entity.getAttribute(attr.get().value());
            if (inst == null) return false;
            double val = inst.getValue();
            return val >= min && val <= max;
        }
    }
}
