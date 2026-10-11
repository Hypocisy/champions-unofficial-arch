package top.theillusivec4.champions.neoforge.datagen;

import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import top.theillusivec4.champions.common.registry.ModDamageTypes;

public class ModDatapackProvider {
public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
        .add(Registries.DAMAGE_TYPE,  ModDamageTypes::bootstrap);

}
