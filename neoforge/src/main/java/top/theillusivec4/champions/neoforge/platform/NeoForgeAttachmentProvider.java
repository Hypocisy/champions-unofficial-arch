package top.theillusivec4.champions.neoforge.platform;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * NeoForge implementation of {@link ChampionAttachmentProvider}.
 *
 * <p>Uses NeoForge's {@link AttachmentType} system to attach {@link ChampionData}
 * directly to {@link LivingEntity} instances. The attachment is codec-serialized,
 * so it survives chunk unload and server restart automatically.</p>
 *
 * <p>Server and client views ({@link ChampionView.Server}, {@link ChampionView.Client})
 * are constructed lazily on each {@link #get(LivingEntity)} call. They are lightweight
 * wrappers — no caching needed since attachment reads are O(1) map lookups.</p>
 */
public final class NeoForgeAttachmentProvider implements ChampionAttachmentProvider {

    private static final String MOD_ID = "champions";

    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MOD_ID);

    /**
     * The attachment holding champion state on server-side entities.
     * Codec-backed so NeoForge handles NBT serialization automatically.
     */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<ChampionData>>
            CHAMPION_DATA = ATTACHMENT_TYPES.register("champion_data", () ->
            AttachmentType.builder(() -> ChampionData.EMPTY)
                    .serialize(ChampionData.CODEC)
                    .build()
    );

    /**
     * The attachment holding the client-side champion view.
     * Not persisted — rebuilt from sync packets on every login/reload.
     */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<ChampionData>>
            CHAMPION_DATA_CLIENT = ATTACHMENT_TYPES.register("champion_data_client", () ->
            AttachmentType.builder(() -> ChampionData.EMPTY)
                    .build()  // no codec — client data is transient
    );

    public NeoForgeAttachmentProvider(IEventBus modBus) {
        ATTACHMENT_TYPES.register(modBus);
    }

    // ── ChampionAttachmentProvider ────────────────────────────────────────────

    @Override
    public Optional<Champion.Server> getServer(LivingEntity entity) {
        if (!entity.hasData(CHAMPION_DATA.get())) return Optional.empty();
        ChampionData data = entity.getExistingDataOrNull(CHAMPION_DATA.get());
        if (data == null || data.tierId() == null) return Optional.empty();
        return buildServerView(entity, data).map(v -> v);
    }

    @Override
    public Optional<Champion.Client> getClient(LivingEntity entity) {
        if (!entity.hasData(CHAMPION_DATA_CLIENT.get())) return Optional.empty();
        ChampionData data = entity.getExistingDataOrNull(CHAMPION_DATA_CLIENT.get());
        if (data == null || data.tierId() == null) return Optional.empty();
        return buildClientView(entity, data).map(v -> v);
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
            return entity.hasData(CHAMPION_DATA_CLIENT.get());
        }
        return entity.hasData(CHAMPION_DATA.get());
    }

    // ── Write ─────────────────────────────────────────────────────────────────

    /**
     * Write champion data to the server-side attachment.
     * Called from {@link ChampionView.Server}'s save callback.
     */
    public void setServer(LivingEntity entity, ChampionData data) {
        entity.setData(CHAMPION_DATA.get(), data);
    }

    /**
     * Write champion data to the client-side attachment.
     * Called from the sync packet handler.
     */
    public void setClient(LivingEntity entity, ChampionData data) {
        entity.setData(CHAMPION_DATA_CLIENT.get(), data);
    }

    @Override
    public void remove(LivingEntity entity) {
        entity.setData(CHAMPION_DATA.get(), ChampionData.EMPTY);
        entity.setData(CHAMPION_DATA_CLIENT.get(), ChampionData.EMPTY);
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
                    updated -> setServer(entity, updated),
                    () -> {
                        PacketHandler.Holder.get()
                                .syncChampionToTrackers(entity,
                                        ChampionSyncData.from(ref[0]));
                        PhaseProcessor.restoreTriggeredPhases(ref[0]);
                    },

                    data.triggeredPhases(),
                    data.archetypeId());


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
