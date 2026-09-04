package top.theillusivec4.champions.common.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * Builds the {@link ForgeConfigSpec} for Champions.
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

    public static final ForgeConfigSpec SPEC;
    private static final Entries ENTRIES;

    static {
        Pair<Entries, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder().configure(Entries::new);
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
        v.lootSource = ENTRIES.lootSource.get();

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

        final ForgeConfigSpec.DoubleValue  spawnChance;
        final ForgeConfigSpec.IntValue     beaconProtectionRange;
        final ForgeConfigSpec.BooleanValue championSpawners;
        final ForgeConfigSpec.BooleanValue championTrialSpawners;

        final ForgeConfigSpec.BooleanValue showHud;
        final ForgeConfigSpec.BooleanValue showParticles;
        final ForgeConfigSpec.ConfigValue<List<? extends String>> bossBarBlacklist;

        final ForgeConfigSpec.IntValue experienceGrowth;
        final ForgeConfigSpec.IntValue explosionGrowth;

        final ForgeConfigSpec.ConfigValue<List<? extends String>> lootDrops;
        final ForgeConfigSpec.BooleanValue lootScaling;
        final ForgeConfigSpec.EnumValue<LootSource> lootSource;

        final ForgeConfigSpec.BooleanValue mobInherit;
        final ForgeConfigSpec.IntValue     rankReduce;
        final ForgeConfigSpec.BooleanValue canHaveInfestedAffix;

        final ForgeConfigSpec.DoubleValue  adaptableReductionIncrement;
        final ForgeConfigSpec.DoubleValue  adaptableMaxReduction;
        final ForgeConfigSpec.IntValue     arcticAttackInterval;
        final ForgeConfigSpec.IntValue     enkindlingAttackInterval;
        final ForgeConfigSpec.DoubleValue  dampeningReduction;
        final ForgeConfigSpec.IntValue     desecratingInterval;
        final ForgeConfigSpec.IntValue     desecratingActivationTime;
        final ForgeConfigSpec.DoubleValue  desecratingRadius;
        final ForgeConfigSpec.IntValue     desecratingDuration;
        final ForgeConfigSpec.DoubleValue  hastySpeedBonus;
        final ForgeConfigSpec.IntValue     infestedAmount;
        final ForgeConfigSpec.IntValue     infestedInterval;
        final ForgeConfigSpec.ConfigValue<String> infestedParasite;
        final ForgeConfigSpec.ConfigValue<String> infestedEnderParasite;
        final ForgeConfigSpec.DoubleValue  knockingKnockback;
        final ForgeConfigSpec.DoubleValue  livelyHealAmount;
        final ForgeConfigSpec.DoubleValue  livelyPassiveMultiplier;
        final ForgeConfigSpec.IntValue     livelyCooldown;
        final ForgeConfigSpec.DoubleValue  magneticStrength;
        final ForgeConfigSpec.DoubleValue  magneticPullRange;
        final ForgeConfigSpec.IntValue     moltenFireTicks;
        final ForgeConfigSpec.DoubleValue  moltenAuraDamage;
        final ForgeConfigSpec.DoubleValue  moltenAuraRange;
        final ForgeConfigSpec.DoubleValue  paralyzingChance;
        final ForgeConfigSpec.IntValue     paralyzingDuration;
        final ForgeConfigSpec.DoubleValue  plaguedRange;
        final ForgeConfigSpec.DoubleValue  reflectiveMinPercent;
        final ForgeConfigSpec.DoubleValue  reflectiveMaxPercent;
        final ForgeConfigSpec.DoubleValue  reflectiveMax;
        final ForgeConfigSpec.BooleanValue reflectiveLethal;
        final ForgeConfigSpec.DoubleValue  shieldingChance;
        final ForgeConfigSpec.DoubleValue  woundingChance;
        final ForgeConfigSpec.IntValue     woundingDuration;

        Entries(ForgeConfigSpec.Builder b) {
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
                    .defineList("bossBarBlacklist",
                            List.<String>of("minecraft:ender_dragon", "minecraft:wither"),
                            s -> s instanceof String);
            b.pop();

            b.push("growth");
            experienceGrowth = b.comment("Bonus XP multiplier per tier level above 1")
                    .defineInRange("experienceGrowth", 1, 0, Integer.MAX_VALUE);
            explosionGrowth  = b.comment("Explosion radius growth multiplier")
                    .defineInRange("explosionGrowth", 2, 0, 100);
            b.pop();

            b.push("loot");
            lootDrops  = b.comment("Loot drop entries: tier;modid:item;amount;enchanted;weight")
                    .defineList("lootDrops",
                            List.of(
                                    "1;minecraft:iron_ingot;1;false;10",
                                    "2;minecraft:gold_ingot;1;false;8",
                                    "3;minecraft:diamond;1;false;5",
                                    "3;minecraft:emerald;1;false;4",
                                    "4;minecraft:diamond;2;true;4",
                                    "5;minecraft:netherite_scrap;1;false;2",
                                    "5;minecraft:diamond;3;true;3"),
                            s -> s instanceof String);
            lootScaling = b.comment("Scale number of loot draws to tier level")
                    .define("lootScaling", true);
            lootSource = b.comment("Which loot sources are rolled on champion death:",
                            "CONFIG - only the lootDrops config entries above",
                            "LOOT_TABLE - only the champions:champion_loot datapack loot table",
                            "CONFIG_AND_LOOT_TABLE - roll both sources")
                    .defineEnum("lootSource", LootSource.CONFIG_AND_LOOT_TABLE);
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
            adaptableReductionIncrement = b.comment("Damage reduction gained per consecutive hit of the same damage type (scales with affix strength)")
                    .defineInRange("reductionIncrement", 0.15, 0.0, 1.0);
            adaptableMaxReduction       = b.comment("Cap for the adaptable damage reduction")
                    .defineInRange("maxReduction", 0.90, 0.0, 1.0);
            b.pop();

            b.push("arctic");
            arcticAttackInterval = b.comment("Interval, in seconds, between Arctic ice-shot attacks")
                    .defineInRange("attackInterval", 3, 1, 100);
            b.pop();

            b.push("dampening");
            dampeningReduction = b.comment("Base fraction of direct melee/projectile damage reduced (scales with strength, hard cap 90%)")
                    .defineInRange("reduction", 0.30, 0.0, 1.0);
            b.pop();

            b.push("desecrating");
            desecratingInterval       = b.comment("Interval, in seconds, between harm-cloud drops at the target's position")
                    .defineInRange("interval", 3, 1, Integer.MAX_VALUE);
            desecratingActivationTime = b.comment("Currently unused (reserved). Intended: delay, in seconds, before the first cloud")
                    .defineInRange("activationTime", 1, 0, Integer.MAX_VALUE);
            desecratingRadius         = b.comment("Initial radius, in blocks, of the harm cloud")
                    .defineInRange("radius", 4.0, 1.0, 32.0);
            desecratingDuration       = b.comment("Currently unused — cloud lifetime is 200 + 40 x strength ticks")
                    .defineInRange("duration", 10, 1, Integer.MAX_VALUE);
            b.pop();

            b.push("enkindling");
            enkindlingAttackInterval = b.comment("Interval, in seconds, between Enkindling fire-shot attacks")
                    .defineInRange("attackInterval", 3, 1, 100);
            b.pop();

            b.push("hasty");
            hastySpeedBonus = b.comment("Movement speed bonus applied while the affix is active (scales with strength)")
                    .defineInRange("speedBonus", 0.25, 0.0, 10.0);
            b.pop();

            b.push("infested");
            infestedAmount   = b.comment("Base number of parasites per spawn wave (scales with strength)")
                    .defineInRange("amount", 2, 1, 100);
            infestedInterval = b.comment("Currently unused (reserved). Intended: interval, in seconds, between parasite waves")
                    .defineInRange("interval", 10, 1, 600);
            infestedParasite = b.comment("The mob to use as a parasite for infestation")
                    .define("parasite", "minecraft:silverfish");
            infestedEnderParasite = b.comment("The mob to use as a parasite for infestation of ender mob")
                    .define("infestedEnderParasite", "minecraft:endermite");
            b.pop();

            b.push("knocking");
            knockingKnockback = b.comment("Knockback force applied to attackers (scales with strength)")
                    .defineInRange("knockback", 5.0, 0.0, 100.0);
            b.pop();

            b.push("lively");
            livelyHealAmount        = b.comment("HP healed per second once the cooldown has elapsed (scales with strength)")
                    .defineInRange("healAmount", 1.0, 0.0, 100.0);
            livelyPassiveMultiplier = b.comment("Bonus multiplier while the champion has been idle/unprovoked for a while")
                    .defineInRange("passiveMultiplier", 5.0, 1.0, 100.0);
            livelyCooldown          = b.comment("Seconds without taking damage before regeneration starts (scales inversely with strength)")
                    .defineInRange("cooldown", 3, 1, 600);
            b.pop();

            b.push("magnetic");
            magneticStrength  = b.comment("Pull force applied to targets (scales with strength)")
                    .defineInRange("strength", 0.8, 0.0, 10.0);
            magneticPullRange = b.comment("Radius, in blocks, in which targets are pulled toward the champion")
                    .defineInRange("pullRange", 16.0, 1.0, 64.0);
            b.pop();

            b.push("molten");
            moltenFireTicks  = b.comment("Fire duration, in ticks, applied on hits in either direction (scales with strength)")
                    .defineInRange("fireTicks", 80, 1, 2000);
            moltenAuraDamage = b.comment("Fire damage dealt by the aura per second and by hit bursts (scales with strength)")
                    .defineInRange("auraDamage", 2.0, 0.0, 100.0);
            moltenAuraRange  = b.comment("Radius, in blocks, of the fire aura (scales with strength)")
                    .defineInRange("auraRange", 3.0, 0.5, 32.0);
            b.pop();

            b.push("paralyzing");
            paralyzingChance   = b.comment("Chance per champion attack to paralyse the victim (scales with strength, capped at 95%)")
                    .defineInRange("chance", 0.20, 0.0, 1.0);
            paralyzingDuration = b.comment("Paralysis duration, in ticks")
                    .defineInRange("duration", 60, 1, 1200);
            b.pop();

            b.push("plagued");
            plaguedRange = b.comment("Base radius, in blocks, of the debuff aura (scales with strength)")
                    .defineInRange("range", 5.0, 1.0, 32.0);
            b.pop();

            b.push("reflective");
            reflectiveMinPercent = b.comment("Minimum fraction of damage reflected back at the attacker")
                    .defineInRange("minPercent", 0.10, 0.0, 1.0);
            reflectiveMaxPercent = b.comment("Maximum fraction of damage reflected (scales with strength)")
                    .defineInRange("maxPercent", 0.35, 0.0, 1.0);
            reflectiveMax        = b.comment("Absolute cap on reflected damage")
                    .defineInRange("max", 8.0, 0.0, 10000.0);
            reflectiveLethal     = b.comment("Whether reflected damage can kill the attacker")
                    .define("lethal", false);
            b.pop();

            b.push("shielding");
            shieldingChance = b.comment("Base chance to fully block an attack (scales with strength)")
                    .defineInRange("chance", 0.50, 0.0, 1.0);
            b.pop();

            b.push("wounding");
            woundingChance   = b.comment("Chance per champion attack to wound the victim (scales with strength)")
                    .defineInRange("chance", 0.40, 0.0, 1.0);
            woundingDuration = b.comment("Wound duration, in ticks (scales with strength)")
                    .defineInRange("duration", 200, 1, 6000);
            b.pop();

            b.pop(); // affixes
        }
    }
}
