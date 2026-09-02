package top.theillusivec4.champions.common.config;

import java.util.List;

/**
 * Baked (flat) config values for Champions.
 *
 * <p>This class is platform-agnostic and has <strong>no</strong> dependency on
 * {@code ModConfigSpec} or any forge-config API — the spec is built by each
 * platform module and pushed here via {@link #bake(Values)}.</p>
 *
 * <p>Access pattern:
 * <ol>
 *   <li>Platform builds a {@link Values} object from its {@code ModConfigSpec}.</li>
 *   <li>Platform calls {@link #bake(Values)} once after config load and again on reload.</li>
 *   <li>All common code reads the static fields directly (no locking needed — single
 *       server thread writes, reads happen on the same thread).</li>
 * </ol>
 * </p>
 */
public final class ChampionsConfig {

    private ChampionsConfig() {}

    // ── Spawning ──────────────────────────────────────────────────────────────
    public static float   spawnChance             = 0.1f;
    public static int     beaconProtectionRange    = 64;
    public static boolean championSpawners         = false;
    public static boolean championTrialSpawners    = false;

    // ── HUD ───────────────────────────────────────────────────────────────────
    public static boolean showHud         = true;
    public static boolean showParticles   = true;
    public static List<? extends String> bossBarBlacklist = List.of(
            "minecraft:ender_dragon", "minecraft:wither");

    // ── Growth ────────────────────────────────────────────────────────────────
    public static int experienceGrowth = 1;
    public static int explosionGrowth  = 2;

    // ── Loot ─────────────────────────────────────────────────────────────────
    public static List<? extends String> lootDrops = List.of(
            "1;minecraft:iron_ingot;1;false;10",
            "2;minecraft:gold_ingot;1;false;8",
            "3;minecraft:diamond;1;false;5",
            "3;minecraft:emerald;1;false;4",
            "4;minecraft:diamond;2;true;4",
            "5;minecraft:netherite_scrap;1;false;2",
            "5;minecraft:diamond;3;true;3"
    );
    public static boolean lootScaling = true;
    /** Which loot sources are rolled — see {@link LootSource}. */
    public static LootSource lootSource = LootSource.CONFIG_AND_LOOT_TABLE;

    // ── Mob split ─────────────────────────────────────────────────────────────
    public static boolean mobInherit           = false;
    public static int     rankReduce            = 1;
    public static boolean canHaveInfestedAffix  = false;

    // ── Affixes ───────────────────────────────────────────────────────────────
    public static double  adaptableReductionIncrement = 0.15;
    public static double  adaptableMaxReduction       = 0.90;
    public static int     arcticAttackInterval        = 3;
    public static int     enkindlingAttackInterval    = 3;
    public static double  dampeningReduction          = 0.30;
    public static int     desecratingInterval         = 3;
    public static int     desecratingActivationTime   = 1;
    public static double  desecratingRadius           = 4.0;
    public static int     desecratingDuration         = 10;
    public static double  hastySpeedBonus             = 0.25;
    public static int     infestedAmount              = 2;
    public static int     infestedInterval            = 10;
    public static String  infestedParasite            = "minecraft:silverfish";
    public static String  infestedEnderParasite       = "minecraft:endermite";
    public static double  knockingKnockback           = 5.0;
    public static double  livelyHealAmount            = 1.0;
    public static double  livelyPassiveMultiplier     = 5.0;
    public static int     livelyCooldown              = 3;
    public static double  magneticStrength            = 0.8;
    public static double  magneticPullRange           = 16.0;
    public static int     moltenFireTicks             = 80;
    public static double  moltenAuraDamage            = 2.0;
    public static double  moltenAuraRange             = 3.0;
    public static double  paralyzingChance            = 0.20;
    public static int     paralyzingDuration          = 60;
    public static double  plaguedRange                = 5.0;
    public static double  reflectiveMinPercent        = 0.10;
    public static double  reflectiveMaxPercent        = 0.35;
    public static double  reflectiveMax               = 8.0;
    public static boolean reflectiveLethal            = false;
    public static double  shieldingChance             = 0.50;
    public static double  woundingChance              = 0.40;
    public static int     woundingDuration            = 200;

    // ── Bake API ──────────────────────────────────────────────────────────────

    /** Called by each platform after config loads/reloads. */
    public static void bake(Values v) {
        spawnChance             = v.spawnChance;
        beaconProtectionRange   = v.beaconProtectionRange;
        championSpawners        = v.championSpawners;
        championTrialSpawners   = v.championTrialSpawners;

        showHud          = v.showHud;
        showParticles    = v.showParticles;
        bossBarBlacklist = v.bossBarBlacklist;

        experienceGrowth = v.experienceGrowth;
        explosionGrowth  = v.explosionGrowth;

        lootDrops   = v.lootDrops;
        lootScaling = v.lootScaling;
        lootSource  = v.lootSource;

        mobInherit           = v.mobInherit;
        rankReduce            = v.rankReduce;
        canHaveInfestedAffix  = v.canHaveInfestedAffix;

        adaptableReductionIncrement = v.adaptableReductionIncrement;
        adaptableMaxReduction       = v.adaptableMaxReduction;
        arcticAttackInterval        = v.arcticAttackInterval;
        enkindlingAttackInterval    = v.enkindlingAttackInterval;
        dampeningReduction          = v.dampeningReduction;
        desecratingInterval         = v.desecratingInterval;
        desecratingActivationTime   = v.desecratingActivationTime;
        desecratingRadius           = v.desecratingRadius;
        desecratingDuration         = v.desecratingDuration;
        hastySpeedBonus             = v.hastySpeedBonus;
        infestedAmount              = v.infestedAmount;
        infestedInterval            = v.infestedInterval;
        infestedParasite            = v.infestedParasite;
        knockingKnockback           = v.knockingKnockback;
        livelyHealAmount            = v.livelyHealAmount;
        livelyPassiveMultiplier     = v.livelyPassiveMultiplier;
        livelyCooldown              = v.livelyCooldown;
        magneticStrength            = v.magneticStrength;
        magneticPullRange           = v.magneticPullRange;
        moltenFireTicks             = v.moltenFireTicks;
        moltenAuraDamage            = v.moltenAuraDamage;
        moltenAuraRange             = v.moltenAuraRange;
        paralyzingChance            = v.paralyzingChance;
        paralyzingDuration          = v.paralyzingDuration;
        plaguedRange                = v.plaguedRange;
        reflectiveMinPercent        = v.reflectiveMinPercent;
        reflectiveMaxPercent        = v.reflectiveMaxPercent;
        reflectiveMax               = v.reflectiveMax;
        reflectiveLethal            = v.reflectiveLethal;
        shieldingChance             = v.shieldingChance;
        woundingChance              = v.woundingChance;
        woundingDuration            = v.woundingDuration;
    }

    // ── Values POJO ───────────────────────────────────────────────────────────

    /**
     * Plain-Java snapshot of all config values.
     * Each platform creates one of these from its {@code ModConfigSpec} and passes it to
     * {@link #bake(Values)}.
     */
    public static final class Values {
        // Spawning
        public float   spawnChance             = 0.1f;
        public int     beaconProtectionRange    = 64;
        public boolean championSpawners         = false;
        public boolean championTrialSpawners    = false;
        // HUD
        public boolean showHud       = true;
        public boolean showParticles = true;
        public List<? extends String> bossBarBlacklist = List.of(
                "minecraft:ender_dragon", "minecraft:wither");
        // Growth
        public int experienceGrowth = 1;
        public int explosionGrowth  = 2;
        // Loot
        public List<? extends String> lootDrops = List.of(
                "1;minecraft:iron_ingot;1;false;10",
                "2;minecraft:gold_ingot;1;false;8",
                "3;minecraft:diamond;1;false;5",
                "3;minecraft:emerald;1;false;4",
                "4;minecraft:diamond;2;true;4",
                "5;minecraft:netherite_scrap;1;false;2",
                "5;minecraft:diamond;3;true;3");
        public boolean lootScaling = true;
        public LootSource lootSource = LootSource.CONFIG_AND_LOOT_TABLE;
        // Mob split
        public boolean mobInherit          = false;
        public int     rankReduce           = 1;
        public boolean canHaveInfestedAffix = false;
        // Affixes
        public double  adaptableReductionIncrement = 0.15;
        public double  adaptableMaxReduction       = 0.90;
        public int     arcticAttackInterval        = 3;
        public int     enkindlingAttackInterval    = 3;
        public double  dampeningReduction          = 0.30;
        public int     desecratingInterval         = 3;
        public int     desecratingActivationTime   = 1;
        public double  desecratingRadius           = 4.0;
        public int     desecratingDuration         = 10;
        public double  hastySpeedBonus             = 0.25;
        public int     infestedAmount              = 2;
        public int     infestedInterval            = 10;
        public String  infestedParasite            = "minecraft:silverfish";
        public String  infestedEnderParasite            = "minecraft:endermite";

        public double  knockingKnockback           = 5.0;
        public double  livelyHealAmount            = 1.0;
        public double  livelyPassiveMultiplier     = 5.0;
        public int     livelyCooldown              = 3;
        public double  magneticStrength            = 0.8;
        public double  magneticPullRange           = 16.0;
        public int     moltenFireTicks             = 80;
        public double  moltenAuraDamage            = 2.0;
        public double  moltenAuraRange             = 3.0;
        public double  paralyzingChance            = 0.20;
        public int     paralyzingDuration          = 60;
        public double  plaguedRange                = 5.0;
        public double  reflectiveMinPercent        = 0.10;
        public double  reflectiveMaxPercent        = 0.35;
        public double  reflectiveMax               = 8.0;
        public boolean reflectiveLethal            = false;
        public double  shieldingChance             = 0.50;
        public double  woundingChance              = 0.40;
        public int     woundingDuration            = 200;
    }
}
