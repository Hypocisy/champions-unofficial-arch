package top.theillusivec4.champions.api.affix;

import net.minecraft.nbt.CompoundTag;

/**
 * Optional interface for {@link AffixType} subclasses that need to push
 * extra data to the client beyond type id and strength.
 *
 * <p>Most affixes do not need this — the client only needs type + strength for rendering.
 * Implement this only when client-side visuals depend on server-side runtime state,
 * e.g. Arctic needing to tell the client which blocks are frozen.</p>
 *
 * <p>The extra data is included as an optional field in {@code AffixSyncEntry}
 * and is null for affixes that do not implement this interface.</p>
 */
public interface IAffixClientSync {

    /**
     * Write any client-relevant state from {@code instance} into a tag.
     * Called server-side before the sync packet is assembled.
     */
    CompoundTag writeClientData(AffixInstance instance);

    /**
     * Apply the tag received from the server to the local client-side {@code instance}.
     * Called client-side when the sync packet arrives.
     */
    void readClientData(AffixInstance instance, CompoundTag tag);
}
