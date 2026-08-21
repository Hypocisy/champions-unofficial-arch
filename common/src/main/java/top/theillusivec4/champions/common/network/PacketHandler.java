package top.theillusivec4.champions.common.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

/**
 * Platform abstraction for sending champion sync packets.
 *
 * <p>Injected at startup alongside {@link top.theillusivec4.champions.platform.ChampionAttachmentProvider}.
 * NeoForge and Fabric implementations live in their respective platform modules.</p>
 */
public interface PacketHandler {

    /**
     * Send updated champion state to all players currently tracking {@code entity}.
     * Called after every affix mutation (add, remove) on the server.
     */
    void syncChampionToTrackers(LivingEntity entity, ChampionSyncData data);

    /**
     * Tell all tracking players to clear the champion state for {@code entity}.
     * Called after /champions remove clears the attachment.
     */
    void clearChampionForTrackers(LivingEntity entity);

    /**
     * Send the full tier registry to a specific player.
     * Called on player login and after datapack reload.
     */
    void syncTiersToPlayer(ServerPlayer player);

    /**
     * Send the current state of every loaded champion visible to {@code player}.
     * Called on player login so the client can render existing champions immediately.
     */
    void syncAllChampionsToPlayer(ServerPlayer player);

    /**
     * Send the full editor payload to {@code player} so the client can open the editor GUI.
     * Default no-op — platforms that support the editor override this.
     */
    default void sendEditorToPlayer(ServerPlayer player) {}

    // ── Holder ────────────────────────────────────────────────────────────────

    final class Holder {
        private Holder() {
        }

        private static PacketHandler INSTANCE;

        public static PacketHandler get() {
            if (INSTANCE == null) throw new IllegalStateException(
                    "PacketHandler not registered yet.");
            return INSTANCE;
        }

        public static void register(PacketHandler handler) {
            if (INSTANCE != null) throw new IllegalStateException(
                    "PacketHandler already registered.");
            INSTANCE = handler;
        }
    }
}
