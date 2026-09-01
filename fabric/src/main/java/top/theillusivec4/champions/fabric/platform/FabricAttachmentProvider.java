package top.theillusivec4.champions.fabric.platform;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import top.theillusivec4.champions.api.ChampionsApi;
import top.theillusivec4.champions.api.affix.AffixInstance;
import top.theillusivec4.champions.api.champion.Champion;
import top.theillusivec4.champions.api.champion.ChampionTier;
import top.theillusivec4.champions.common.champion.ChampionData;
import top.theillusivec4.champions.common.champion.ChampionView;
import top.theillusivec4.champions.common.network.ChampionSyncData;
import top.theillusivec4.champions.common.network.PacketHandler;
import top.theillusivec4.champions.common.phase.PhaseProcessor;
import top.theillusivec4.champions.platform.ChampionAttachmentProvider;

import java.util.*;

/**
 * Fabric implementation of {@link ChampionAttachmentProvider}.
 *
 * <p>Uses Cardinal Components ({@link FabricChampionComponent}) to store
 * {@link ChampionData} on {@link LivingEntity} instances.
 * The API surface is identical to the NeoForge provider — all champion logic
 * lives in {@link ChampionView} and is shared.</p>
 *
 * <p>Server views are cached per entity in a plain {@link HashMap} with explicit
 * invalidation (see the NeoForge provider for the rationale — rebuilding on every
 * tick re-resolves affixes and re-applies phase effects; a {@code WeakHashMap} would
 * leak because the value holds a strong reference back to the key entity). The cache
 * is invalidated on {@link #setServer}, {@link #remove}, and when the entity is
 * unloaded from its chunk.</p>
 */
public final class FabricAttachmentProvider implements ChampionAttachmentProvider {

    /** Live server views, keyed by entity identity. Server-thread only. */
    private final Map<LivingEntity, ChampionView.Server> serverViewCache =
            new HashMap<>();

    // ── ChampionAttachmentProvider ────────────────────────────────────────────

    @Override
    public Optional<Champion.Server> getServer(LivingEntity entity) {
        Optional<FabricChampionComponent> component = FabricChampionComponent.SERVER.maybeGet(entity)
                .filter(FabricChampionComponent::isPresent);
        if (component.isEmpty()) return Optional.empty();

        ChampionView.Server cached = serverViewCache.get(entity);
        if (cached != null && entity.isRemoved()) {
            // Defensive: an entity that has already left the world may still be queried
            // during teardown. Never keep caching a view for it.
            serverViewCache.remove(entity);
            cached = null;
        }
        if (cached != null) return Optional.of(cached);

        Optional<ChampionView.Server> built = buildServerView(entity, component.get());
        built.ifPresent(view -> serverViewCache.put(entity, view));
        return built.map(v -> v);
    }

    @Override
    public Optional<Champion.Client> getClient(LivingEntity entity) {
        return FabricChampionComponent.CLIENT.maybeGet(entity)
                .filter(FabricChampionComponent::isPresent)
                .flatMap(component -> buildClientView(entity, component));
    }

    @Override
    public Optional<Champion> get(LivingEntity entity) {
        if (entity.level().isClientSide()) {
            return getClient(entity).map(c -> c);
        }
        return getServer(entity).map(s -> s);
    }

    @Override
    public boolean has(LivingEntity entity) {
        if (entity.level().isClientSide()) {
            return FabricChampionComponent.CLIENT.maybeGet(entity)
                    .map(FabricChampionComponent::isPresent)
                    .orElse(false);
        }
        return FabricChampionComponent.SERVER.maybeGet(entity)
                .map(FabricChampionComponent::isPresent)
                .orElse(false);
    }

    // ── Write ─────────────────────────────────────────────────────────────────

    public void setServer(LivingEntity entity, ChampionData data) {
        serverViewCache.remove(entity);
        FabricChampionComponent.SERVER.maybeGet(entity)
                .ifPresent(c -> c.setData(data));
    }

    @Override
    public void persistServer(LivingEntity entity, ChampionData data) {
        // Write-through without invalidating the cached view.
        FabricChampionComponent.SERVER.maybeGet(entity)
                .ifPresent(c -> c.setData(data));
    }

    public void setClient(LivingEntity entity, ChampionData data) {
        FabricChampionComponent.CLIENT.maybeGet(entity)
                .ifPresent(c -> c.setData(data));
    }

    @Override
    public void remove(LivingEntity entity) {
        serverViewCache.remove(entity);
        FabricChampionComponent.SERVER.maybeGet(entity)
                .ifPresent(c -> c.setData(ChampionData.EMPTY));
        FabricChampionComponent.CLIENT.maybeGet(entity)
                .ifPresent(c -> c.setData(ChampionData.EMPTY));
    }

    /**
     * Invalidate the cached view when an entity is unloaded from its chunk (death,
     * chunk unload, dimension change). Registered from the Fabric entrypoint via
     * {@code ServerEntityEvents.ENTITY_UNLOAD}. Keeps the cache size proportional
     * to the number of live champions.
     */
    public void onEntityUnload(LivingEntity entity) {
        serverViewCache.remove(entity);
    }

    // ── View construction ─────────────────────────────────────────────────────

    private Optional<ChampionView.Server> buildServerView(
            LivingEntity entity,
            FabricChampionComponent component
    ) {
        ChampionData data = component.getData();
        return ChampionsApi.get().getTier(data.tierId()).map(tier -> {
            List<AffixInstance> baseAffixes = resolveAffixes(data);
            List<AffixInstance> liveAffixes = rebuildLiveAffixes(baseAffixes, data, tier);
            // Use a single-element array so the sync lambda can capture the server
            // reference after construction — avoids capturing the pre-copy liveAffixes list.
            ChampionView.Server[] ref = { null };

            ChampionView.Server server = new ChampionView.Server(
                    entity, tier, baseAffixes, liveAffixes,
                    updated -> persistServer(entity, updated),
                    () -> {
                        PacketHandler.Holder.get()
                                .syncChampionToTrackers(entity,
                                        ChampionSyncData.from(ref[0]));
                        // Intentionally does NOT call restoreTriggeredPhases here:
                        // the sync callback runs on every persist (affix add/remove, phase
                        // trigger), and re-applying already-fired phase effects each time would
                        // stack attribute modifiers and refresh effects. Silent restore happens
                        // exactly once, on view construction, in buildServerView.
                    },
                    data.triggeredPhases(),
                    data.archetypeId());

            // Restore effects of phases triggered in a previous session exactly once, before
            // the entity joins the world. Deliberately kept out of the sync callback above.
            PhaseProcessor.restoreTriggeredPhases(server);

            ref[0] = server;
            return server;
        });
    }

    private Optional<ChampionView.Client> buildClientView(
            LivingEntity entity,
            FabricChampionComponent component
    ) {
        ChampionData data = component.getData();
        return ChampionsApi.get().getTier(data.tierId()).map(tier ->
                new ChampionView.Client(entity, tier, resolveAffixes(data))
        );
    }

    // ── Shared helpers ────────────────────────────────────────────────────────

    private static List<AffixInstance> resolveAffixes(ChampionData data) {
        return data.baseAffixes().stream()
                .flatMap(entry -> ChampionsApi.get().getAffixType(entry.typeId())
                        .map(type -> {
                            CompoundTag tag = entry.data().copy();
                            tag.putInt("strength", entry.strength());
                            return AffixInstance.load(type, tag);
                        })
                        .stream())
                .toList();
    }

    /**
     * Rebuild the live affix list from base affixes + already-triggered phase effects.
     * Phase effects that were triggered in a previous session are re-applied via
     * {@link PhaseProcessor#restoreTriggeredPhases}
     * in the server view's init callback. This method provides the initial list.
     */
    private static List<AffixInstance> rebuildLiveAffixes(
            List<AffixInstance> base,
            ChampionData data,
            ChampionTier tier
    ) {
        return new ArrayList<>(base);
    }
}
