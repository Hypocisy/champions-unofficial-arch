package top.theillusivec4.champions.neoforge.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.champions.common.network.ChampionSyncData;
import top.theillusivec4.champions.neoforge.ChampionsNeoForge;

// ── ChampionSyncPacket ────────────────────────────────────────────────────────

/**
 * Sent server → client when a champion's live affix list changes.
 * Carries the entity id and the full {@link ChampionSyncData} snapshot.
 */
public record ChampionSyncPacket(
        int entityId,
        ChampionSyncData data
) implements CustomPacketPayload {

    public static final Type<ChampionSyncPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ChampionsNeoForge.MOD_ID, "champion_sync")
    );

    public static final StreamCodec<FriendlyByteBuf, ChampionSyncPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, ChampionSyncPacket::entityId,
                    ChampionSyncData.STREAM_CODEC, ChampionSyncPacket::data,
                    ChampionSyncPacket::new
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
