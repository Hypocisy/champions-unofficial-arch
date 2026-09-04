package top.theillusivec4.champions.common.datagen;
import top.theillusivec4.champions.common.utils.Utils;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import top.theillusivec4.champions.common.filter.EntityFilter;
import top.theillusivec4.champions.common.phase.ChampionPhase;
import top.theillusivec4.champions.common.phase.PhaseCondition;
import top.theillusivec4.champions.common.phase.PhaseEffect;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Exercises the codec dispatch for the three polymorphic data types that the
 * archetype system deserializes from datapack JSON:
 * {@link EntityFilter}, {@link PhaseCondition}, {@link PhaseEffect}.
 *
 * <p>These are the codecs the game itself uses to load {@code archetype/*.json};
 * verifying them here means the generator output and the runtime loader agree on
 * the same schema. Full archetype deserialization is covered separately by
 * {@link ArchetypeGeneratedDataTest}.</p>
 */
class CodecDispatchTest {

    // ── EntityFilter ──────────────────────────────────────────────────────────

    @Test
    void entityTypeFilterDecodesWithWhitelistDefaultingToTrue() {
        EntityFilter f = decode(
                "{\"type\":\"entity_type\",\"types\":[\"minecraft:zombie\",\"minecraft:husk\"]}",
                EntityFilter.CODEC);
        assertInstanceOf(EntityFilter.EntityTypeFilter.class, f);
        var etf = (EntityFilter.EntityTypeFilter) f;
        assertEquals(Set.of("minecraft:zombie", "minecraft:husk"),
                etf.typeIds().stream().map(ResourceLocation::toString).collect(Collectors.toSet()));
        assertTrue(etf.whitelist());
    }

    @Test
    void entityTypeFilterDecodesWithExplicitWhitelistFalse() {
        EntityFilter f = decode(
                "{\"type\":\"entity_type\",\"types\":[\"minecraft:creeper\"],\"whitelist\":false}",
                EntityFilter.CODEC);
        var etf = (EntityFilter.EntityTypeFilter) f;
        assertFalse(etf.whitelist());
        assertEquals(Set.of("minecraft:creeper"),
                etf.typeIds().stream().map(ResourceLocation::toString).collect(Collectors.toSet()));
    }

    @Test
    void modIdFilterDecodesTheNonMinecraftCatchAllShape() {
        EntityFilter f = decode(
                "{\"type\":\"mod_id\",\"mod_ids\":[\"minecraft\"],\"whitelist\":false}",
                EntityFilter.CODEC);
        var mif = (EntityFilter.ModIdFilter) f;
        assertEquals(Set.of("minecraft"), mif.modIds());
        assertFalse(mif.whitelist());
    }

    @Test
    void allOfAndMobCategoryFiltersDecode() {
        EntityFilter f = decode(
                "{\"type\":\"all_of\",\"filters\":[" +
                        "{\"type\":\"entity_tag\",\"tag\":\"minecraft:undead\",\"whitelist\":true}," +
                        "{\"type\":\"mob_category\",\"categories\":[\"monster\"]}]}",
                EntityFilter.CODEC);
        assertInstanceOf(EntityFilter.AllOfFilter.class, f);
        var all = (EntityFilter.AllOfFilter) f;
        assertEquals(2, all.filters().size());
        assertInstanceOf(EntityFilter.EntityTagFilter.class, all.filters().get(0));
        assertInstanceOf(EntityFilter.MobCategoryFilter.class, all.filters().get(1));
    }

    @Test
    void anyFilterIsTheFallbackShape() {
        EntityFilter f = decode("{\"type\":\"any\"}", EntityFilter.CODEC);
        assertSame(EntityFilter.ANY, f);
    }

    // ── PhaseCondition ────────────────────────────────────────────────────────

    @Test
    void healthPercentConditionDecodes() {
        PhaseCondition c = decode("{\"type\":\"health_percent\",\"below\":0.4}", PhaseCondition.CODEC);
        assertInstanceOf(PhaseCondition.HealthPercent.class, c);
        assertEquals(0.4f, ((PhaseCondition.HealthPercent) c).below(), 0.0001f);
    }

    @Test
    void timeElapsedConditionDecodes() {
        PhaseCondition c = decode("{\"type\":\"time_elapsed\",\"seconds\":45}", PhaseCondition.CODEC);
        assertInstanceOf(PhaseCondition.TimeElapsed.class, c);
        assertEquals(45, ((PhaseCondition.TimeElapsed) c).seconds());
    }

    @Test
    void affixTriggeredConditionDecodes() {
        PhaseCondition c = decode(
                "{\"type\":\"affix_triggered\",\"affix\":\"champions:adaptable\",\"count\":3}",
                PhaseCondition.CODEC);
        assertInstanceOf(PhaseCondition.AffixTriggered.class, c);
        var at = (PhaseCondition.AffixTriggered) c;
        assertEquals(Utils.key("adaptable"), at.affixId());
        assertEquals(3, at.count());
    }

    // ── PhaseEffect ───────────────────────────────────────────────────────────

    @Test
    void addAffixEffectDecodesWithDefaultStrengthOne() {
        PhaseEffect e = decode("{\"type\":\"add_affix\",\"affix\":\"champions:enkindling\"}", PhaseEffect.CODEC);
        assertInstanceOf(PhaseEffect.AddAffix.class, e);
        var add = (PhaseEffect.AddAffix) e;
        assertEquals(Utils.key("enkindling"), add.affixId());
        assertEquals(1, add.strength());
    }

    @Test
    void addAttributeEffectDecodesWithExplicitOperation() {
        PhaseEffect e = decode(
                "{\"type\":\"add_attribute\",\"attribute\":\"minecraft:generic.attack_damage\"," +
                        "\"amount\":1.0,\"operation\":\"add_multiplied_total\"}",
                PhaseEffect.CODEC);
        assertInstanceOf(PhaseEffect.AddAttribute.class, e);
        var add = (PhaseEffect.AddAttribute) e;
        assertEquals(ResourceLocation.tryParse("generic.attack_damage"), add.attribute());
        assertEquals(1.0, add.amount(), 0.0001);
        assertEquals("add_multiplied_total", add.operation());
    }

    @Test
    void addAttributeEffectDecodesWithDefaultOperation() {
        PhaseEffect e = decode(
                "{\"type\":\"add_attribute\",\"attribute\":\"minecraft:generic.movement_speed\",\"amount\":0.3}",
                PhaseEffect.CODEC);
        var add = (PhaseEffect.AddAttribute) e;
        assertEquals("add_value", add.operation());
    }

    @Test
    void addMobEffectDecodesWithInfiniteDefault() {
        PhaseEffect e = decode(
                "{\"type\":\"add_mob_effect\",\"effect\":\"minecraft:strength\",\"amplifier\":1}",
                PhaseEffect.CODEC);
        assertInstanceOf(PhaseEffect.AddMobEffect.class, e);
        var add = (PhaseEffect.AddMobEffect) e;
        assertEquals(ResourceLocation.tryParse("strength"), add.effectId());
        assertEquals(1, add.amplifier());
        assertTrue(add.infinite());
    }

    @Test
    void addMobEffectDecodesWithDurationWhenNotInfinite() {
        PhaseEffect e = decode(
                "{\"type\":\"add_mob_effect\",\"effect\":\"minecraft:strength\"," +
                        "\"amplifier\":0,\"infinite\":false,\"duration_ticks\":600}",
                PhaseEffect.CODEC);
        var add = (PhaseEffect.AddMobEffect) e;
        assertFalse(add.infinite());
        assertEquals(600, add.durationTicks());
    }

    // ── ChampionPhase wrapper ─────────────────────────────────────────────────

    @Test
    void championPhaseDecodesTheFullGeneratedShape() {
        String json = """
                {
                  "id": "champions:zombie_line_second_wind",
                  "condition": { "type": "health_percent", "below": 0.4 },
                  "effects": [
                    { "type": "add_mob_effect", "effect": "minecraft:strength", "amplifier": 1, "infinite": true, "duration_ticks": 0 }
                  ],
                  "repeatable": false
                }
                """;
        ChampionPhase phase = ChampionPhase.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json))
                .resultOrPartial(err -> fail("phase did not decode: " + err))
                .orElseThrow();
        assertEquals(Utils.key("zombie_line_second_wind"), phase.id());
        assertInstanceOf(PhaseCondition.HealthPercent.class, phase.condition());
        assertEquals(1, phase.effects().size());
        assertInstanceOf(PhaseEffect.AddMobEffect.class, phase.effects().get(0));
        assertFalse(phase.repeatable());
    }

    @Test
    void unknownDispatchTypeThrowsIllegalArgumentException() {
        // The dispatch codecs throw IllegalArgumentException for an unknown type —
        // matching the runtime loader behaviour. This guards the codec map so an
        // unrecognised "type" in a datapack file fails loudly instead of silently
        // defaulting to a wrong parse.
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> EntityFilter.CODEC.parse(JsonOps.INSTANCE,
                        JsonParser.parseString("{\"type\":\"not_a_real_filter\"}")));
        assertTrue(ex.getMessage().contains("not_a_real_filter"),
                "error should name the offending type, got: " + ex.getMessage());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static <T> T decode(String json, com.mojang.serialization.Codec<T> codec) {
        return codec.parse(JsonOps.INSTANCE, JsonParser.parseString(json))
                .resultOrPartial(err -> fail("failed to decode " + json + ": " + err))
                .orElseThrow();
    }
}
