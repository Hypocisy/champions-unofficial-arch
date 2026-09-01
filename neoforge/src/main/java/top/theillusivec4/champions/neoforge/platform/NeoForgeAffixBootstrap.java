package top.theillusivec4.champions.neoforge.platform;

import net.neoforged.neoforge.registries.DeferredHolder;
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

    public static DeferredHolder<AffixType<?>, AdaptableAffix> ADAPTABLE;
    public static DeferredHolder<AffixType<?>, ArcticAffix> ARCTIC;
    public static DeferredHolder<AffixType<?>, DampeningAffix> DAMPENING;
    public static DeferredHolder<AffixType<?>, DesecratingAffix> DESECRATING;
    public static DeferredHolder<AffixType<?>, EnkindlingAffix> ENKINDLING;
    public static DeferredHolder<AffixType<?>, HastyAffix> HASTY;
    public static DeferredHolder<AffixType<?>, InfestedAffix> INFESTED;
    public static DeferredHolder<AffixType<?>, KnockingAffix> KNOCKING;
    public static DeferredHolder<AffixType<?>, LivelyAffix> LIVELY;
    public static DeferredHolder<AffixType<?>, MagneticAffix> MAGNETIC;
    public static DeferredHolder<AffixType<?>, MoltenAffix> MOLTEN;
    public static DeferredHolder<AffixType<?>, ParalyzingAffix> PARALYZING;
    public static DeferredHolder<AffixType<?>, PlaguedAffix> PLAGUED;
    public static DeferredHolder<AffixType<?>, ReflectiveAffix> REFLECTIVE;
    public static DeferredHolder<AffixType<?>, ShieldingAffix> SHIELDING;
    public static DeferredHolder<AffixType<?>, WoundingAffix> WOUNDING;

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