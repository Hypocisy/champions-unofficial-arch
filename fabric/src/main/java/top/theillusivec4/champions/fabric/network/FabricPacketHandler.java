package top.theillusivec4.champions.fabric.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import top.theillusivec4.champions.api.ChampionsApi;
import top.theillusivec4.champions.api.affix.AffixInstance;
import top.theillusivec4.champions.api.affix.IAffixClientSync;
import top.theillusivec4.champions.common.api.ChampionsRegistries;
import top.theillusivec4.champions.common.champion.ChampionData;
import top.theillusivec4.champions.common.client.screen.ChampionEditorScreen;
import top.theillusivec4.champions.common.editor.DatapackEditorHandler;
import top.theillusivec4.champions.common.network.*;
import top.theillusivec4.champions.fabric.platform.FabricAttachmentProvider;

import java.util.List;
import java.util.Optional;

/**
 * Fabric implementation of {@link PacketHandler}.
 *
 * <p>Call {@link #registerServerPayloads()} from {@code onInitialize()},
 * and {@link #registerClientHandlers()} from a client-only entrypoint.</p>
 */
public final class FabricPacketHandler implements PacketHandler {

  private final FabricAttachmentProvider attachmentProvider;

  public FabricPacketHandler(FabricAttachmentProvider attachmentProvider) {
    this.attachmentProvider = attachmentProvider;
  }

  // ── Registration ──────────────────────────────────────────────────────────

  /** Call server-side during mod init. */
  public static void registerServerPayloads() {
    PayloadTypeRegistry.playS2C().register(
        FabricChampionSyncPacket.TYPE, FabricChampionSyncPacket.STREAM_CODEC);
    PayloadTypeRegistry.playS2C().register(
        FabricTierSyncPacket.TYPE, FabricTierSyncPacket.STREAM_CODEC);
    PayloadTypeRegistry.playS2C().register(
        ChampionClearPacket.TYPE, ChampionClearPacket.STREAM_CODEC);
    PayloadTypeRegistry.playS2C().register(
        OpenEditorPacket.TYPE, OpenEditorPacket.STREAM_CODEC);
    // C2S editor save packet
    PayloadTypeRegistry.playC2S().register(
        SaveEditorPacket.TYPE, SaveEditorPacket.STREAM_CODEC);
  }

  /** Call client-side only. */
  public static void registerClientHandlers(FabricAttachmentProvider provider) {
    ClientPlayNetworking.registerGlobalReceiver(
        FabricChampionSyncPacket.TYPE,
        (payload, context) -> context.client().execute(
            () -> handleChampionSync(payload, context.client(), provider)));

    ClientPlayNetworking.registerGlobalReceiver(
        ChampionClearPacket.TYPE,
        (payload, context) -> context.client().execute(() -> {
          if (context.client().level == null) return;
          Entity entity = context.client().level.getEntity(payload.entityId());
          if (entity instanceof LivingEntity living) provider.remove(living);
        }));

    ClientPlayNetworking.registerGlobalReceiver(
        FabricTierSyncPacket.TYPE,
        (payload, context) -> context.client().execute(
            () -> FabricClientTierCache.rebuild(payload.tiers())));

    // Editor S2C: open the editor screen
    ClientPlayNetworking.registerGlobalReceiver(
        OpenEditorPacket.TYPE,
        (payload, context) -> context.client().execute(() -> {
          // Wire save callback: send SaveEditorPacket to server
          ChampionEditorScreen.setSaveCallback(editorPayload ->
              ClientPlayNetworking.send(new SaveEditorPacket(editorPayload)));
          ChampionEditorScreen.open(payload.payload());
        }));
  }

  /** Register server-side C2S handler for save packets. Call from server init. */
  public static void registerServerEditorHandler() {
    ServerPlayNetworking.registerGlobalReceiver(
        SaveEditorPacket.TYPE,
        (payload, context) -> context.server().execute(() ->
            DatapackEditorHandler.handleSave(
                new DatapackEditorHandler.SaveEditorRequest(payload.payload()),
                context.player())));
  }

  // ── PacketHandler impl ────────────────────────────────────────────────────

  @Override
  public void clearChampionForTrackers(LivingEntity entity) {
    ChampionClearPacket packet = new ChampionClearPacket(entity.getId());
    for (ServerPlayer player : PlayerLookup.tracking(entity)) {
      ServerPlayNetworking.send(player, packet);
    }
  }

  @Override
  public void syncChampionToTrackers(LivingEntity entity, ChampionSyncData data) {
    FabricChampionSyncPacket packet = new FabricChampionSyncPacket(entity.getId(), data);
    for (ServerPlayer player : PlayerLookup.tracking(entity)) {
      ServerPlayNetworking.send(player, packet);
    }
  }

  @Override
  public void syncTiersToPlayer(ServerPlayer player) {
    FabricTierSyncPacket packet = FabricTierSyncPacket.from(
        ChampionsRegistries.tiers().getAll());
    ServerPlayNetworking.send(player, packet);
  }

  @Override
  public void syncAllChampionsToPlayer(ServerPlayer player) {
    player.serverLevel().getAllEntities().forEach(entity -> {
      if (!(entity instanceof LivingEntity living)) return;
      ChampionsApi.get().getChampion(living).ifPresent(champion -> {
        ChampionSyncData data = ChampionSyncData.from(champion);
        ServerPlayNetworking.send(player,
            new FabricChampionSyncPacket(entity.getId(), data));
      });
    });
  }

  @Override
  public void sendEditorToPlayer(ServerPlayer player) {
    ServerPlayNetworking.send(player,
        new OpenEditorPacket(EditorPayload.fromServerState()));
  }

  // ── Client handlers ───────────────────────────────────────────────────────

  private static void handleChampionSync(
      FabricChampionSyncPacket packet,
      Minecraft mc,
      FabricAttachmentProvider provider
  ) {
    if (mc.level == null) return;

    Entity entity = mc.level.getEntity(packet.entityId());
    if (!(entity instanceof LivingEntity living)) return;

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
