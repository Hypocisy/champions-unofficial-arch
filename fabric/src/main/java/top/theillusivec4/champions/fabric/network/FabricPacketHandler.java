package top.theillusivec4.champions.fabric.network;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import top.theillusivec4.champions.api.ChampionsApi;
import top.theillusivec4.champions.api.affix.AffixInstance;
import top.theillusivec4.champions.api.affix.IAffixClientSync;
import top.theillusivec4.champions.common.api.ChampionsRegistries;
import top.theillusivec4.champions.common.champion.ChampionData;
import top.theillusivec4.champions.common.client.ClientTierCache;
import top.theillusivec4.champions.common.client.screen.ChampionEditorScreen;
import top.theillusivec4.champions.common.editor.DatapackEditorHandler;
import top.theillusivec4.champions.common.network.*;
import top.theillusivec4.champions.fabric.platform.FabricAttachmentProvider;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Fabric implementation of {@link PacketHandler} using the classic 1.20.1
 * raw {@link FriendlyByteBuf} networking API.
 *
 * <p>Play channels are plain {@code ResourceLocation} ids — each packet record in
 * {@code common.network} owns its {@code ID} constant and its {@code encode}/
 * {@code decode} methods, so both platforms share the exact same wire format.
 * Receivers decode on the netty thread, then hop to the main thread via
 * {@code server.execute()} / {@code client.execute()}.</p>
 *
 * <p>Because a champion sync can (despite the server-side deferral in
 * {@code FabricChampionEventsHandler}) still arrive before the entity's spawn
 * packet, syncs for unknown entities are parked in a bounded pending cache and
 * applied on the next client tick once the entity exists — see
 * {@link #tickPendingSyncs(Minecraft)}.</p>
 *
 * <p>Call {@link #registerServerReceivers()} from {@code onInitialize()},
 * and {@link #registerClientHandlers(FabricAttachmentProvider)} from a
 * client-only entrypoint.</p>
 */
public final class FabricPacketHandler implements PacketHandler {

  private final FabricAttachmentProvider attachmentProvider;

  public FabricPacketHandler(FabricAttachmentProvider attachmentProvider) {
    this.attachmentProvider = attachmentProvider;
  }

  // ── Registration ──────────────────────────────────────────────────────────

  /**
   * Register the C2S (server-side) receivers for editor packets.
   * Call from server mod init; S2C channels need no registration on 1.20.1 Fabric.
   */
  public static void registerServerReceivers() {
    ServerPlayNetworking.registerGlobalReceiver(SaveEditorPacket.ID,
        (MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender) -> {
          SaveEditorPacket packet = SaveEditorPacket.decode(buf);
          server.execute(() ->
              DatapackEditorHandler.handleSave(
                  new DatapackEditorHandler.SaveEditorRequest(packet.payload()), player));
        });

    ServerPlayNetworking.registerGlobalReceiver(EditorPackActionPacket.ID,
        (MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender) -> {
          EditorPackActionPacket packet = EditorPackActionPacket.decode(buf);
          server.execute(() -> DatapackEditorHandler.handlePackAction(packet, player));
        });
  }

  /** Register the S2C (client-side) receivers. Call from a client entrypoint only. */
  public static void registerClientHandlers(FabricAttachmentProvider provider) {
    clientProvider = provider;
    ClientTickEvents.END_CLIENT_TICK.register(FabricPacketHandler::tickPendingSyncs);

    ClientPlayNetworking.registerGlobalReceiver(ChampionSyncPacket.ID,
        (client, handler, buf, responseSender) -> {
          ChampionSyncPacket packet = ChampionSyncPacket.decode(buf);
          client.execute(() -> {
            if (client.level == null) return;
            Entity entity = client.level.getEntity(packet.entityId());
            if (entity == null) {
              // Sync raced ahead of the spawn packet — park it until the entity
              // exists (see tickPendingSyncs).
              if (PENDING_SYNCS.size() >= PENDING_SYNC_LIMIT) PENDING_SYNCS.clear();
              pendingLevel = client.level;
              PENDING_SYNCS.put(packet.entityId(),
                  new PendingSync(packet, client.level.getGameTime()));
              return;
            }
            if (entity instanceof LivingEntity living) {
              applyChampionSync(packet, living, provider);
            }
          });
        });

    ClientPlayNetworking.registerGlobalReceiver(ChampionClearPacket.ID,
        (client, handler, buf, responseSender) -> {
          ChampionClearPacket packet = ChampionClearPacket.decode(buf);
          client.execute(() -> {
            // Drop any parked sync too — the entity is going away.
            PENDING_SYNCS.remove(packet.entityId());
            if (client.level == null) return;
            Entity entity = client.level.getEntity(packet.entityId());
            if (entity instanceof LivingEntity living) provider.remove(living);
          });
        });

    ClientPlayNetworking.registerGlobalReceiver(TierSyncPacket.ID,
        (client, handler, buf, responseSender) -> {
          TierSyncPacket packet = TierSyncPacket.decode(buf);
          client.execute(() -> ClientTierCache.rebuild(packet.tiers()));
        });

    ClientPlayNetworking.registerGlobalReceiver(OpenEditorPacket.ID,
        (client, handler, buf, responseSender) -> {
          OpenEditorPacket packet = OpenEditorPacket.decode(buf);
          client.execute(() -> {
            // Wire save callback: send SaveEditorPacket to server
            ChampionEditorScreen.setSaveCallback(payload ->
                sendToServer(SaveEditorPacket.ID, new SaveEditorPacket(payload)::encode));
            ChampionEditorScreen.setPackActionCallback(packet2 ->
                sendToServer(EditorPackActionPacket.ID, packet2::encode));
            ChampionEditorScreen.receivePayload(packet.payload());
          });
        });
  }

  // ── Pending client syncs ──────────────────────────────────────────────────

  /** Ticks a parked sync waits for its entity to appear before being dropped. */
  private static final long PENDING_SYNC_TIMEOUT_TICKS = 200;

  /** Hard cap against pathological traffic — cleared wholesale when exceeded. */
  private static final int PENDING_SYNC_LIMIT = 1024;

  private static final Map<Integer, PendingSync> PENDING_SYNCS = new ConcurrentHashMap<>();

  private static FabricAttachmentProvider clientProvider;

  /** The level the parked syncs belong to; a level swap invalidates them. */
  private static ClientLevel pendingLevel;

  private record PendingSync(ChampionSyncPacket packet, long receivedAt) {}

  /**
   * Client tick: apply parked syncs whose entity has appeared, and drop entries
   * that timed out or belong to a previous level.
   */
  private static void tickPendingSyncs(Minecraft client) {
    if (PENDING_SYNCS.isEmpty()) return;
    if (client.level == null || client.level != pendingLevel) {
      // Disconnect or dimension change — parked ids belong to the old level.
      pendingLevel = client.level;
      PENDING_SYNCS.clear();
      return;
    }

    long now = client.level.getGameTime();
    Iterator<Map.Entry<Integer, PendingSync>> it = PENDING_SYNCS.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry<Integer, PendingSync> entry = it.next();
      PendingSync pending = entry.getValue();
      if (now - pending.receivedAt() > PENDING_SYNC_TIMEOUT_TICKS) {
        it.remove();
      } else if (client.level.getEntity(entry.getKey()) instanceof LivingEntity living) {
        it.remove();
        applyChampionSync(pending.packet(), living);
      }
    }
  }

  // ── PacketHandler impl ────────────────────────────────────────────────────

  @Override
  public void clearChampionForTrackers(LivingEntity entity) {
    ChampionClearPacket packet = new ChampionClearPacket(entity.getId());
    for (ServerPlayer player : PlayerLookup.tracking(entity)) {
      sendTo(player, ChampionClearPacket.ID, packet::encode);
    }
  }

  @Override
  public void syncChampionToTrackers(LivingEntity entity, ChampionSyncData data) {
    ChampionSyncPacket packet = new ChampionSyncPacket(entity.getId(), data);
    for (ServerPlayer player : PlayerLookup.tracking(entity)) {
      sendTo(player, ChampionSyncPacket.ID, packet::encode);
    }
  }

  @Override
  public void syncTiersToPlayer(ServerPlayer player) {
    TierSyncPacket packet = TierSyncPacket.from(ChampionsRegistries.tiers().getAll());
    sendTo(player, TierSyncPacket.ID, packet::encode);
  }

  @Override
  public void syncAllChampionsToPlayer(ServerPlayer player) {
    player.serverLevel().getAllEntities().forEach(entity -> {
      if (!(entity instanceof LivingEntity living)) return;
      ChampionsApi.get().getChampion(living).ifPresent(champion -> {
        ChampionSyncPacket packet =
            new ChampionSyncPacket(entity.getId(), ChampionSyncData.from(champion));
        sendTo(player, ChampionSyncPacket.ID, packet::encode);
      });
    });
  }

  @Override
  public void sendEditorToPlayer(ServerPlayer player) {
    OpenEditorPacket packet = new OpenEditorPacket(
        EditorPayload.fromServerState(player.getServer()));
    sendTo(player, OpenEditorPacket.ID, packet::encode);
  }

  // ── Send helpers ──────────────────────────────────────────────────────────

  private static void sendTo(ServerPlayer player, ResourceLocation id, Consumer<FriendlyByteBuf> writer) {
    FriendlyByteBuf buf = PacketByteBufs.create();
    writer.accept(buf);
    ServerPlayNetworking.send(player, id, buf);
  }

  private static void sendToServer(ResourceLocation id, Consumer<FriendlyByteBuf> writer) {
    FriendlyByteBuf buf = PacketByteBufs.create();
    writer.accept(buf);
    ClientPlayNetworking.send(id, buf);
  }

  // ── Client apply ──────────────────────────────────────────────────────────

  private static void applyChampionSync(ChampionSyncPacket packet, LivingEntity living) {
    applyChampionSync(packet, living, clientProvider);
  }

  private static void applyChampionSync(
      ChampionSyncPacket packet,
      LivingEntity living,
      FabricAttachmentProvider provider
  ) {
    ChampionSyncData syncData = packet.data();

    ChampionsApi.get().getTier(syncData.tierId()).ifPresent(tier -> {
      List<AffixInstance> affixes = syncData.affixes().stream()
          .flatMap(entry -> ChampionsApi.get().getAffixType(entry.typeId())
              .map(type -> {
                AffixInstance instance = AffixInstance.fromSync(type, entry.strength());
                if (type instanceof IAffixClientSync sync && entry.clientData() != null) {
                  sync.readClientData(instance, entry.clientData());
                }
                return instance;
              })
              .stream())
          .toList();

      ChampionData clientData = new ChampionData(
          tier.id(),
          affixes.stream()
              .flatMap(i -> ChampionsApi.get().getAffixTypeId(i.type())
                  .map(id -> new ChampionData.AffixEntry(id, i.strength(), i.save()))
                  .stream())
              .toList(),
          List.of(),
          Optional.empty()
      );
      provider.setClient(living, clientData);
    });
  }
}
