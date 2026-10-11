package top.theillusivec4.champions.fabric.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import top.theillusivec4.champions.common.effect.ChampionEffects;
import top.theillusivec4.champions.common.effect.ParalysisEffect;
import top.theillusivec4.champions.common.effect.WoundEffect;

public final class ModMobEffects {

    public static final WoundEffect WOUND = new WoundEffect();
    public static final ParalysisEffect PARALYSIS = new ParalysisEffect();

    public static void register() {
        Registry.register(BuiltInRegistries.MOB_EFFECT,
                Identifier.fromNamespaceAndPath("champions", "wound"), WOUND);
        Registry.register(BuiltInRegistries.MOB_EFFECT,
                Identifier.fromNamespaceAndPath("champions", "paralysis"), PARALYSIS);
        ChampionEffects.register(() -> WOUND, () -> PARALYSIS);
    }
}
