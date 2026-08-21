package top.theillusivec4.champions.neoforge.registry;

import net.minecraft.world.effect.MobEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import top.theillusivec4.champions.common.effect.ChampionEffects;
import top.theillusivec4.champions.common.effect.ParalysisEffect;
import top.theillusivec4.champions.common.effect.WoundEffect;

public final class ModMobEffects {

    private static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, "champions");

    public static final DeferredHolder<MobEffect, WoundEffect> WOUND =
            EFFECTS.register("wound", WoundEffect::new);

    public static final DeferredHolder<MobEffect, ParalysisEffect> PARALYSIS =
            EFFECTS.register("paralysis", ParalysisEffect::new);

    public static void register(IEventBus bus) {
        EFFECTS.register(bus);
    }

    /**
     * Wire the common {@link ChampionEffects} holder.
     * Call after the registry is frozen (i.e. after mod load).
     */
    public static void wireCommon() {
        ChampionEffects.register(WOUND::get, PARALYSIS::get);
    }
}
