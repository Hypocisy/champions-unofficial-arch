package top.theillusivec4.champions.common.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.champions.api.ChampionsApi;
import top.theillusivec4.champions.api.affix.AffixInstance;
import top.theillusivec4.champions.api.affix.IAffixClientSync;
import top.theillusivec4.champions.api.champion.Champion;

import java.util.List;
import java.util.Optional;

/**
 * Wire format for champion state sent from server to client.
 *
 * <p>Contains only what the client needs for rendering: tier id and
 * the live affix list (type id + strength + optional extra data).
 * Per-champion runtime state ({@code IAffixData}) is never sent.</p>
 *
 * <p>Both the full packet and the entry list are stream-codec friendly
 * so they work identically on NeoForge and Fabric networking APIs.</p>
 */
public record ChampionSyncData(
        ResourceLocation tierId,
        List<AffixSyncEntry> affixes
) {

    // ── Stream codec ──────────────────────────────────────────────────────────

    public static final StreamCodec<FriendlyByteBuf, ChampionSyncData> STREAM_CODEC =
            StreamCodec.composite(
                    ResourceLocation.STREAM_CODEC, ChampionSyncData::tierId,
                    AffixSyncEntry.STREAM_CODEC.apply(ByteBufCodecs.list()), ChampionSyncData::affixes,
                    ChampionSyncData::new
            );

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

        public static final StreamCodec<FriendlyByteBuf, AffixSyncEntry> STREAM_CODEC =
                StreamCodec.composite(
                        ResourceLocation.STREAM_CODEC, AffixSyncEntry::typeId,
                        ByteBufCodecs.VAR_INT, AffixSyncEntry::strength,
                        ByteBufCodecs.optional(ByteBufCodecs.COMPOUND_TAG)
                                .map(opt -> opt.orElse(new CompoundTag()), Optional::ofNullable), // Optional<CompoundTag> ↔ @Nullable
                        AffixSyncEntry::clientData,
                        AffixSyncEntry::new
                );
    }
}
