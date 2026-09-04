package top.theillusivec4.champions.common.utils;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.Optional;
import java.util.function.Function;

/**
 * 1.20.1 stand-ins for codecs that only exist in 1.21+ vanilla.
 * JSON shapes match the formats documented in DATAPACK_GUIDE.md.
 */
public final class ChampionsCodecs {

    private ChampionsCodecs() {}

    /** Serial names used by datapacks: add_value / add_multiplied_base / add_multiplied_total. */
    public static final Codec<AttributeModifier.Operation> OPERATION_CODEC = Codec.STRING
            .comapFlatMap(ChampionsCodecs::parseOperation, ChampionsCodecs::operationId);

    private static DataResult<AttributeModifier.Operation> parseOperation(String id) {
        return switch (id) {
            case "add_value" -> DataResult.success(AttributeModifier.Operation.ADDITION);
            case "add_multiplied_base" -> DataResult.success(AttributeModifier.Operation.MULTIPLY_BASE);
            case "add_multiplied_total" -> DataResult.success(AttributeModifier.Operation.MULTIPLY_TOTAL);
            default -> DataResult.error(() -> "Unknown attribute modifier operation: " + id);
        };
    }

    private static String operationId(AttributeModifier.Operation op) {
        return switch (op) {
            case ADDITION -> "add_value";
            case MULTIPLY_BASE -> "add_multiplied_base";
            case MULTIPLY_TOTAL -> "add_multiplied_total";
        };
    }

    private static final Codec<MinMaxBounds.Ints> BOUNDS_RECORD = RecordCodecBuilder.create(inst -> inst.group(
            Codec.INT.optionalFieldOf("min").forGetter(b -> Optional.ofNullable(b.getMin())),
            Codec.INT.optionalFieldOf("max").forGetter(b -> Optional.ofNullable(b.getMax()))
    ).apply(inst, ChampionsCodecs::boundsFrom));

    private static MinMaxBounds.Ints boundsFrom(Optional<Integer> min, Optional<Integer> max) {
        if (min.isPresent() && max.isPresent()) return MinMaxBounds.Ints.between(min.get(), max.get());
        if (min.isPresent()) return MinMaxBounds.Ints.atLeast(min.get());
        if (max.isPresent()) return MinMaxBounds.Ints.atMost(max.get());
        return MinMaxBounds.Ints.ANY;
    }

    /** Accepts a bare int or {@code {"min":..,"max":..}}; mirrors MinMaxBounds.Ints.CODEC (1.21). */
    public static final Codec<MinMaxBounds.Ints> INTS_BOUNDS =
            Codec.either(Codec.INT, BOUNDS_RECORD).xmap(
                    either -> either.map(MinMaxBounds.Ints::exactly, Function.identity()),
                    bounds -> Either.right(bounds));
}
