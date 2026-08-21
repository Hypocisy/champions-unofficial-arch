package top.theillusivec4.champions.api.affix;

import net.minecraft.nbt.CompoundTag;

/**
 * A live instance of an {@link AffixType} attached to a specific champion.
 *
 * <p>There is exactly one {@code AffixInstance} per active affix per champion. It holds:</p>
 * <ul>
 *   <li>A reference to the singleton {@link AffixType} (defines behavior)</li>
 *   <li>A {@code strength} value (1–5, scales affix power)</li>
 *   <li>A mutable {@link IAffixData} object (per-champion runtime state)</li>
 * </ul>
 *
 * <p>Instances are created by the build strategy on champion spawn, or by
 * {@code PhaseEffect} during combat. They are destroyed when the champion dies
 * or the affix is explicitly removed.</p>
 *
 * <h3>Creating an instance</h3>
 * <pre>{@code
 * // Normal creation — data initialised by the type's createData()
 * AffixInstance instance = new AffixInstance(affixType, strength);
 *
 * // Deserialization — data object provided externally after read from NBT
 * AffixInstance instance = AffixInstance.load(affixType, tag);
 * }</pre>
 *
 * <h3>Accessing per-champion data</h3>
 * <pre>{@code
 * // Inside an AffixType subclass — always use getData() for type safety
 * MyData data = getData(instance);
 *
 * // Outside — only IAffixData is visible, which is intentional
 * instance.data(); // → IAffixData
 * }</pre>
 */
public final class AffixInstance {

    private final AffixType<?> type;
    private final int strength;
    private final IAffixData data;

    /**
     * Create a new instance. Data is initialised by {@link AffixType#createData()}.
     *
     * @param type     the affix type (registry singleton)
     * @param strength strength level, 1–5
     */
    public AffixInstance(AffixType<?> type, int strength) {
        this.type = type;
        this.strength = strength;
        this.data = type.createData();
    }

    /**
     * Create a new instance with an externally supplied data object.
     * Used during deserialization — see {@link #load(AffixType, CompoundTag)}.
     */
    private AffixInstance(AffixType<?> type, int strength, IAffixData data) {
        this.type = type;
        this.strength = strength;
        this.data = data;
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public AffixType<?> type() {
        return type;
    }

    public int strength() {
        return strength;
    }

    /**
     * The live per-champion state. The concrete type is only visible inside the owning
     * {@link AffixType} via {@link AffixType#getData(AffixInstance)}.
     */
    public IAffixData data() {
        return data;
    }

    // ── Serialization ─────────────────────────────────────────────────────────

    /**
     * Serialize this instance's mutable state to NBT.
     *
     * <p>The affix type id is <em>not</em> stored here — the champion attachment stores the
     * ordered list of affix ids separately, so type and data can be reconstructed independently
     * and correlated by position.</p>
     */
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("strength", strength);
        data.write(tag);
        return tag;
    }

    /**
     * Deserialize an instance from NBT.
     *
     * @param type the affix type, resolved from the registry by the champion attachment
     * @param tag  the tag produced by {@link #save()}
     */
    public static AffixInstance load(AffixType<?> type, CompoundTag tag) {
        int strength = tag.getInt("strength");
        IAffixData data = type.createData();
        data.read(tag);
        return new AffixInstance(type, strength, data);
    }

    // ── Client-side reconstruction ────────────────────────────────────────────

    /**
     * Create a client-side instance from sync data.
     * Data is default-initialised — the client does not need runtime state.
     *
     * @param type     resolved from the registry
     * @param strength received from the sync packet
     */
    public static AffixInstance fromSync(AffixType<?> type, int strength) {
        return new AffixInstance(type, strength);
    }

    @Override
    public String toString() {
        return "AffixInstance[type=" + type.getClass().getSimpleName()
                + ", strength=" + strength + "]";
    }
}
