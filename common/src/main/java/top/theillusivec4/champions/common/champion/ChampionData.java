package top.theillusivec4.champions.common.champion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

/**
 * The serializable snapshot of a champion's state.
 *
 * <p>This is what gets written to disk (NBT) and synced between sides. It is deliberately
 * kept flat and codec-friendly — no live objects, no platform types.
 * The live representation ({@link ChampionView}) is built from this on demand.</p>
 *
 * <p>Two structural choices worth noting:</p>
 * <ul>
 *   <li>{@code baseAffixes} — affixes assigned at spawn. This is the ground truth for
 *       serialization. Live affixes (including phase additions) are derived, not stored.</li>
 *   <li>{@code triggeredPhases} — which phase ids have already fired. Combined with the
 *       archetype definition, the full live affix list can be reconstructed without storing
 *       it explicitly.</li>
 * </ul>
 */
public record ChampionData(
        ResourceLocation tierId,
        List<AffixEntry> baseAffixes,
        List<ResourceLocation> triggeredPhases,
        Optional<ResourceLocation> archetypeId
) {

    public static final ChampionData EMPTY = new ChampionData(
            new ResourceLocation("minecraft", "tier_1"),
            List.of(),
            List.of(),
            Optional.empty()
    );

    // ── Codec ─────────────────────────────────────────────────────────────────

    public static final Codec<ChampionData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ResourceLocation.CODEC
                    .fieldOf("tier")
                    .forGetter(ChampionData::tierId),
            AffixEntry.CODEC.listOf()
                    .optionalFieldOf("base_affixes", List.of())
                    .forGetter(ChampionData::baseAffixes),
            ResourceLocation.CODEC.listOf()
                    .optionalFieldOf("triggered_phases", List.of())
                    .forGetter(ChampionData::triggeredPhases),
            ResourceLocation.CODEC
                    .optionalFieldOf("archetype")
                    .forGetter(ChampionData::archetypeId)
    ).apply(inst, ChampionData::new));

    // ── Builder ───────────────────────────────────────────────────────────────

    public ChampionData withAffixes(List<AffixEntry> newAffixes) {
        return new ChampionData(tierId, newAffixes, triggeredPhases, archetypeId);
    }

    public ChampionData withTriggeredPhases(List<ResourceLocation> newPhases) {
        return new ChampionData(tierId, baseAffixes, newPhases, archetypeId);
    }

    // ── AffixEntry ────────────────────────────────────────────────────────────

    /**
     * Serialized form of a single affix instance.
     * Holds the type id, strength, and the affix's own per-champion NBT data.
     */
    public record AffixEntry(
            ResourceLocation typeId,
            int strength,
            CompoundTag data
    ) {

        public static final Codec<AffixEntry> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                ResourceLocation.CODEC
                        .fieldOf("type")
                        .forGetter(AffixEntry::typeId),
                Codec.INT
                        .optionalFieldOf("strength", 1)
                        .forGetter(AffixEntry::strength),
                CompoundTag.CODEC
                        .optionalFieldOf("data", new CompoundTag())
                        .forGetter(AffixEntry::data)
        ).apply(inst, AffixEntry::new));
    }
}
