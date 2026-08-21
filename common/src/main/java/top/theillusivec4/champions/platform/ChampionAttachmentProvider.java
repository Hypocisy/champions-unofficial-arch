package top.theillusivec4.champions.platform;

import net.minecraft.world.entity.LivingEntity;
import top.theillusivec4.champions.api.ChampionsApi;
import top.theillusivec4.champions.api.champion.Champion;
import top.theillusivec4.champions.common.champion.ChampionData;

import java.util.Optional;

/**
 * Platform abstraction for champion attachment storage.
 *
 * <p>NeoForge implements this with {@code AttachmentType}; Fabric with Cardinal Components.
 * The implementation is injected at startup alongside {@link ChampionsApi}.</p>
 *
 * <p>Only the server-side attachment is mutable. The client-side attachment is rebuilt
 * from sync packets and should not be mutated directly.</p>
 */
public interface ChampionAttachmentProvider {

    /**
     * Returns the server-side champion view for {@code entity}, if present and initialised.
     * Returns empty for non-champion entities or during early spawn before assignment.
     */
    Optional<Champion.Server> getServer(LivingEntity entity);

    /**
     * Returns the client-side champion view for {@code entity}, if present.
     * Only valid on the logical client.
     */
    Optional<Champion.Client> getClient(LivingEntity entity);

    /**
     * Returns either the server or client view depending on the calling side.
     * Convenience method used by {@link ChampionsApi#getChampion}.
     */
    Optional<Champion> get(LivingEntity entity);

    /**
     * Returns true if {@code entity} currently has a champion attachment on either side.
     * Cheaper than {@link #get(LivingEntity)} — does not allocate an Optional.
     */
    boolean has(LivingEntity entity);

    void setServer(LivingEntity entity, ChampionData data);

    void setClient(LivingEntity entity, ChampionData data);

    /**
     * Remove the champion attachment from {@code entity} entirely.
     * After this call {@link #has(LivingEntity)} returns false.
     */
    void remove(LivingEntity entity);
    // ── Internal ──────────────────────────────────────────────────────────────

    /**
     * Holder for the active platform implementation.
     * Injected at startup by the platform module — not for direct use.
     */
    final class Holder {
        private Holder() {
        }

        private static ChampionAttachmentProvider INSTANCE = null;

        public static ChampionAttachmentProvider get() {
            if (INSTANCE == null) {
                throw new IllegalStateException(
                        "ChampionAttachmentProvider has not been registered. " +
                                "Ensure the platform module initialises before any API calls."
                );
            }
            return INSTANCE;
        }

        public static void register(ChampionAttachmentProvider provider) {
            if (INSTANCE != null) {
                throw new IllegalStateException("ChampionAttachmentProvider already registered.");
            }
            INSTANCE = provider;
        }
    }
}
