package top.theillusivec4.champions.fabric.registry;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
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

public class FabricAffixBootStrap {
    // ── Fabric bootstrap ──────────────────────────────────────────────────────

    /**
     * Register all built-in affixes directly into the Fabric registry.
     * Called from ChampionsFabric after FabricAffixTypeRegistry.bootstrap().
     */
    public static void registerAll() {
        Registry<AffixType<?>> reg =
                FabricAffixTypeRegistry.REGISTRY;
        ResourceLocation ns = ResourceLocation
                .fromNamespaceAndPath("champions", "");

        register(reg, "adaptable", new AdaptableAffix());
        register(reg, "arctic", new ArcticAffix());
        register(reg, "dampening", new DampeningAffix());
        register(reg, "desecrating", new DesecratingAffix());
        register(reg, "enkindling", new EnkindlingAffix());
        register(reg, "hasty", new HastyAffix());
        register(reg, "infested", new InfestedAffix());
        register(reg, "knocking", new KnockingAffix());
        register(reg, "lively", new LivelyAffix());
        register(reg, "magnetic", new MagneticAffix());
        register(reg, "molten", new MoltenAffix());
        register(reg, "paralyzing", new ParalyzingAffix());
        register(reg, "plagued", new PlaguedAffix());
        register(reg, "reflective", new ReflectiveAffix());
        register(reg, "shielding", new ShieldingAffix());
        register(reg, "wounding", new WoundingAffix());
    }

    private static void register(
            Registry<AffixType<?>> reg,
            String name,
            AffixType<?> type
    ) {
        Registry.register(reg,
                ResourceLocation.fromNamespaceAndPath("champions", name),
                type);
    }
}
