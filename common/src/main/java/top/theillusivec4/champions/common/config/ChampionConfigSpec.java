package top.theillusivec4.champions.common.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * Builds the {@link ModConfigSpec} for Champions.
 *
 * <p>Compiled against forge-config-api-port in {@code common} (modCompileOnly).
 * At runtime, NeoForge provides the same class natively; Fabric gets it via the
 * bundled Forge Config API Port jar.</p>
 *
 * <p>Both platform entry points use this class:
 * <ul>
 *   <li>NeoForge: {@code ModLoadingContext.get().registerConfig(SERVER, SPEC)}</li>
 *   <li>Fabric:   {@code NeoForgeConfigRegistry.INSTANCE.register(MOD_ID, SERVER, SPEC)}</li>
 * </ul>
 * After the config is loaded, call {@link #bakeAndApply()} to push values into
 * {@link ChampionsConfig}.</p>
 */
public final class ChampionConfigSpec {

    public static final ModConfigSpec SPEC;
    private static final Entries ENTRIES;

    static {
        Pair<Entries, ModConfigSpec> pair = new ModConfigSpec.Builder().configure(Entries::new);
        ENTRIES = pair.getLeft();
        SPEC    = pair.getRight();
    }

    private ChampionConfigSpec() {}

    /** Read current spec values and push them into {@link ChampionsConfig}. */
    public static void bakeAndApply() {
        ChampionsConfig.Values v = new ChampionsConfig.Values();

        v.spawnChance             = ENTRIES.spawnChance.get().floatValue();
        v.beaconProtectionRange   = ENTRIES.beaconProtectionRange.get();
        v.championSpawners        = ENTRIES.championSpawners.get();
        v.championTrialSpawners   = ENTRIES.championTrialSpawners.get();

        v.showHud          = ENTRIES.showHud.get();
        v.showParticles    = ENTRIES.showParticles.get();
        v.bossBarBlacklist = ENTRIES.bossBarBlacklist.get();

        v.experienceGrowth = ENTRIES.experienceGrowth.get();
        v.explosionGrowth  = ENTRIES.explosionGrowth.get();

        v.lootDrops  = ENTRIES.lootDrops.get();
        v.lootScaling= ENTRIES.lootScaling.get();

        v.mobInherit           = ENTRIES.mobInherit.get();
        v.rankReduce            = ENTRIES.rankReduce.get();
        v.canHaveInfestedAffix  = ENTRIES.canHaveInfestedAffix.get();

        v.adaptableReductionIncrement = ENTRIES.adaptableReductionIncrement.get();
        v.adaptableMaxReduction       = ENTRIES.adaptableMaxReduction.get();
        v.arcticAttackInterval        = ENTRIES.arcticAttackInterval.get();
        v.enkindlingAttackInterval    = ENTRIES.enkindlingAttackInterval.get();
        v.dampeningReduction          = ENTRIES.dampeningReduction.get();
        v.desecratingInterval         = ENTRIES.desecratingInterval.get();
        v.desecratingActivationTime   = ENTRIES.desecratingActivationTime.get();
        v.desecratingRadius           = ENTRIES.desecratingRadius.get();
        v.desecratingDuration         = ENTRIES.desecratingDuration.get();
        v.hastySpeedBonus             = ENTRIES.hastySpeedBonus.get();
        v.infestedAmount              = ENTRIES.infestedAmount.get();
        v.infestedInterval            = ENTRIES.infestedInterval.get();
        v.infestedParasite            = ENTRIES.infestedParasite.get();
        v.infestedEnderParasite       = ENTRIES.infestedEnderParasite.get();
        v.knockingKnockback           = ENTRIES.knockingKnockback.get();
        v.livelyHealAmount            = ENTRIES.livelyHealAmount.get();
        v.livelyPassiveMultiplier     = ENTRIES.livelyPassiveMultiplier.get();
        v.livelyCooldown              = ENTRIES.livelyCooldown.get();
        v.magneticStrength            = ENTRIES.magneticStrength.get();
        v.magneticPullRange           = ENTRIES.magneticPullRange.get();
        v.moltenFireTicks             = ENTRIES.moltenFireTicks.get();
        v.moltenAuraDamage            = ENTRIES.moltenAuraDamage.get();
        v.moltenAuraRange             = ENTRIES.moltenAuraRange.get();
        v.paralyzingChance            = ENTRIES.paralyzingChance.get();
        v.paralyzingDuration          = ENTRIES.paralyzingDuration.get();
        v.plaguedRange                = ENTRIES.plaguedRange.get();
        v.reflectiveMinPercent        = ENTRIES.reflectiveMinPercent.get();
        v.reflectiveMaxPercent        = ENTRIES.reflectiveMaxPercent.get();
        v.reflectiveMax               = ENTRIES.reflectiveMax.get();
        v.reflectiveLethal            = ENTRIES.reflectiveLethal.get();
        v.shieldingChance             = ENTRIES.shieldingChance.get();
        v.woundingChance              = ENTRIES.woundingChance.get();
        v.woundingDuration            = ENTRIES.woundingDuration.get();

        ChampionsConfig.bake(v);
    }

    // ── Spec entries ──────────────────────────────────────────────────────────

    static final class Entries {

        final ModConfigSpec.DoubleValue  spawnChance;
        final ModConfigSpec.IntValue     beaconProtectionRange;
        final ModConfigSpec.BooleanValue championSpawners;
        final ModConfigSpec.BooleanValue championTrialSpawners;

        final ModConfigSpec.BooleanValue showHud;
        final ModConfigSpec.BooleanValue showParticles;
        final ModConfigSpec.ConfigValue<List<? extends String>> bossBarBlacklist;

        final ModConfigSpec.IntValue experienceGrowth;
        final ModConfigSpec.IntValue explosionGrowth;

        final ModConfigSpec.ConfigValue<List<? extends String>> lootDrops;
        final ModConfigSpec.BooleanValue lootScaling;

        final ModConfigSpec.BooleanValue mobInherit;
        final ModConfigSpec.IntValue     rankReduce;
        final ModConfigSpec.BooleanValue canHaveInfestedAffix;

        final ModConfigSpec.DoubleValue  adaptableReductionIncrement;
        final ModConfigSpec.DoubleValue  adaptableMaxReduction;
        final ModConfigSpec.IntValue     arcticAttackInterval;
        final ModConfigSpec.IntValue     enkindlingAttackInterval;
        final ModConfigSpec.DoubleValue  dampeningReduction;
        final ModConfigSpec.IntValue     desecratingInterval;
        final ModConfigSpec.IntValue     desecratingActivationTime;
        final ModConfigSpec.DoubleValue  desecratingRadius;
        final ModConfigSpec.IntValue     desecratingDuration;
        final ModConfigSpec.DoubleValue  hastySpeedBonus;
        final ModConfigSpec.IntValue     infestedAmount;
        final ModConfigSpec.IntValue     infestedInterval;
        final ModConfigSpec.ConfigValue<String> infestedParasite;
        final ModConfigSpec.ConfigValue<String> infestedEnderParasite;
        final ModConfigSpec.DoubleValue  knockingKnockback;
        final ModConfigSpec.DoubleValue  livelyHealAmount;
        final ModConfigSpec.DoubleValue  livelyPassiveMultiplier;
        final ModConfigSpec.IntValue     livelyCooldown;
        final ModConfigSpec.DoubleValue  magneticStrength;
        final ModConfigSpec.DoubleValue  magneticPullRange;
        final ModConfigSpec.IntValue     moltenFireTicks;
        final ModConfigSpec.DoubleValue  moltenAuraDamage;
        final ModConfigSpec.DoubleValue  moltenAuraRange;
        final ModConfigSpec.DoubleValue  paralyzingChance;
        final ModConfigSpec.IntValue     paralyzingDuration;
        final ModConfigSpec.DoubleValue  plaguedRange;
        final ModConfigSpec.DoubleValue  reflectiveMinPercent;
        final ModConfigSpec.DoubleValue  reflectiveMaxPercent;
        final ModConfigSpec.DoubleValue  reflectiveMax;
        final ModConfigSpec.BooleanValue reflectiveLethal;
        final ModConfigSpec.DoubleValue  shieldingChance;
        final ModConfigSpec.DoubleValue  woundingChance;
        final ModConfigSpec.IntValue     woundingDuration;

        Entries(ModConfigSpec.Builder b) {
            b.push("spawning");
            spawnChance           = b.comment("Probability (0–1) that an eligible mob becomes a champion on spawn")
                    .defineInRange("spawnChance", 0.1, 0.0, 1.0);
            beaconProtectionRange = b.comment("Radius around an active beacon where no champions spawn. 0 = disabled")
                    .defineInRange("beaconProtectionRange", 64, 0, 1000);
            championSpawners      = b.comment("Allow mobs from vanilla spawner blocks to become champions")
                    .define("championSpawners", false);
            championTrialSpawners = b.comment("Allow mobs from trial spawner blocks to become champions")
                    .define("championTrialSpawners", false);
            b.pop();

            b.push("hud");
            showHud          = b.comment("Show champion HUD when looking at a champion")
                    .define("showHud", true);
            showParticles    = b.comment("Show coloured rank particles around champions")
                    .define("showParticles", true);
            bossBarBlacklist = b.comment("Entity IDs whose HUD overlay is suppressed")
                    .defineListAllowEmpty("bossBarBlacklist",
                            List.of("minecraft:ender_dragon", "minecraft:wither"),
                            () -> "minecraft:ender_dragon", s -> s instanceof String);
            b.pop();

            b.push("growth");
            experienceGrowth = b.comment("Bonus XP multiplier per tier level above 1")
                    .defineInRange("experienceGrowth", 1, 0, Integer.MAX_VALUE);
            explosionGrowth  = b.comment("Explosion radius growth multiplier")
                    .defineInRange("explosionGrowth", 2, 0, 100);
            b.pop();

            b.push("loot");
            lootDrops  = b.comment("Loot drop entries: tier;modid:item;amount;enchanted;weight")
                    .defineListAllowEmpty("lootDrops",
                            List.of(
                                    "1;minecraft:iron_ingot;1;false;10",
                                    "2;minecraft:gold_ingot;1;false;8",
                                    "3;minecraft:diamond;1;false;5",
                                    "3;minecraft:emerald;1;false;4",
                                    "4;minecraft:diamond;2;true;4",
                                    "5;minecraft:netherite_scrap;1;false;2",
                                    "5;minecraft:diamond;3;true;3"),
                            () -> "1;minecraft:iron_ingot;1;false;10", s -> s instanceof String);
            lootScaling = b.comment("Scale number of loot draws to tier level")
                    .define("lootScaling", true);
            b.pop();

            b.push("mobSplit");
            mobInherit           = b.comment("Allow slime-like mobs to inherit champion status when splitting")
                    .define("mobInherit", false);
            rankReduce            = b.comment("Rank reduction applied to child mobs when splitting")
                    .defineInRange("rankReduce", 1, 0, Integer.MAX_VALUE);
            canHaveInfestedAffix  = b.comment("Allow child mobs from splits to have the Infested affix")
                    .define("canHaveInfestedAffix", false);
            b.pop();

            b.push("affixes");

            b.push("adaptable");
            adaptableReductionIncrement = b.defineInRange("reductionIncrement", 0.15, 0.0, 1.0);
            adaptableMaxReduction       = b.defineInRange("maxReduction", 0.90, 0.0, 1.0);
            b.pop();

            b.push("arctic");
            arcticAttackInterval = b.defineInRange("attackInterval", 3, 1, 100);
            b.pop();

            b.push("dampening");
            dampeningReduction = b.defineInRange("reduction", 0.30, 0.0, 1.0);
            b.pop();

            b.push("desecrating");
            desecratingInterval       = b.defineInRange("interval", 3, 1, Integer.MAX_VALUE);
            desecratingActivationTime = b.defineInRange("activationTime", 1, 0, Integer.MAX_VALUE);
            desecratingRadius         = b.defineInRange("radius", 4.0, 1.0, 32.0);
            desecratingDuration       = b.defineInRange("duration", 10, 1, Integer.MAX_VALUE);
            b.pop();

            b.push("enkindling");
            enkindlingAttackInterval = b.defineInRange("attackInterval", 3, 1, 100);
            b.pop();

            b.push("hasty");
            hastySpeedBonus = b.defineInRange("speedBonus", 0.25, 0.0, 10.0);
            b.pop();

            b.push("infested");
            infestedAmount   = b.defineInRange("amount", 2, 1, 100);
            infestedInterval = b.defineInRange("interval", 10, 1, 600);
            infestedParasite = b.comment("The mob to use as a parasite for infestation")
                    .define("parasite", "minecraft:silverfish");
            infestedEnderParasite = b.comment("The mob to use as a parasite for infestation of ender mob")
                    .define("infestedEnderParasite", "minecraft:endermite");
            b.pop();

            b.push("knocking");
            knockingKnockback = b.defineInRange("knockback", 5.0, 0.0, 100.0);
            b.pop();

            b.push("lively");
            livelyHealAmount        = b.defineInRange("healAmount", 1.0, 0.0, 100.0);
            livelyPassiveMultiplier = b.defineInRange("passiveMultiplier", 5.0, 1.0, 100.0);
            livelyCooldown          = b.defineInRange("cooldown", 3, 1, 600);
            b.pop();

            b.push("magnetic");
            magneticStrength  = b.defineInRange("strength", 0.8, 0.0, 10.0);
            magneticPullRange = b.defineInRange("pullRange", 16.0, 1.0, 64.0);
            b.pop();

            b.push("molten");
            moltenFireTicks  = b.defineInRange("fireTicks", 80, 1, 2000);
            moltenAuraDamage = b.defineInRange("auraDamage", 2.0, 0.0, 100.0);
            moltenAuraRange  = b.defineInRange("auraRange", 3.0, 0.5, 32.0);
            b.pop();

            b.push("paralyzing");
            paralyzingChance   = b.defineInRange("chance", 0.20, 0.0, 1.0);
            paralyzingDuration = b.defineInRange("duration", 60, 1, 1200);
            b.pop();

            b.push("plagued");
            plaguedRange = b.defineInRange("range", 5.0, 1.0, 32.0);
            b.pop();

            b.push("reflective");
            reflectiveMinPercent = b.defineInRange("minPercent", 0.10, 0.0, 1.0);
            reflectiveMaxPercent = b.defineInRange("maxPercent", 0.35, 0.0, 1.0);
            reflectiveMax        = b.defineInRange("max", 8.0, 0.0, 10000.0);
            reflectiveLethal     = b.define("lethal", false);
            b.pop();

            b.push("shielding");
            shieldingChance = b.defineInRange("chance", 0.50, 0.0, 1.0);
            b.pop();

            b.push("wounding");
            woundingChance   = b.defineInRange("chance", 0.40, 0.0, 1.0);
            woundingDuration = b.defineInRange("duration", 200, 1, 6000);
            b.pop();

            b.pop(); // affixes
        }
    }
}
