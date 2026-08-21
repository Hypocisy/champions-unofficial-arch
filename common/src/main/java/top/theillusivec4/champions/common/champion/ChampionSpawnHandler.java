package top.theillusivec4.champions.common.champion;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import top.theillusivec4.champions.api.ChampionsApi;
import top.theillusivec4.champions.api.champion.ChampionTier;
import top.theillusivec4.champions.common.api.ChampionsRegistries;
import top.theillusivec4.champions.common.config.ChampionsConfig;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Platform-agnostic champion spawn logic.
 *
 * <p>Called from platform event bridges ({@code ChampionEventsHandler} on NeoForge,
 * {@code ServerEntityEvents.ENTITY_LOAD} on Fabric) whenever an entity joins the world.</p>
 */
public final class ChampionSpawnHandler {

  private ChampionSpawnHandler() {}

  // Default tier weights when no per-tier weight is defined.
  // Index 0 = lowest tier (most common). TODO: expose as config once per-tier config is designed.
  private static final int[] DEFAULT_TIER_WEIGHTS = { 50, 25, 15, 8, 2 };

  // ── Public entry point ────────────────────────────────────────────────────

  /**
   * Attempt to spawn a champion from {@code entity}.
   * Safe to call from any entity-join hook; does its own eligibility checks.
   *
   * @param entity the entity that just joined the level
   * @param level  the server level (must be server-side)
   */
  public static void trySpawn(LivingEntity entity, ServerLevel level) {
    if (!isEligible(entity)) return;
    if (!hasData()) return;

    RandomSource random = entity.getRandom();
    // Roll before the beacon scan: the scan walks every block entity in a large
    // chunk radius, and 90% of eligible mobs are rejected here anyway.
    if (random.nextFloat() >= ChampionsConfig.spawnChance) return;

    if (isNearActiveBeacon(entity, level)) return;

    ChampionTier tier = selectTier(random);
    if (tier == null) return;

    ChampionsRegistries.builder().trySpawn(entity, tier, random);
  }

  // ── Eligibility ───────────────────────────────────────────────────────────

  /**
   * Coarse, universal gate only — <em>not</em> a whitelist.
   *
   * <p>Which mobs may become champions is decided by each archetype's
   * {@code entity_filter} ({@link top.theillusivec4.champions.common.filter.EntityFilter}).
   * A mob that no loaded archetype matches simply never gets one, so there is no
   * second filtering mechanism here to keep in sync. Datapack authors control the
   * range entirely from {@code champions/archetype/*.json} — for example by
   * referencing {@code champions:allow_champions} through an entity-tag filter.</p>
   */
  private static boolean isEligible(LivingEntity entity) {
    // Must be a server-side mob
    if (entity.level().isClientSide()) return false;
    if (!(entity instanceof Mob)) return false;

    // Don't upgrade something already a champion
    if (ChampionsApi.get().isChampion(entity)) return false;

    return true;
  }

  /**
   * Beacon protection: no champions within range of an active beacon.
   *
   * <p>This runs from an entity-join hook, which the server fires while it is in the
   * middle of adding entities to a chunk. {@code Level#getChunk(int, int)} requests
   * {@code ChunkStatus.FULL} with {@code load = true}, so on a not-yet-ready chunk it
   * blocks the server thread and pumps the chunk task queue while it waits. Those tasks
   * post further entity-join events, which re-enter this method and block again — the
   * outer wait can then never complete and the server thread deadlocks against itself.
   *
   * <p>{@code getChunkNow} is the non-blocking counterpart: it returns {@code null}
   * instead of loading. A beacon in an unloaded chunk simply does not protect, which is
   * the correct trade-off — never block here.
   */
  private static boolean isNearActiveBeacon(LivingEntity entity, ServerLevel level) {
    int beaconRange = ChampionsConfig.beaconProtectionRange;
    if (beaconRange <= 0) return false;

    BlockPos entityPos  = entity.blockPosition();
    int entityChunkX    = entityPos.getX() >> 4;
    int entityChunkZ    = entityPos.getZ() >> 4;
    int chunkRadius     = (beaconRange >> 4) + 1;
    long rangeSq        = (long) beaconRange * beaconRange;

    for (int cx = entityChunkX - chunkRadius; cx <= entityChunkX + chunkRadius; cx++) {
      for (int cz = entityChunkZ - chunkRadius; cz <= entityChunkZ + chunkRadius; cz++) {
        LevelChunk chunk = level.getChunkSource().getChunkNow(cx, cz);
        if (chunk == null) continue;
        for (BlockEntity be : chunk.getBlockEntities().values()) {
          if (be instanceof BeaconBlockEntity beacon
              && !beacon.getBeamSections().isEmpty()
              && be.getBlockPos().distSqr(entityPos) <= rangeSq) {
            return true;
          }
        }
      }
    }

    return false;
  }

  /** Returns true if there's at least one datapack providing champion configuration. */
  private static boolean hasData() {
    return ChampionsRegistries.archetypes().hasAny();
  }

  // ── Tier selection ────────────────────────────────────────────────────────

  /** Exposed for the champion egg item's random-mode spawn. */
  public static ChampionTier selectRandomTier(RandomSource random) {
    return selectTier(random);
  }

  private static ChampionTier selectTier(RandomSource random) {
    List<ChampionTier> tiers = ChampionsApi.get().getTiers().stream()
        .sorted(Comparator.comparingInt(ChampionTier::level))
        .toList();

    if (tiers.isEmpty()) return null;

    // Build weight list — use TIER_WEIGHTS where available, fall back to 1
    List<Integer> weights = new ArrayList<>();
    for (int i = 0; i < tiers.size(); i++) {
      weights.add(i < DEFAULT_TIER_WEIGHTS.length ? DEFAULT_TIER_WEIGHTS[i] : 1);
    }

    int total = weights.stream().mapToInt(Integer::intValue).sum();
    int roll  = random.nextInt(total);
    int cumulative = 0;

    for (int i = 0; i < tiers.size(); i++) {
      cumulative += weights.get(i);
      if (roll < cumulative) return tiers.get(i);
    }

    return tiers.getLast();
  }
}