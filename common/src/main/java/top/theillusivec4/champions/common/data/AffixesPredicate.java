package top.theillusivec4.champions.common.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.resources.ResourceLocation;
import top.theillusivec4.champions.api.ChampionsApi;
import top.theillusivec4.champions.api.affix.AffixInstance;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Predicate for filtering champions by affix criteria.
 *
 * <p>Cross-platform port from the old NeoForge-only version. NeoForgeExtraCodecs.setOf
 * has been replaced with Codec.list(...).xmap(Set::copyOf, List::copyOf) for compatibility.</p>
 */
public record AffixesPredicate(Set<ResourceLocation> values, MinMaxBounds.Ints matches,
                               MinMaxBounds.Ints count) {

    public static final Codec<AffixesPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.list(ResourceLocation.CODEC)
                    .xmap(Set::copyOf, List::copyOf)
                    .fieldOf("values")
                    .forGetter(AffixesPredicate::values),
            MinMaxBounds.Ints.CODEC.fieldOf("matches").forGetter(AffixesPredicate::matches),
            MinMaxBounds.Ints.CODEC.fieldOf("count").forGetter(AffixesPredicate::count)
    ).apply(instance, AffixesPredicate::new));

    /**
     * Test whether the given affix list matches this predicate.
     *
     * @param input the current affix instances from a champion (new API)
     */
    public boolean matches(List<AffixInstance> input) {
        if (this.values.isEmpty()) {
            return this.count.matches(input.size());
        } else {
            // Convert AffixInstance → ResourceLocation via ChampionsApi
            Set<ResourceLocation> affixIds = input.stream()
                    .flatMap(inst -> ChampionsApi.get().getAffixTypeId(inst.type()).stream())
                    .collect(Collectors.toSet());
            long found = values.stream().filter(affixIds::contains).count();
            return this.matches.matches((int) found) && this.count.matches(input.size());
        }
    }
}
