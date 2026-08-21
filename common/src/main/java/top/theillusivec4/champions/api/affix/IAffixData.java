package top.theillusivec4.champions.api.affix;

import net.minecraft.nbt.CompoundTag;

/**
 * Per-champion state for an affix.
 *
 * <p>Each {@link AffixInstance} holds one {@code IAffixData} object that lives as long as the
 * instance does. Implementations are mutable — handler lambdas write directly into them.</p>
 *
 * <p>Affix types that carry no per-champion state should use {@link EmptyAffixData#INSTANCE}
 * as their data type and do not need to override {@link AffixType#createData()}.</p>
 */
public interface IAffixData {

    /**
     * Serialize this data into {@code tag}.
     * Called when the champion attachment is written to disk.
     */
    void write(CompoundTag tag);

    /**
     * Deserialize this data from {@code tag}.
     * Called when the champion attachment is read from disk.
     */
    void read(CompoundTag tag);
}
