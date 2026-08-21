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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Fabric implementation of {@link ChampionAttachmentProvider}.
 *
 * <p>Uses Cardinal Components ({@link FabricChampionComponent}) to store
 * {@link ChampionData} on {@link LivingEntity} instances.
 * The API surface is identical to the NeoForge provider — all champion logic
 * lives in {@link ChampionView} and is shared.</p>
 */
public final class FabricAttachmentProvider implements ChampionAttachmentProvider {

    // ── ChampionAttachmentProvider ────────────────────────────────────────────

    @Override
    public Optional<Champion.Server> getServer(LivingEntity entity) {
        return FabricChampionComponent.SERVER.maybeGet(entity)
                .filter(FabricChampionComponent::isPresent)
                .flatMap(component -> buildServerView(entity, component));
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
        FabricChampionComponent.SERVER.maybeGet(entity)
                .ifPresent(c -> c.setData(data));
    }

    public void setClient(LivingEntity entity, ChampionData data) {
        FabricChampionComponent.CLIENT.maybeGet(entity)
                .ifPresent(c -> c.setData(data));
    }

    @Override
    public void remove(LivingEntity entity) {
        FabricChampionComponent.SERVER.maybeGet(entity)
                .ifPresent(c -> c.setData(ChampionData.EMPTY));
        FabricChampionComponent.CLIENT.maybeGet(entity)
                .ifPresent(c -> c.setData(ChampionData.EMPTY));
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
