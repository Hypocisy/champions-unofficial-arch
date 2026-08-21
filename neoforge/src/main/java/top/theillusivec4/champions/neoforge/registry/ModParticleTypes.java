package top.theillusivec4.champions.neoforge.registry;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModParticleTypes {

  private static final DeferredRegister<ParticleType<?>> TYPES =
      DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, "champions");

  public static final DeferredHolder<ParticleType<?>, SimpleParticleType> RANK =
      TYPES.register("rank", () -> new SimpleParticleType(true));

  public static void register(IEventBus bus) {
    TYPES.register(bus);
  }
}