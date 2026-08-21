package top.theillusivec4.champions.common.command;

import com.google.gson.JsonObject;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

public class AffixArgumentInfo implements ArgumentTypeInfo<AffixArgumentType, AffixArgumentInfo.Template> {

  @Override
  public void serializeToNetwork(@NotNull Template template, @NotNull FriendlyByteBuf buffer) {
  }

  @Override
  public @NotNull Template deserializeFromNetwork(@NotNull FriendlyByteBuf buffer) {
    return new Template();
  }

  @Override
  public void serializeToJson(@NotNull Template template, @NotNull JsonObject json) {
  }

  @Override
  public @NotNull Template unpack(@NotNull AffixArgumentType argument) {
    return new Template();
  }

  public class Template implements ArgumentTypeInfo.Template<AffixArgumentType> {
    @Override
    public @NotNull AffixArgumentType instantiate(@NotNull CommandBuildContext context) {
      return new AffixArgumentType();
    }

    @Override
    public @NotNull ArgumentTypeInfo<AffixArgumentType, ?> type() {
      return AffixArgumentInfo.this;
    }
  }
}
