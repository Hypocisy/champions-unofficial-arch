package top.theillusivec4.champions.neoforge.registry;


import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModParticleTypes {

  private static final DeferredRegister<ParticleType<?>> TYPES =
      DeferredRegister.create(Registries.PARTICLE_TYPE, "champions");

  public static final RegistryObject<SimpleParticleType> RANK =
      TYPES.register("rank", () -> new SimpleParticleType(true));

  public static void register(IEventBus bus) {
    TYPES.register(bus);
  }
}