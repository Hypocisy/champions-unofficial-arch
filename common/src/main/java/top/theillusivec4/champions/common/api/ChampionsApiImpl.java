package top.theillusivec4.champions.common.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import top.theillusivec4.champions.api.ChampionsApi;
import top.theillusivec4.champions.api.affix.AffixType;
import top.theillusivec4.champions.api.champion.Champion;
import top.theillusivec4.champions.api.champion.ChampionTier;
import top.theillusivec4.champions.common.registry.AffixTypeRegistry;
import top.theillusivec4.champions.common.registry.TierRegistry;
import top.theillusivec4.champions.platform.ChampionAttachmentProvider;

import java.util.Collection;
import java.util.Optional;

/**
 * Concrete implementation of {@link ChampionsApi}.
 *
 * <p>Registered at startup via {@link ChampionsApi#register(ChampionsApi)}.
 * Dependencies ({@link AffixTypeRegistry}, {@link TierRegistry},
 * {@link ChampionAttachmentProvider}) are injected at construction time,
 * keeping this class testable and platform-agnostic.</p>
 *
 * <p>This class is internal — third-party mods should only reference {@link ChampionsApi}.</p>
 */
public final class ChampionsApiImpl implements ChampionsApi {

    private final AffixTypeRegistry affixTypeRegistry;
    private final TierRegistry tierRegistry;
    private final ChampionAttachmentProvider attachmentProvider;

    public ChampionsApiImpl(
            AffixTypeRegistry affixTypeRegistry,
            TierRegistry tierRegistry,
            ChampionAttachmentProvider attachmentProvider
    ) {
        this.affixTypeRegistry = affixTypeRegistry;
        this.tierRegistry = tierRegistry;
        this.attachmentProvider = attachmentProvider;
    }

    // ── Champion queries ──────────────────────────────────────────────────────

    @Override
    public Optional<Champion> getChampion(LivingEntity entity) {
        return attachmentProvider.get(entity);
    }

    @Override
    public boolean isChampion(LivingEntity entity) {
        return attachmentProvider.has(entity);
    }

    // ── Affix type registry ───────────────────────────────────────────────────

    @Override
    public Optional<AffixType<?>> getAffixType(ResourceLocation id) {
        return affixTypeRegistry.get(id);
    }

    @Override
    public Optional<ResourceLocation> getAffixTypeId(AffixType<?> type) {
        return affixTypeRegistry.getId(type);
    }

    @Override
    public Collection<AffixType<?>> getAffixTypes() {
        return affixTypeRegistry.getAll();
    }

    // ── Tier registry ─────────────────────────────────────────────────────────

    @Override
    public Optional<ChampionTier> getTier(ResourceLocation id) {
        return tierRegistry.get(id);
    }

    @Override
    public Optional<ChampionTier> getTierByLevel(int level) {
        return tierRegistry.getByLevel(level);
    }

    @Override
    public Collection<ChampionTier> getTiers() {
        return tierRegistry.getAll();
    }

    // ── Registration helper ───────────────────────────────────────────────────

    /**
     * Convenience: construct and register this implementation in one call.
     * Called from the mod's entry point after all registries are ready.
     *
     * <pre>{@code
     * ChampionsApiImpl.registerWith(
     *     new NeoForgeAffixTypeRegistry(),
     *     new DatapackTierRegistry(),
     *     new NeoForgeAttachmentProvider()
     * );
     * }</pre>
     */
    public static void registerWith(
            AffixTypeRegistry affixTypeRegistry,
            TierRegistry tierRegistry,
            ChampionAttachmentProvider attachmentProvider
    ) {
        ChampionsApi.register(new ChampionsApiImpl(
                affixTypeRegistry,
                tierRegistry,
                attachmentProvider
        ));
    }
}
