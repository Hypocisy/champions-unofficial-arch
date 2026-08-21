package top.theillusivec4.champions.neoforge.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.champions.api.champion.ChampionTier;
import top.theillusivec4.champions.neoforge.ChampionsNeoForge;

import java.util.ArrayList;
import java.util.List;

/**
 * Sent server → client on login and after every datapack reload.
 * Carries the full tier registry so the client can resolve tier ids received
 * in subsequent {@link ChampionSyncPacket}s.
 *
 * <p>Only {@code id}, {@code level}, and {@code display.color} are synced — the icon
 * texture path is a client-side concern and does not need to round-trip.</p>
 */
public record TierSyncPacket(List<TierEntry> tiers) implements CustomPacketPayload {

    public static final Type<TierSyncPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ChampionsNeoForge.MOD_ID, "tier_sync")
    );

    public static final StreamCodec<FriendlyByteBuf, TierEntry> ENTRY_CODEC =
            StreamCodec.composite(
                    ResourceLocation.STREAM_CODEC, TierEntry::id,
                    ByteBufCodecs.VAR_INT, TierEntry::level,
                    ByteBufCodecs.VAR_INT, TierEntry::color,
                    TierEntry::new
            );

    public static final StreamCodec<FriendlyByteBuf, TierSyncPacket> STREAM_CODEC =
            ENTRY_CODEC.apply(ByteBufCodecs.list()).map(TierSyncPacket::new, TierSyncPacket::tiers);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // ── Factory ───────────────────────────────────────────────────────────────

    public static TierSyncPacket from(Iterable<ChampionTier> tiers) {
        List<TierEntry> entries = new ArrayList<>();
        tiers.forEach(tier -> entries.add(
                new TierEntry(tier.id(), tier.level(), tier.display().color())
        ));
        return new TierSyncPacket(entries);
    }

    // ── Entry ─────────────────────────────────────────────────────────────────

    public record TierEntry(ResourceLocation id, int level, int color) {
    }
}
