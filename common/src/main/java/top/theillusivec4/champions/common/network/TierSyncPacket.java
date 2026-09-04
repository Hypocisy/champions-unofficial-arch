package top.theillusivec4.champions.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import top.theillusivec4.champions.api.champion.ChampionTier;
import top.theillusivec4.champions.common.utils.Utils;

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
public record TierSyncPacket(List<TierEntry> tiers) {

    public static final ResourceLocation ID = Utils.key("tier_sync");

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(tiers.size());
        for (TierEntry entry : tiers) {
            entry.encode(buf);
        }
    }

    public static TierSyncPacket decode(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<TierEntry> tiers = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            tiers.add(TierEntry.decode(buf));
        }
        return new TierSyncPacket(tiers);
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

        public void encode(FriendlyByteBuf buf) {
            buf.writeResourceLocation(id);
            buf.writeVarInt(level);
            buf.writeVarInt(color);
        }

        public static TierEntry decode(FriendlyByteBuf buf) {
            return new TierEntry(buf.readResourceLocation(), buf.readVarInt(), buf.readVarInt());
        }
    }
}
