package top.theillusivec4.champions.fabric.network;

import net.minecraft.resources.Identifier;
import top.theillusivec4.champions.api.champion.ChampionTier;

import java.util.*;

/**
 * Client-side tier cache rebuilt from {@link FabricTierSyncPacket} on login/reload.
 */
public final class FabricClientTierCache {

  private static volatile Map<Identifier, ChampionTier> tiers = Map.of();

  private FabricClientTierCache() {}

  public static void rebuild(List<FabricTierSyncPacket.TierEntry> entries) {
    Map<Identifier, ChampionTier> map = new HashMap<>();
    for (var entry : entries) {
      ChampionTier.TierDisplay display = new ChampionTier.TierDisplay(
          entry.color(),
          Identifier.withDefaultNamespace("textures/gui/icons.png")
      );
      map.put(entry.id(), new ChampionTier(entry.id(), entry.level(), display));
    }
    tiers = Collections.unmodifiableMap(map);
  }

  public static Optional<ChampionTier> get(Identifier id) {
    return Optional.ofNullable(tiers.get(id));
  }

  public static Collection<ChampionTier> getAll() {
    return tiers.values();
  }
}