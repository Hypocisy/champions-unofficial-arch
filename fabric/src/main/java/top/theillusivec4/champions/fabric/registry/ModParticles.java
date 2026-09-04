package top.theillusivec4.champions.fabric.registry;
import top.theillusivec4.champions.common.utils.Utils;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

public class ModParticles {

    public static final SimpleParticleType MAGIC =
            FabricParticleTypes.simple();

    public static void register() {
        Registry.register(
                BuiltInRegistries.PARTICLE_TYPE,
                Utils.key("rank"),
                MAGIC
        );
    }
}