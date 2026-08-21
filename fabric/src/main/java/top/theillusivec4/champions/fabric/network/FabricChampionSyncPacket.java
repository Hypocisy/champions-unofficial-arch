package top.theillusivec4.champions.fabric.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.champions.common.network.ChampionSyncData;

public record FabricChampionSyncPacket(
    int entityId,
    ChampionSyncData data
) implements CustomPacketPayload {

  public static final Type<FabricChampionSyncPacket> TYPE = new Type<>(
      ResourceLocation.fromNamespaceAndPath("champions", "champion_sync")
  );

  public static final StreamCodec<FriendlyByteBuf, FabricChampionSyncPacket> STREAM_CODEC =
      StreamCodec.composite(
          ByteBufCodecs.VAR_INT,         FabricChampionSyncPacket::entityId,
          ChampionSyncData.STREAM_CODEC, FabricChampionSyncPacket::data,
          FabricChampionSyncPacket::new
      );

  @Override
  public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }
}