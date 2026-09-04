package top.theillusivec4.champions.common.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.champions.api.ChampionsApi;
import top.theillusivec4.champions.api.affix.AffixInstance;
import top.theillusivec4.champions.api.affix.IAffixClientSync;
import top.theillusivec4.champions.api.champion.Champion;

import java.util.ArrayList;
import java.util.List;

/**
 * Wire format for champion state sent from server to client.
 *
 * <p>Contains only what the client needs for rendering: tier id and
 * the live affix list (type id + strength + optional extra data).
 * Per-champion runtime state ({@code IAffixData}) is never sent.</p>
 *
 * <p>Encoding uses plain {@link FriendlyByteBuf} read/write so it works
 * identically on Forge and Fabric 1.20.1 networking.</p>
 */
public record ChampionSyncData(
        ResourceLocation tierId,
        List<AffixSyncEntry> affixes
) {

    // ── Serialization ─────────────────────────────────────────────────────────

    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(tierId);
        buf.writeVarInt(affixes.size());
        for (AffixSyncEntry entry : affixes) {
            entry.encode(buf);
        }
    }

    public static ChampionSyncData decode(FriendlyByteBuf buf) {
        ResourceLocation tierId = buf.readResourceLocation();
        int size = buf.readVarInt();
        List<AffixSyncEntry> affixes = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            affixes.add(AffixSyncEntry.decode(buf));
        }
        return new ChampionSyncData(tierId, affixes);
    }

    // ── Factory ───────────────────────────────────────────────────────────────

    /**
     * Build sync data from a live server-side champion.
     * Collects affix entries and asks each type for any extra client data.
     */
    public static ChampionSyncData from(Champion champion) {
        List<AffixSyncEntry> entries = champion.affixes().stream()
                .map(ChampionSyncData::toEntry)
                .toList();
        return new ChampionSyncData(champion.tier().id(), entries);
    }

    private static AffixSyncEntry toEntry(AffixInstance instance) {
        ResourceLocation typeId = ChampionsApi.get()
                .getAffixTypeId(instance.type())
                .orElseThrow(() -> new IllegalStateException(
                        "Affix type not in registry: " + instance.type().getClass().getName()
                ));

        CompoundTag clientData = null;
        if (instance.type() instanceof IAffixClientSync sync) {
            clientData = sync.writeClientData(instance);
        }

        return new AffixSyncEntry(typeId, instance.strength(), clientData);
    }

    // ── AffixSyncEntry ────────────────────────────────────────────────────────

    /**
     * Wire representation of one affix instance.
     * {@code clientData} is null for affixes that do not implement {@link IAffixClientSync}.
     */
    public record AffixSyncEntry(
            ResourceLocation typeId,
            int strength,
            @Nullable CompoundTag clientData
    ) {

        public void encode(FriendlyByteBuf buf) {
            buf.writeResourceLocation(typeId);
            buf.writeVarInt(strength);
            // writeNbt(null) writes a TAG_END byte that readNbt() reads back as null
            buf.writeNbt(clientData);
        }

        public static AffixSyncEntry decode(FriendlyByteBuf buf) {
            return new AffixSyncEntry(
                    buf.readResourceLocation(),
                    buf.readVarInt(),
                    buf.readNbt()
            );
        }
    }
}
