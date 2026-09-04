package top.theillusivec4.champions.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import top.theillusivec4.champions.common.utils.Utils;

/**
 * S2C packet telling the client to clear champion data for a specific entity.
 * Sent by {@link PacketHandler#clearChampionForTrackers} after a /champions remove.
 */
public record ChampionClearPacket(int entityId) {

    public static final ResourceLocation ID = Utils.key("champion_clear");

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
    }

    public static ChampionClearPacket decode(FriendlyByteBuf buf) {
        return new ChampionClearPacket(buf.readVarInt());
    }
}
