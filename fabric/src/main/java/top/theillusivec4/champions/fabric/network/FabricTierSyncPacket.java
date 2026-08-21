package top.theillusivec4.champions.fabric.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.champions.api.champion.ChampionTier;

import java.util.ArrayList;
import java.util.List;

public record FabricTierSyncPacket(List<TierEntry> tiers) implements CustomPacketPayload {

  public static final Type<FabricTierSyncPacket> TYPE = new Type<>(
      ResourceLocation.fromNamespaceAndPath("champions", "tier_sync")
  );

  public static final StreamCodec<FriendlyByteBuf, TierEntry> ENTRY_CODEC =
      StreamCodec.composite(
          ResourceLocation.STREAM_CODEC, TierEntry::id,
          ByteBufCodecs.VAR_INT,         TierEntry::level,
          ByteBufCodecs.VAR_INT,         TierEntry::color,
          TierEntry::new
      );

  public static final StreamCodec<FriendlyByteBuf, FabricTierSyncPacket> STREAM_CODEC =
      ENTRY_CODEC.apply(ByteBufCodecs.list())
          .map(FabricTierSyncPacket::new, FabricTierSyncPacket::tiers);

  @Override
  public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }

  public static FabricTierSyncPacket from(Iterable<ChampionTier> tiers) {
    List<TierEntry> entries = new ArrayList<>();
    tiers.forEach(t -> entries.add(new TierEntry(t.id(), t.level(), t.display().color())));
    return new FabricTierSyncPacket(entries);
  }

  public record TierEntry(ResourceLocation id, int level, int color) {}
}