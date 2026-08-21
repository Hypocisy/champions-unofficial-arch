package top.theillusivec4.champions.api.affix;

import net.minecraft.nbt.CompoundTag;

/**
 * Default {@link IAffixData} for affixes that carry no per-champion state.
 *
 * <p>Use {@link #INSTANCE} as the data type. {@link AffixType#createData()} returns this
 * by default, so stateless affix types do not need to override it.</p>
 */
public enum EmptyAffixData implements IAffixData {
    INSTANCE;

    @Override
    public void write(CompoundTag tag) {
        // nothing to serialize
    }

    @Override
    public void read(CompoundTag tag) {
        // nothing to deserialize
    }
}
