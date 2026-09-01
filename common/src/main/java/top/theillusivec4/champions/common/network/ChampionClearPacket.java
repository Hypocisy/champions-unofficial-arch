package top.theillusivec4.champions.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * S2C packet telling the client to clear champion data for a specific entity.
 * Sent by {@link PacketHandler#clearChampionForTrackers} after a /champions remove.
 */
public record ChampionClearPacket(int entityId) implements CustomPacketPayload {

    public static final Type<ChampionClearPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath("champions", "champion_clear"));

    public static final StreamCodec<ByteBuf, ChampionClearPacket> STREAM_CODEC =
            ByteBufCodecs.VAR_INT.map(ChampionClearPacket::new, ChampionClearPacket::entityId);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
