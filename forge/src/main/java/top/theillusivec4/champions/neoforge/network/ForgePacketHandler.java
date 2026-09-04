package top.theillusivec4.champions.neoforge.network;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.network.NetworkEvent;
import top.theillusivec4.champions.api.ChampionsApi;
import top.theillusivec4.champions.api.affix.AffixInstance;
import top.theillusivec4.champions.api.affix.IAffixClientSync;
import top.theillusivec4.champions.api.champion.ChampionTier;
import top.theillusivec4.champions.common.api.ChampionsRegistries;
import top.theillusivec4.champions.common.champion.ChampionData;
import top.theillusivec4.champions.common.client.ClientTierCache;
import top.theillusivec4.champions.common.client.screen.ChampionEditorScreen;
import top.theillusivec4.champions.common.editor.DatapackEditorHandler;
import top.theillusivec4.champions.common.network.*;
import top.theillusivec4.champions.common.utils.Utils;
import top.theillusivec4.champions.neoforge.platform.NeoForgeAttachmentProvider;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Registers Forge packets and implements {@link PacketHandler}.
 *
 * <p>Uses a classic 1.20.1 {@link SimpleChannel}: messages are indexed with
 * {@code registerMessage(id, class, encoder, decoder, handler)} and sent through
 * {@link PacketDistributor} — {@code TRACKING_ENTITY} for champion updates,
 * {@code PLAYER} for per-player login sync. Registration happens in the mod
 * constructor, before any client/server handshake.</p>
 */
public final class ForgePacketHandler implements PacketHandler {

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            channelName(),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private int nextId = 0;

    private final NeoForgeAttachmentProvider attachmentProvider;

    public ForgePacketHandler(NeoForgeAttachmentProvider attachmentProvider) {
        this.attachmentProvider = attachmentProvider;

        CHANNEL.registerMessage(nextId++, ChampionSyncPacket.class,
                ChampionSyncPacket::encode, ChampionSyncPacket::decode,
                this::handleChampionSync);
        CHANNEL.registerMessage(nextId++, ChampionClearPacket.class,
                ChampionClearPacket::encode, ChampionClearPacket::decode,
                this::handleChampionClear);
        CHANNEL.registerMessage(nextId++, TierSyncPacket.class,
                TierSyncPacket::encode, TierSyncPacket::decode,
                this::handleTierSync);
        // Editor packets
        CHANNEL.registerMessage(nextId++, OpenEditorPacket.class,
                OpenEditorPacket::encode, OpenEditorPacket::decode,
                this::handleOpenEditor);
        CHANNEL.registerMessage(nextId++, SaveEditorPacket.class,
                SaveEditorPacket::encode, SaveEditorPacket::decode,
                this::handleSaveEditor);
        CHANNEL.registerMessage(nextId++, EditorPackActionPacket.class,
                EditorPackActionPacket::encode, EditorPackActionPacket::decode,
                this::handlePackAction);
    }

    private static ResourceLocation channelName() {
        return Utils.key("main");
    }

    // ── PacketHandler impl ────────────────────────────────────────────────────

    @Override
    public void clearChampionForTrackers(LivingEntity entity) {
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity),
                new ChampionClearPacket(entity.getId()));
    }

    @Override
    public void syncChampionToTrackers(LivingEntity entity, ChampionSyncData data) {
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity),
                new ChampionSyncPacket(entity.getId(), data));
    }

    @Override
    public void syncTiersToPlayer(ServerPlayer player) {
        TierSyncPacket packet = TierSyncPacket.from(
                ChampionsRegistries.tiers().getAll()
        );
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    @Override
    public void syncAllChampionsToPlayer(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        level.getAllEntities().forEach(entity -> {
            if (!(entity instanceof LivingEntity living)) return;
            ChampionsApi.get().getChampion(living).ifPresent(champion -> {
                ChampionSyncData data = ChampionSyncData.from(champion);
                CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                        new ChampionSyncPacket(entity.getId(), data));
            });
        });
    }

    @Override
    public void sendEditorToPlayer(ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new OpenEditorPacket(EditorPayload.fromServerState(player.getServer())));
    }

    // ── S2C handlers ──────────────────────────────────────────────────────────

    /** S2C: apply champion sync data on the client. */
    private void handleChampionSync(ChampionSyncPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
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
        ctx.get().setPacketHandled(true);
    }

    /** S2C: drop the champion state for a despawned/cleared entity. */
    private void handleChampionClear(ChampionClearPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            var mc = Minecraft.getInstance();
            if (mc.level == null) return;
            Entity entity = mc.level.getEntity(packet.entityId());
            if (!(entity instanceof LivingEntity living)) return;
            attachmentProvider.remove(living);
        });
        ctx.get().setPacketHandled(true);
    }

    /** S2C: rebuild the client-side tier registry from packet entries. */
    private void handleTierSync(TierSyncPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // TierDataLoader is server-only; the client uses a lightweight in-memory map.
            ClientTierCache.rebuild(packet.tiers());
        });
        ctx.get().setPacketHandled(true);
    }

    /** S2C: open the editor screen on the client. */
    private void handleOpenEditor(OpenEditorPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Set up save callback: sends SaveEditorPacket back to server
            ChampionEditorScreen.setSaveCallback(payload ->
                    CHANNEL.sendToServer(new SaveEditorPacket(payload)));
            ChampionEditorScreen.setPackActionCallback(CHANNEL::sendToServer);
            ChampionEditorScreen.receivePayload(packet.payload());
        });
        ctx.get().setPacketHandled(true);
    }

    // ── C2S handlers ──────────────────────────────────────────────────────────

    /** C2S: client saved edits — write to disk and reload. */
    private void handleSaveEditor(SaveEditorPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sender = ctx.get().getSender();
            if (sender != null) {
                DatapackEditorHandler.handleSave(
                        new DatapackEditorHandler.SaveEditorRequest(packet.payload()), sender);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    /** C2S: pack management action (toggle / export / import). */
    private void handlePackAction(EditorPackActionPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sender = ctx.get().getSender();
            if (sender != null) {
                DatapackEditorHandler.handlePackAction(packet, sender);
            }
        });
        ctx.get().setPacketHandled(true);
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
