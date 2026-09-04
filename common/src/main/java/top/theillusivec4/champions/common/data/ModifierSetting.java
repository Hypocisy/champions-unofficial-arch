package top.theillusivec4.champions.common.data;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import top.theillusivec4.champions.common.utils.ChampionsCodecs;

import java.util.Optional;

/**
 * One attribute scaling rule from a {@code modifier_setting} datapack file.
 *
 * <p>Cross-platform port — no NeoForge dependencies.</p>
 *
 * <p>JSON schema:</p>
 * <pre>{@code
 * {
 *   "attributeType": "minecraft:generic.max_health",
 *   "enable": true,
 *   "modifier": { "value": 0.35, "operation": "add_multiplied_total" },
 *   "conditions": {
 *     "entity_filter": {
 *       "type": "entity_type",
 *       "types": ["minecraft:creeper"],
 *       "whitelist": false
 *     },
 *     "tier": { "min": 1 },
 *     "affixes": { "values": [], "matches": {}, "count": {} }
 *   }
 * }
 * }</pre>
 *
 * <p>{@code conditions} is optional. Its {@code entity_filter} uses the same
 * {@link top.theillusivec4.champions.common.filter.EntityFilter} system as archetypes;
 * {@code tier} and {@code affixes} are optional sub-conditions.</p>
 */
public record ModifierSetting(
        ResourceLocation attributeType,
        boolean enable,
        Pair<Double, AttributeModifier.Operation> setting,
        Optional<ChampionModifierCondition> modifierCondition
) {

    public static final MapCodec<ModifierSetting> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ResourceLocation.CODEC
                            .fieldOf("attributeType")
                            .forGetter(ModifierSetting::attributeType),
                    Codec.BOOL
                            .fieldOf("enable")
                            .forGetter(ModifierSetting::enable),
                    RecordCodecBuilder.<Pair<Double, AttributeModifier.Operation>>create(inst ->
                            inst.group(
                                    Codec.DOUBLE.fieldOf("value").forGetter(Pair::getFirst),
                                    ChampionsCodecs.OPERATION_CODEC.fieldOf("operation").forGetter(Pair::getSecond)
                            ).apply(inst, Pair::new)
                    ).fieldOf("modifier").forGetter(ModifierSetting::setting),
                    ChampionModifierCondition.MAP_CODEC.codec()
                            .optionalFieldOf("conditions")
                            .forGetter(ModifierSetting::modifierCondition)
            ).apply(instance, ModifierSetting::new));
}
