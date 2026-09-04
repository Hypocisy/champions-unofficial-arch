package top.theillusivec4.champions.common.phase;
import top.theillusivec4.champions.common.utils.Utils;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import top.theillusivec4.champions.api.ChampionsApi;
import top.theillusivec4.champions.api.affix.AffixInstance;
import top.theillusivec4.champions.api.champion.Champion;
import top.theillusivec4.champions.common.champion.ChampionView;

import java.util.UUID;

/**
 * Effect applied when a {@link ChampionPhase} triggers.
 *
 * <p>Each effect must support three paths:</p>
 * <ul>
 *   <li>{@link #apply} — normal trigger, called live during combat</li>
 *   <li>{@link #restoreQuietly} — silent restore after loading from disk,
 *       no side effects (no goal setup, no sync)</li>
 *   <li>{@link #remove} — phase teardown (only relevant for non-repeatable phases
 *       that are somehow reversed, or on champion death cleanup)</li>
 * </ul>
 */
public interface PhaseEffect {

    void apply(Champion.Server champion);

    void restoreQuietly(Champion.Server champion);

    void remove(Champion.Server champion);

    // ── Codec dispatch ────────────────────────────────────────────────────────

    Codec<PhaseEffect> CODEC = Codec.STRING.dispatch(
            PhaseEffect::typeKey,
            type -> PhaseEffect.codecFor(type).codec()
    );

    private static String typeKey(PhaseEffect effect) {
        if (effect instanceof AddAffix) return "add_affix";
        if (effect instanceof AddAttribute) return "add_attribute";
        if (effect instanceof AddMobEffect) return "add_mob_effect";
        throw new IllegalArgumentException("Unknown PhaseEffect: " + effect.getClass());
    }

    private static MapCodec<? extends PhaseEffect> codecFor(String type) {
        return switch (type) {
            case "add_affix" -> AddAffix.CODEC;
            case "add_attribute" -> AddAttribute.CODEC;
            case "add_mob_effect" -> AddMobEffect.CODEC;
            default -> throw new IllegalArgumentException("Unknown phase effect type: " + type);
        };
    }

    // ── Built-in implementations ──────────────────────────────────────────────

    /**
     * Add an affix instance to the champion when the phase triggers.
     *
     * <pre>{@code { "type": "add_affix", "affix": "champions:enkindling", "strength": 3 } }</pre>
     */
    record AddAffix(ResourceLocation affixId, int strength) implements PhaseEffect {

        public static final MapCodec<AddAffix> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                ResourceLocation.CODEC.fieldOf("affix").forGetter(AddAffix::affixId),
                Codec.INT.optionalFieldOf("strength", 1).forGetter(AddAffix::strength)
        ).apply(inst, AddAffix::new));

        @Override
        public void apply(Champion.Server champion) {
            ChampionsApi.get().getAffixType(affixId).ifPresent(type -> {
                // Idempotent guard: repeatable phases re-evaluate every 10 ticks while
                // their condition holds. Without this the affix would stack unboundedly
                // (growing the live list, goal list and sync cost each cycle).
                if (champion.hasAffix(type)) return;
                // addAffix triggers goal setup + sync automatically
                champion.addAffix(new AffixInstance(type, strength));
            });
        }

        @Override
        public void restoreQuietly(Champion.Server champion) {
            // Directly add to live list without goal setup or sync —
            // entity hasn't joined the world yet at this point
            ChampionsApi.get().getAffixType(affixId).ifPresent(type -> {
                if (champion instanceof ChampionView.Server server) {
                    // Idempotent guard so repeated view reconstruction never stacks
                    // the same phase affix onto the live list.
                    if (champion.hasAffix(type)) return;
                    server.addAffixSilently(new AffixInstance(type, strength));
                }
            });
        }

        @Override
        public void remove(Champion.Server champion) {
            ChampionsApi.get().getAffixType(affixId)
                    .ifPresent(champion::removeAffixByType);
        }
    }

    /**
     * Apply a flat attribute modifier to the champion.
     *
     * <pre>{@code
     * {
     *   "type": "add_attribute",
     *   "attribute": "minecraft:generic.movement_speed",
     *   "amount": 0.3,
     *   "operation": "add_value"
     * }
     * }</pre>
     */
    record AddAttribute(
            ResourceLocation attribute,
            double amount,
            String operation
    ) implements PhaseEffect {

        // 1.20.1 keys modifiers by UUID — derive a stable one from the logical name
        private static final String MODIFIER_NAME = Utils.key("phase_modifier").toString();
        private static final UUID MODIFIER_UUID = UUID.nameUUIDFromBytes(MODIFIER_NAME.getBytes());

        public static final MapCodec<AddAttribute> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                ResourceLocation.CODEC.fieldOf("attribute").forGetter(AddAttribute::attribute),
                Codec.DOUBLE.fieldOf("amount").forGetter(AddAttribute::amount),
                Codec.STRING.optionalFieldOf("operation", "add_value").forGetter(AddAttribute::operation)
        ).apply(inst, AddAttribute::new));

        @Override
        public void apply(Champion.Server champion) {
            applyModifier(champion);
        }

        @Override
        public void restoreQuietly(Champion.Server champion) {
            applyModifier(champion);
        }

        @Override
        public void remove(Champion.Server champion) {
            var attr = BuiltInRegistries.ATTRIBUTE.getHolder(
                    ResourceKey.create(BuiltInRegistries.ATTRIBUTE.key(), attribute));
            if (attr.isEmpty()) return;
            var instance = champion.entity().getAttribute(attr.get().value());
            if (instance != null) instance.removeModifier(MODIFIER_UUID);
        }

        private void applyModifier(Champion.Server champion) {
            var attr = BuiltInRegistries.ATTRIBUTE.getHolder(
                    ResourceKey.create(BuiltInRegistries.ATTRIBUTE.key(), attribute));
            if (attr.isEmpty()) return;
            var instance = champion.entity().getAttribute(attr.get().value());
            if (instance == null) return;
            AttributeModifier.Operation op = switch (operation) {
                case "add_multiplied_base" -> AttributeModifier.Operation.MULTIPLY_BASE;
                case "add_multiplied_total" -> AttributeModifier.Operation.MULTIPLY_TOTAL;
                default -> AttributeModifier.Operation.ADDITION;
            };
            // addOrReplace: phase restore can run on every server-view reconstruction,
            // so a plain addPermanentModifier would throw on the second call for the
            // same (attribute, modifier id) pair. 1.20.1 has no addOrReplace — remove first.
            AttributeModifier modifier = new AttributeModifier(MODIFIER_UUID, MODIFIER_NAME, amount, op);
            instance.removePermanentModifier(modifier.getId());
            instance.addPermanentModifier(modifier);
        }
    }

    /**
     * Apply a mob effect to the champion (optionally infinite).
     *
     * <pre>{@code
     * { "type": "add_mob_effect", "effect": "minecraft:speed", "amplifier": 1, "infinite": true }
     * }</pre>
     */
    record AddMobEffect(
            ResourceLocation effectId,
            int amplifier,
            boolean infinite,
            int durationTicks
    ) implements PhaseEffect {

        public static final MapCodec<AddMobEffect> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                ResourceLocation.CODEC.fieldOf("effect").forGetter(AddMobEffect::effectId),
                Codec.INT.optionalFieldOf("amplifier", 0).forGetter(AddMobEffect::amplifier),
                Codec.BOOL.optionalFieldOf("infinite", true).forGetter(AddMobEffect::infinite),
                Codec.INT.optionalFieldOf("duration_ticks", 200).forGetter(AddMobEffect::durationTicks)
        ).apply(inst, AddMobEffect::new));

        @Override
        public void apply(Champion.Server champion) {
            applyEffect(champion);
        }

        @Override
        public void restoreQuietly(Champion.Server champion) {
            applyEffect(champion);
        }

        @Override
        public void remove(Champion.Server champion) {
            BuiltInRegistries.MOB_EFFECT.getHolder(
                    ResourceKey.create(BuiltInRegistries.MOB_EFFECT.key(), effectId))
                    .ifPresent(e -> champion.entity().removeEffect(e.value()));
        }

        private void applyEffect(Champion.Server champion) {
            var effectHolder = BuiltInRegistries.MOB_EFFECT.getHolder(
                    ResourceKey.create(BuiltInRegistries.MOB_EFFECT.key(), effectId));
            if (effectHolder.isEmpty()) return;
            int duration = infinite ? MobEffectInstance.INFINITE_DURATION : durationTicks;
            champion.entity().addEffect(
                    new MobEffectInstance(effectHolder.get().value(), duration, amplifier, false, true)
            );
        }
    }
}
