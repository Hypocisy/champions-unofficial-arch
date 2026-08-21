package top.theillusivec4.champions.neoforge.network;

import net.minecraft.resources.ResourceLocation;
import top.theillusivec4.champions.api.champion.ChampionTier;

import java.util.*;

/**
 * Client-side cache of the tier registry, populated from {@link TierSyncPacket}.
 *
 * <p>The server's {@link top.theillusivec4.champions.common.data.TierDataLoader} is
 * server-only. The client only needs enough tier data to render particles and HUD,
 * so we maintain a simple in-memory map here rather than running the full loader.</p>
 */
public final class ClientTierCache {

    private static volatile Map<ResourceLocation, ChampionTier> tiers = Map.of();

    private ClientTierCache() {
    }

    /**
     * Rebuild from a {@link TierSyncPacket}. Called on the render thread.
     */
    static void rebuild(List<TierSyncPacket.TierEntry> entries) {
        Map<ResourceLocation, ChampionTier> map = new HashMap<>();
        for (TierSyncPacket.TierEntry entry : entries) {
            ChampionTier.TierDisplay display = new ChampionTier.TierDisplay(
                    entry.color(),
                    // icon is a client-only asset — default is fine here
                    ResourceLocation.withDefaultNamespace("textures/gui/icons.png")
            );
            map.put(entry.id(), new ChampionTier(entry.id(), entry.level(), display));
        }
        tiers = Collections.unmodifiableMap(map);
    }

    public static Optional<ChampionTier> get(ResourceLocation id) {
        return Optional.ofNullable(tiers.get(id));
    }

    public static Collection<ChampionTier> getAll() {
        return tiers.values();
    }
}
