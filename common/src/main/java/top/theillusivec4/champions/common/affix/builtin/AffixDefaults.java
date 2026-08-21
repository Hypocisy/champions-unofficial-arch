package top.theillusivec4.champions.common.affix.builtin;

import top.theillusivec4.champions.common.config.ChampionsConfig;

/**
 * Runtime-read affix tuning values.
 *
 * <p>Every field here simply forwards to {@link ChampionsConfig}'s baked static fields,
 * which are updated on each config load/reload via {@link ChampionsConfig#bake(ChampionsConfig.Values)}.
 * Affix code should only call these — never read ChampionsConfig directly — so that
 * the indirection layer remains easy to swap.</p>
 */
public final class AffixDefaults {
    private AffixDefaults() {}

    // Adaptable
    public static double ADAPTABLE_REDUCTION_INCREMENT() { return ChampionsConfig.adaptableReductionIncrement; }
    public static double ADAPTABLE_MAX_REDUCTION()       { return ChampionsConfig.adaptableMaxReduction; }

    // Dampening
    public static double DAMPENING_REDUCTION() { return ChampionsConfig.dampeningReduction; }

    // Hasty
    public static double HASTY_SPEED_BONUS() { return ChampionsConfig.hastySpeedBonus; }

    // Knocking
    public static double KNOCKING_KNOCKBACK() { return ChampionsConfig.knockingKnockback; }

    // Lively
    public static double LIVELY_HEAL_AMOUNT()        { return ChampionsConfig.livelyHealAmount; }
    public static double LIVELY_PASSIVE_MULTIPLIER() { return ChampionsConfig.livelyPassiveMultiplier; }
    public static int    LIVELY_COOLDOWN_SECONDS()   { return ChampionsConfig.livelyCooldown; }

    // Magnetic
    public static double MAGNETIC_PULL_STRENGTH() { return ChampionsConfig.magneticStrength; }
    public static double MAGNETIC_PULL_RANGE()    { return ChampionsConfig.magneticPullRange; }

    // Molten
    public static int    MOLTEN_FIRE_TICKS()  { return ChampionsConfig.moltenFireTicks; }
    public static double MOLTEN_AURA_DAMAGE() { return ChampionsConfig.moltenAuraDamage; }
    public static double MOLTEN_AURA_RANGE()  { return ChampionsConfig.moltenAuraRange; }

    // Paralyzing
    public static double PARALYZING_CHANCE()   { return ChampionsConfig.paralyzingChance; }
    public static int    PARALYZING_DURATION() { return ChampionsConfig.paralyzingDuration; }

    // Plagued
    public static double PLAGUED_RANGE() { return ChampionsConfig.plaguedRange; }

    // Reflective
    public static double  REFLECTIVE_MIN_PERCENT() { return ChampionsConfig.reflectiveMinPercent; }
    public static double  REFLECTIVE_MAX_PERCENT() { return ChampionsConfig.reflectiveMaxPercent; }
    public static double  REFLECTIVE_MAX()         { return ChampionsConfig.reflectiveMax; }
    public static boolean REFLECTIVE_LETHAL()      { return ChampionsConfig.reflectiveLethal; }

    // Shielding
    public static double SHIELDING_CHANCE() { return ChampionsConfig.shieldingChance; }

    // Wounding
    public static double WOUNDING_CHANCE()   { return ChampionsConfig.woundingChance; }
    public static int    WOUNDING_DURATION() { return ChampionsConfig.woundingDuration; }

    // Arctic / Enkindling
    public static int ARCTIC_ATTACK_INTERVAL()     { return ChampionsConfig.arcticAttackInterval; }
    public static int ENKINDLING_ATTACK_INTERVAL() { return ChampionsConfig.enkindlingAttackInterval; }

    // Desecrating
    public static int    DESECRATING_INTERVAL()        { return ChampionsConfig.desecratingInterval; }
    public static int    DESECRATING_ACTIVATION_TIME() { return ChampionsConfig.desecratingActivationTime; }
    public static double DESECRATING_RADIUS()          { return ChampionsConfig.desecratingRadius; }
    public static int    DESECRATING_DURATION()        { return ChampionsConfig.desecratingDuration; }

    // Infested
    public static int    INFESTED_SPAWN_COUNT() { return ChampionsConfig.infestedAmount; }
    public static int    INFESTED_INTERVAL()    { return ChampionsConfig.infestedInterval; }
    public static String INFESTED_PARASITE()    { return ChampionsConfig.infestedParasite; }
}
