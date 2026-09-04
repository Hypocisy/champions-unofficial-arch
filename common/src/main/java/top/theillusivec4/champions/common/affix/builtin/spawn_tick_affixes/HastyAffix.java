package top.theillusivec4.champions.common.affix.builtin.spawn_tick_affixes;
import top.theillusivec4.champions.common.utils.Utils;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import top.theillusivec4.champions.api.affix.AffixType;
import top.theillusivec4.champions.api.affix.EmptyAffixData;
import top.theillusivec4.champions.api.affix.handler.HandlerRegistry;
import top.theillusivec4.champions.api.affix.handler.event.SpawnEvent;
import top.theillusivec4.champions.api.affix.handler.event.TickEvent;
import top.theillusivec4.champions.common.config.ChampionsConfig;

import java.util.UUID;

// ── HastyAffix ────────────────────────────────────────────────────────────────

/**
 * Permanently boosts movement speed. Strength multiplies the bonus.
 */
public final class HastyAffix extends AffixType<EmptyAffixData> {

    /** 1.20.1 modifiers are keyed by UUID — derive a stable one from the logical id. */
    private static final UUID MODIFIER_UUID =
            UUID.nameUUIDFromBytes(Utils.key("hasty_speed").toString().getBytes());
    private static final String MODIFIER_NAME = "champions:hasty_speed";

    @Override
    public void registerHandlers(HandlerRegistry<EmptyAffixData> registry) {

        registry.on(SpawnEvent.class, (champion, data, strength, evt) ->
                applySpeed(champion.entity(), strength)
        );

        // Re-apply every 20 ticks in case the modifier was stripped by another effect
        registry.on(TickEvent.class, (champion, data, strength, evt) -> {
            if (!evt.every(20)) return;
            applySpeed(champion.entity(), strength);
        });
    }

    private static void applySpeed(LivingEntity entity, int strength) {
        var attr = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attr == null) return;
        if (attr.getModifier(MODIFIER_UUID) != null) return; // already applied
        // ADDITION matches original: flat bonus on top of base speed,
        // consistent across all mob types regardless of their base speed value.
        attr.addTransientModifier(new AttributeModifier(
                MODIFIER_UUID,
                MODIFIER_NAME,
                ChampionsConfig.hastySpeedBonus * strength,
                AttributeModifier.Operation.ADDITION
        ));
    }
}