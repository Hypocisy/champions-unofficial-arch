package top.theillusivec4.champions.common.api;

import top.theillusivec4.champions.common.champion.ChampionBuilder;
import top.theillusivec4.champions.common.data.ArchetypeDataLoader;
import top.theillusivec4.champions.common.data.AttributesModifierDataLoader;
import top.theillusivec4.champions.common.data.TierDataLoader;
import top.theillusivec4.champions.common.registry.AffixTypeRegistry;
import top.theillusivec4.champions.platform.ChampionAttachmentProvider;

/**
 * Holds shared singletons and wires the platform-agnostic part of startup.
 */
public final class ChampionsRegistries {

    public static final TierDataLoader TIERS = new TierDataLoader();
    public static final ArchetypeDataLoader ARCHETYPES = new ArchetypeDataLoader();
    public static final AttributesModifierDataLoader MODIFIERS = new AttributesModifierDataLoader();

    private static ChampionBuilder builder;

    private ChampionsRegistries() {
    }

    public static void bootstrapCommon(
            AffixTypeRegistry affixTypes,
            ChampionAttachmentProvider attachments
    ) {
        ChampionsApiImpl.registerWith(affixTypes, TIERS, attachments);
        builder = new ChampionBuilder(attachments, ARCHETYPES);
    }

    public static TierDataLoader tiers() {
        return TIERS;
    }

    public static ArchetypeDataLoader archetypes() {
        return ARCHETYPES;
    }

    public static AttributesModifierDataLoader modifiers() {
        return MODIFIERS;
    }

    public static ChampionBuilder builder() {
        return builder;
    }
}
