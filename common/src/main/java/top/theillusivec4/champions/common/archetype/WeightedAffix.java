package top.theillusivec4.champions.common.archetype;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

/**
 * A candidate affix entry in an {@link AffixPool}, with weight and strength range.
 *
 * <p>During pool drawing, strength is sampled uniformly in
 * [{@code minStrength}, {@code maxStrength}].</p>
 */
public record WeightedAffix(
        ResourceLocation affixId,
        int weight,
        int minStrength,
        int maxStrength
) {

    public static final Codec<WeightedAffix> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ResourceLocation.CODEC
                    .fieldOf("affix").forGetter(WeightedAffix::affixId),
            Codec.INT
                    .optionalFieldOf("weight", 10).forGetter(WeightedAffix::weight),
            Codec.INT
                    .optionalFieldOf("min_strength", 1).forGetter(WeightedAffix::minStrength),
            Codec.INT
                    .optionalFieldOf("max_strength", 1).forGetter(WeightedAffix::maxStrength)
    ).apply(inst, WeightedAffix::new));
}
