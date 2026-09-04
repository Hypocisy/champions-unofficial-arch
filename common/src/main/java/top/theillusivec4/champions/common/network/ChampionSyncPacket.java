package top.theillusivec4.champions.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import top.theillusivec4.champions.common.utils.Utils;

/**
 * Sent server → client when a champion's live affix list changes.
 * Carries the entity id and the full {@link ChampionSyncData} snapshot.
 */
public record ChampionSyncPacket(
        int entityId,
        ChampionSyncData data
) {

    public static final ResourceLocation ID = Utils.key("champion_sync");

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        data.encode(buf);
    }

    public static ChampionSyncPacket decode(FriendlyByteBuf buf) {
        int entityId = buf.readVarInt();
        ChampionSyncData data = ChampionSyncData.decode(buf);
        return new ChampionSyncPacket(entityId, data);
    }
}
