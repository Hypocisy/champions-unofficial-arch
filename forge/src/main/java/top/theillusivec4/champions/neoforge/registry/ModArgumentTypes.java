package top.theillusivec4.champions.neoforge.registry;

import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import top.theillusivec4.champions.common.command.AffixArgumentInfo;
import top.theillusivec4.champions.common.command.AffixArgumentType;
import top.theillusivec4.champions.neoforge.ChampionsNeoForge;


public class ModArgumentTypes {
  private static final DeferredRegister<ArgumentTypeInfo<?, ?>> ARGUMENT_TYPES = DeferredRegister.create(Registries.COMMAND_ARGUMENT_TYPE, ChampionsNeoForge.MOD_ID);

  public static final RegistryObject<AffixArgumentInfo> AFFIX_ARGUMENT_TYPE = ARGUMENT_TYPES.register("affixes", () -> ArgumentTypeInfos.registerByClass(AffixArgumentType.class, new AffixArgumentInfo()));
  public static void register(IEventBus bus) {
    ARGUMENT_TYPES.register(bus);
  }
}
