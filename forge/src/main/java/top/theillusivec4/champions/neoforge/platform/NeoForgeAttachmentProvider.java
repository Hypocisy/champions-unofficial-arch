package top.theillusivec4.champions.neoforge.platform;


import com.mojang.serialization.JsonOps;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import top.theillusivec4.champions.api.ChampionsApi;
import top.theillusivec4.champions.api.affix.AffixInstance;
import top.theillusivec4.champions.api.champion.Champion;
import top.theillusivec4.champions.api.champion.ChampionTier;
import top.theillusivec4.champions.common.champion.ChampionData;
import top.theillusivec4.champions.common.champion.ChampionView;
import top.theillusivec4.champions.common.network.ChampionSyncData;
import top.theillusivec4.champions.common.network.PacketHandler;
import top.theillusivec4.champions.common.phase.PhaseProcessor;
import top.theillusivec4.champions.common.utils.Utils;
import top.theillusivec4.champions.platform.ChampionAttachmentProvider;

import java.util.*;

/**
 * Forge 1.20.1 implementation of {@link ChampionAttachmentProvider}.
 *
 * <p>Forge 1.20.1 has no data-attachment system, so champion state is stored in a
 * {@link Capability} attached to every {@link LivingEntity} via
 * {@link AttachCapabilitiesEvent}. The capability payload serializes through
 * {@link ChampionData#CODEC} + {@link NbtOps}, mirroring the codec-backed
 * attachment the NeoForge build used, so saved champions survive chunk unload
 * and server restart.</p>
 *
 * <p>Server views are cached per entity in a plain {@link HashMap} with explicit
 * invalidation. Rebuilding them on every {@link #getServer} call used to re-resolve
 * affixes and re-apply triggered phase effects on every tick — O(affixes + phases) per
 * champion per tick. The cache is invalidated on {@link #setServer} (a real re-roll /
 * initial write), on {@link #remove}, and when the entity leaves the level
 * ({@link #onEntityLeaveLevel}) so entries never outlive their entity.</p>
 *
 * <p>A {@code WeakHashMap} is deliberately <em>not</em> used: the cached value
 * ({@link ChampionView.Server}) holds a strong reference back to the key entity, so
 * weak-key clearing would never fire — a textbook WeakHashMap leak.</p>
 */
public final class NeoForgeAttachmentProvider implements ChampionAttachmentProvider {

    /** Live server views, keyed by entity identity. Server-thread only. */
    private final Map<LivingEntity, ChampionView.Server> serverViewCache =
            new HashMap<>();

    // ── Capability wiring ─────────────────────────────────────────────────────

    /** Capability holding champion state on any LivingEntity (server + client slots). */
    public static final Capability<ChampionHolder> CHAMPION_CAP =
            CapabilityManager.get(new CapabilityToken<>() {});

    private static final ResourceLocation CAP_KEY = Utils.key("champion_data");

    /**
     * Attach the capability provider to every living entity.
     * Called from the mod constructor (game bus).
     */
    public void registerCapability() {
        // AttachCapabilitiesEvent is generic — must use addGenericListener
        MinecraftForge.EVENT_BUS.addGenericListener(Entity.class,
                (AttachCapabilitiesEvent<Entity> event) -> {
                    if (!(event.getObject() instanceof LivingEntity)) return;
                    AttachmentProvider provider = new AttachmentProvider();
                    event.addCapability(CAP_KEY, provider);
                    event.addListener(provider::invalidate);
                });
    }

    /** Per-entity capability payload: server slot persisted, client slot transient. */
    public static class ChampionHolder {
        private ChampionData serverData = ChampionData.EMPTY;
        private ChampionData clientData = ChampionData.EMPTY;

        /** A "real" champion is one with an assigned tier. */
        private static boolean isChampion(ChampionData data) {
            return data != null && data.tierId() != null;
        }

        boolean hasServer() {
            return isChampion(serverData);
        }

        ChampionData serverData() {
            return serverData;
        }

        void setServerData(ChampionData data) {
            this.serverData = data == null ? ChampionData.EMPTY : data;
        }

        ChampionData clientData() {
            return clientData;
        }

        void setClientData(ChampionData data) {
            this.clientData = data == null ? ChampionData.EMPTY : data;
        }

        void clear() {
            serverData = ChampionData.EMPTY;
            clientData = ChampionData.EMPTY;
        }

        CompoundTag serializeNBT() {
            return ChampionData.CODEC
                    .encodeStart(NbtOps.INSTANCE, serverData)
                    .result()
                    .map(tag -> (CompoundTag) tag)
                    .orElseGet(CompoundTag::new);
        }

        void deserializeNBT(CompoundTag tag) {
            if (tag == null || tag.isEmpty()) return;
            ChampionData.CODEC
                    .parse(NbtOps.INSTANCE, tag)
                    .result()
                    .ifPresent(this::setServerData);
        }
    }

    /** Capability provider attached to each entity; serializes through the holder. */
    public static class AttachmentProvider implements ICapabilitySerializable<CompoundTag> {
        private final ChampionHolder backend = new ChampionHolder();
        private final LazyOptional<ChampionHolder> optional = LazyOptional.of(() -> backend);

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
            return cap == CHAMPION_CAP ? optional.cast() : LazyOptional.empty();
        }

        @Override
        public CompoundTag serializeNBT() {
            return backend.serializeNBT();
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            backend.deserializeNBT(tag);
        }

        void invalidate() {
            optional.invalidate();
        }
    }

    // ── ChampionAttachmentProvider ────────────────────────────────────────────

    @Override
    public Optional<Champion.Server> getServer(LivingEntity entity) {
        return entity.getCapability(CHAMPION_CAP)
                .resolve()
                .filter(ChampionHolder::hasServer)
                .flatMap(holder -> {
                    ChampionData data = holder.serverData();

                    ChampionView.Server cached = serverViewCache.get(entity);
                    if (cached == null && entity.isRemoved()) {
                        // Defensive: an entity that has already left the world may still be queried
                        // during teardown (e.g. death loot). Never cache a view for it.
                        return Optional.empty();
                    }
                    if (cached != null && entity.isRemoved()) {
                        serverViewCache.remove(entity);
                        cached = null;
                    }
                    if (cached != null) return Optional.of(cached);

                    Optional<ChampionView.Server> built = buildServerView(entity, data);
                    built.ifPresent(view -> serverViewCache.put(entity, view));
                    return built;
                });
    }

    @Override
    public Optional<Champion.Client> getClient(LivingEntity entity) {
        return entity.getCapability(CHAMPION_CAP)
                .resolve()
                .filter(holder -> ChampionHolder.isChampion(holder.clientData()))
                .flatMap(holder -> buildClientView(entity, holder.clientData()));
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
        return entity.getCapability(CHAMPION_CAP)
                .resolve()
                .map(holder -> entity.level().isClientSide()
                        ? ChampionHolder.isChampion(holder.clientData())
                        : holder.hasServer())
                .orElse(false);
    }

    // ── Write ─────────────────────────────────────────────────────────────────

    /**
     * Write champion data to the server-side capability.
     * Called from {@link ChampionView.Server}'s save callback.
     */
    public void setServer(LivingEntity entity, ChampionData data) {
        // A real (re)write — the old in-memory view is stale.
        serverViewCache.remove(entity);
        entity.getCapability(CHAMPION_CAP).ifPresent(holder -> holder.setServerData(data));
    }

    @Override
    public void persistServer(LivingEntity entity, ChampionData data) {
        // Write-through without invalidating the cached view. Called from the
        // view's save callback after every mutation (affix add/remove, phase
        // trigger, runtime-state persist).
        entity.getCapability(CHAMPION_CAP).ifPresent(holder -> holder.setServerData(data));
    }

    /**
     * Write champion data to the client-side slot.
     * Called from the sync packet handler.
     */
    public void setClient(LivingEntity entity, ChampionData data) {
        entity.getCapability(CHAMPION_CAP).ifPresent(holder -> holder.setClientData(data));
    }

    @Override
    public void remove(LivingEntity entity) {
        serverViewCache.remove(entity);
        entity.getCapability(CHAMPION_CAP).ifPresent(ChampionHolder::clear);
    }

    /**
     * Invalidate the cached view when an entity leaves the level (death, chunk
     * unload, dimension change). Keeps the cache size proportional to the number
     * of live champions instead of every champion that ever existed.
     * Registered from the mod constructor.
     */
    public void onEntityLeaveLevel(LivingEntity entity) {
        serverViewCache.remove(entity);
    }

    // ── View construction ─────────────────────────────────────────────────────

    private Optional<ChampionView.Server> buildServerView(LivingEntity entity, ChampionData data) {
        return ChampionsApi.get().getTier(data.tierId()).map(tier -> {
            List<AffixInstance> baseAffixes = resolveAffixes(data);
            List<AffixInstance> liveAffixes = rebuildLiveAffixes(baseAffixes, data, tier);
            // Use a single-element array so the sync lambda can capture the server
            // reference after construction — avoids capturing the pre-copy liveAffixes list.
            ChampionView.Server[] ref = { null };

            ChampionView.Server server = new ChampionView.Server(
                    entity,
                    tier,
                    baseAffixes,
                    liveAffixes,
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

    private Optional<ChampionView.Client> buildClientView(LivingEntity entity, ChampionData data) {
        return ChampionsApi.get().getTier(data.tierId()).map(tier -> {
            List<AffixInstance> affixes = resolveAffixes(data);
            return new ChampionView.Client(entity, tier, affixes);
        });
    }

    // ── Reconstruction helpers ────────────────────────────────────────────────

    /**
     * Resolve {@link ChampionData.AffixEntry} records into live {@link AffixInstance} objects.
     * Entries whose type id is no longer in the registry are silently skipped.
     */
    private static List<AffixInstance> resolveAffixes(ChampionData data) {
        return data.baseAffixes().stream()
                .flatMap(entry -> ChampionsApi.get().getAffixType(entry.typeId())
                        .map(type -> AffixInstance.load(type, buildInstanceTag(entry)))
                        .stream())
                .toList();
    }

    /**
     * Rebuild the live affix list from base affixes + already-triggered phase effects.
     * Phase effects that were triggered in a previous session are re-applied via
     * {@link PhaseProcessor#restoreTriggeredPhases} in the server view's init callback.
     * This method only provides the initial list before restoration runs.
     */
    private static List<AffixInstance> rebuildLiveAffixes(
            List<AffixInstance> base,
            ChampionData data,
            ChampionTier tier
    ) {
        return new ArrayList<>(base);
    }

    /**
     * Pack an {@link ChampionData.AffixEntry} into the CompoundTag shape that
     * {@link AffixInstance#load} expects: {@code strength} key + affix data keys.
     */
    private static CompoundTag buildInstanceTag(ChampionData.AffixEntry entry) {
        CompoundTag tag = entry.data().copy();
        tag.putInt("strength", entry.strength());
        return tag;
    }
}
