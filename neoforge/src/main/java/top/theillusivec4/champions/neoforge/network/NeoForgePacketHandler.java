package top.theillusivec4.champions.neoforge.network;

import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import top.theillusivec4.champions.api.ChampionsApi;
import top.theillusivec4.champions.api.affix.AffixInstance;
import top.theillusivec4.champions.api.affix.IAffixClientSync;
import top.theillusivec4.champions.api.champion.ChampionTier;
import top.theillusivec4.champions.common.api.ChampionsRegistries;
import top.theillusivec4.champions.common.champion.ChampionData;
import top.theillusivec4.champions.common.client.screen.ChampionEditorScreen;
import top.theillusivec4.champions.common.editor.DatapackEditorHandler;
import top.theillusivec4.champions.common.network.ChampionClearPacket;
import top.theillusivec4.champions.common.network.ChampionSyncData;
import top.theillusivec4.champions.common.network.EditorPayload;
import top.theillusivec4.champions.common.network.OpenEditorPacket;
import top.theillusivec4.champions.common.network.PacketHandler;
import top.theillusivec4.champions.common.network.SaveEditorPacket;
import top.theillusivec4.champions.neoforge.platform.NeoForgeAttachmentProvider;

import java.util.List;
import java.util.Optional;

/**
 * Registers NeoForge packets and implements {@link PacketHandler}.
 *
 * <p>Registration happens on the mod bus via {@link RegisterPayloadHandlersEvent}.
 * Sending uses {@link PacketDistributor} — {@code toTrackingChunk} for champion updates,
 * direct send for per-player login sync.</p>
 */
public final class NeoForgePacketHandler implements PacketHandler {

    private final NeoForgeAttachmentProvider attachmentProvider;

    public NeoForgePacketHandler(IEventBus modBus, NeoForgeAttachmentProvider attachmentProvider) {
        this.attachmentProvider = attachmentProvider;
        modBus.addListener(this::onRegisterPayloads);
    }

    // ── Payload registration ──────────────────────────────────────────────────

    private void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar reg = event.registrar("1");

        reg.playToClient(
                ChampionSyncPacket.TYPE,
                ChampionSyncPacket.STREAM_CODEC,
                this::handleChampionSync
        );
        reg.playToClient(
                ChampionClearPacket.TYPE,
                ChampionClearPacket.STREAM_CODEC,
                this::handleChampionClear
        );
        reg.playToClient(
                TierSyncPacket.TYPE,
                TierSyncPacket.STREAM_CODEC,
                this::handleTierSync
        );
        // Editor packets
        reg.playToClient(
                OpenEditorPacket.TYPE,
                OpenEditorPacket.STREAM_CODEC,
                this::handleOpenEditor
        );
        reg.playToServer(
                SaveEditorPacket.TYPE,
                SaveEditorPacket.STREAM_CODEC,
                this::handleSaveEditor
        );
    }

    // ── Editor handlers ───────────────────────────────────────────────────────

    /** S2C: open the editor screen on the client. */
    private void handleOpenEditor(OpenEditorPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            // Set up save callback: sends SaveEditorPacket back to server
            ChampionEditorScreen
                    .setSaveCallback(payload ->
                            PacketDistributor
                                    .sendToServer(new SaveEditorPacket(payload)));
            ChampionEditorScreen
                    .open(packet.payload());
        });
    }

    /** C2S: client saved edits — write to disk and reload. */
    private void handleSaveEditor(SaveEditorPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer sp) {
                DatapackEditorHandler.handleSave(
                        new DatapackEditorHandler.SaveEditorRequest(packet.payload()), sp);
            }
        });
    }

    // ── Open editor (server-side send) ────────────────────────────────────────

    /** Called from the /champions editor command to push data to the requesting player. */
    public void sendEditorToPlayer(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player,
                new OpenEditorPacket(EditorPayload.fromServerState()));
    }

    // ── Client handlers ───────────────────────────────────────────────────────

    private void handleChampionClear(ChampionClearPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            var mc = Minecraft.getInstance();
            if (mc.level == null) return;
            Entity entity = mc.level.getEntity(packet.entityId());
            if (!(entity instanceof LivingEntity living)) return;
            attachmentProvider.remove(living);
        });
    }

    /**
     * Apply champion sync data on the client.
     */
    private void handleChampionSync(ChampionSyncPacket packet,
                                    IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            var mc = Minecraft.getInstance();
            if (mc.level == null) return;

            Entity entity = mc.level.getEntity(packet.entityId());
            if (!(entity instanceof LivingEntity living)) return;

            ChampionSyncData syncData = packet.data();

            // Resolve tier
            ChampionsApi.get().getTier(syncData.tierId()).ifPresent(tier -> {
                // Rebuild client-side affix list
                List<AffixInstance> affixes = syncData.affixes().stream()
                        .flatMap(entry -> ChampionsApi.get().getAffixType(entry.typeId())
                                .map(type -> {
                                    AffixInstance instance = AffixInstance.fromSync(type, entry.strength());
                                    // Apply any extra client data if the type supports it
                                    if (type instanceof IAffixClientSync sync && entry.clientData() != null) {
                                        sync.readClientData(instance, entry.clientData());
                                    }
                                    return instance;
                                })
                                .stream())
                        .toList();

                // Write to client attachment
                attachmentProvider.setClient(living,
                        buildClientData(tier, affixes));
            });
        });
    }

    /**
     * Apply tier registry sync on the client.
     */
    private void handleTierSync(TierSyncPacket packet,
                                IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            // Rebuild client-side tier registry from packet entries.
            // TierDataLoader is server-only; the client uses a lightweight in-memory map.
            ClientTierCache.rebuild(packet.tiers());
        });
    }

    // ── PacketHandler impl ────────────────────────────────────────────────────

    @Override
    public void clearChampionForTrackers(LivingEntity entity) {
        PacketDistributor.sendToPlayersTrackingEntity(entity,
                new ChampionClearPacket(entity.getId()));
    }

    @Override
    public void syncChampionToTrackers(LivingEntity entity, ChampionSyncData data) {
        ChampionSyncPacket packet = new ChampionSyncPacket(entity.getId(), data);
        PacketDistributor.sendToPlayersTrackingEntity(entity, packet);
    }

    @Override
    public void syncTiersToPlayer(ServerPlayer player) {
        TierSyncPacket packet = TierSyncPacket.from(
                ChampionsRegistries.tiers().getAll()
        );
        PacketDistributor.sendToPlayer(player, packet);
    }

    @Override
    public void syncAllChampionsToPlayer(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        level.getAllEntities().forEach(entity -> {
            if (!(entity instanceof LivingEntity living)) return;
            ChampionsApi.get().getChampion(living).ifPresent(champion -> {
                ChampionSyncData data = ChampionSyncData.from(champion);
                PacketDistributor.sendToPlayer(player,
                        new ChampionSyncPacket(entity.getId(), data));
            });
        });
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static ChampionData buildClientData(
            ChampionTier tier,
            List<AffixInstance> affixes
    ) {
        // Client data only needs enough to reconstruct the view — no base/triggered phase split
        var entries = affixes.stream()
                .flatMap(i -> ChampionsApi.get().getAffixTypeId(i.type())
                        .map(id -> new ChampionData.AffixEntry(
                                id, i.strength(), i.save()))
                        .stream())
                .toList();
        return new ChampionData(
                tier.id(), entries, List.of(), Optional.empty());
    }
}
