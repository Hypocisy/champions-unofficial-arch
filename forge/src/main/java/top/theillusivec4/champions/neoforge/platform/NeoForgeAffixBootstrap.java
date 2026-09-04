package top.theillusivec4.champions.neoforge.platform;

import net.minecraftforge.registries.RegistryObject;
import top.theillusivec4.champions.api.affix.AffixType;
import top.theillusivec4.champions.common.affix.builtin.AdaptableAffix;
import top.theillusivec4.champions.common.affix.builtin.LivelyAffix;
import top.theillusivec4.champions.common.affix.builtin.ReflectiveAffix;
import top.theillusivec4.champions.common.affix.builtin.ShieldingAffix;
import top.theillusivec4.champions.common.affix.builtin.StatelessCombatAffixes.DampeningAffix;
import top.theillusivec4.champions.common.affix.builtin.StatelessCombatAffixes.KnockingAffix;
import top.theillusivec4.champions.common.affix.builtin.StatelessCombatAffixes.ParalyzingAffix;
import top.theillusivec4.champions.common.affix.builtin.StatelessCombatAffixes.WoundingAffix;
import top.theillusivec4.champions.common.affix.builtin.goal_affixes.*;
import top.theillusivec4.champions.common.affix.builtin.spawn_tick_affixes.HastyAffix;
import top.theillusivec4.champions.common.affix.builtin.spawn_tick_affixes.MoltenAffix;
import top.theillusivec4.champions.common.affix.builtin.spawn_tick_affixes.PlaguedAffix;
import top.theillusivec4.champions.neoforge.registry.NeoForgeAffixTypeRegistry;

public final class NeoForgeAffixBootstrap {
    // ── Holders ───────────────────────────────────────────────────────────────
    // Held as static fields so code outside this package can reference the types,
    // e.g. champion.hasAffix(ChampionAffixes.ADAPTABLE.get()).

    public static RegistryObject<AffixType<?>> ADAPTABLE;
    public static RegistryObject<AffixType<?>> ARCTIC;
    public static RegistryObject<AffixType<?>> DAMPENING;
    public static RegistryObject<AffixType<?>> DESECRATING;
    public static RegistryObject<AffixType<?>> ENKINDLING;
    public static RegistryObject<AffixType<?>> HASTY;
    public static RegistryObject<AffixType<?>> INFESTED;
    public static RegistryObject<AffixType<?>> KNOCKING;
    public static RegistryObject<AffixType<?>> LIVELY;
    public static RegistryObject<AffixType<?>> MAGNETIC;
    public static RegistryObject<AffixType<?>> MOLTEN;
    public static RegistryObject<AffixType<?>> PARALYZING;
    public static RegistryObject<AffixType<?>> PLAGUED;
    public static RegistryObject<AffixType<?>> REFLECTIVE;
    public static RegistryObject<AffixType<?>> SHIELDING;
    public static RegistryObject<AffixType<?>> WOUNDING;

    // ── NeoForge bootstrap ────────────────────────────────────────────────────

    /**
     * Register all built-in affixes via NeoForge's DeferredRegister.
     * Must be called before the registry is frozen (i.e. during mod init).
     */
    public static void bootstrap() {
        ADAPTABLE = NeoForgeAffixTypeRegistry.register("adaptable", AdaptableAffix::new);
        ARCTIC = NeoForgeAffixTypeRegistry.register("arctic", ArcticAffix::new);
        DAMPENING = NeoForgeAffixTypeRegistry.register("dampening", DampeningAffix::new);
        DESECRATING = NeoForgeAffixTypeRegistry.register("desecrating", DesecratingAffix::new);
        ENKINDLING = NeoForgeAffixTypeRegistry.register("enkindling", EnkindlingAffix::new);
        HASTY = NeoForgeAffixTypeRegistry.register("hasty", HastyAffix::new);
        INFESTED = NeoForgeAffixTypeRegistry.register("infested", InfestedAffix::new);
        KNOCKING = NeoForgeAffixTypeRegistry.register("knocking", KnockingAffix::new);
        LIVELY = NeoForgeAffixTypeRegistry.register("lively", LivelyAffix::new);
        MAGNETIC = NeoForgeAffixTypeRegistry.register("magnetic", MagneticAffix::new);
        MOLTEN = NeoForgeAffixTypeRegistry.register("molten", MoltenAffix::new);
        PARALYZING = NeoForgeAffixTypeRegistry.register("paralyzing", ParalyzingAffix::new);
        PLAGUED = NeoForgeAffixTypeRegistry.register("plagued", PlaguedAffix::new);
        REFLECTIVE = NeoForgeAffixTypeRegistry.register("reflective", ReflectiveAffix::new);
        SHIELDING = NeoForgeAffixTypeRegistry.register("shielding", ShieldingAffix::new);
        WOUNDING = NeoForgeAffixTypeRegistry.register("wounding", WoundingAffix::new);
    }
}