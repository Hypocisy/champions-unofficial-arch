package top.theillusivec4.champions.fabric.event;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.LivingEntity;
import top.theillusivec4.champions.api.ChampionsApi;
import top.theillusivec4.champions.api.champion.ChampionTier;
import top.theillusivec4.champions.common.client.ChampionsOverlay;
import top.theillusivec4.champions.common.config.ChampionsConfig;
import top.theillusivec4.champions.fabric.registry.ModParticles;

/**
 * Registers client-side rendering hooks on Fabric.
 * Call {@link #register()} from {@code ChampionsFabricClient.onInitializeClient()}.
 */
public final class FabricClientRenderer {

  private FabricClientRenderer() {}

  private static final ChampionsOverlay OVERLAY = new ChampionsOverlay();

  public static void register() {
    registerParticles();
    registerHud();
  }

  // ── Particles ─────────────────────────────────────────────────────────────

  /**
   * Spawns rank particles around champion entities each client tick.
   *
   * <p>Mirrors NeoForge {@code ClientModEventHandler.GameBus#onLevelTick}:
   * computes tier color as r/g/b floats and passes them as the velocity
   * arguments to {@code addParticle} — {@link top.theillusivec4.champions.common.particle.RankParticle.RankFactory}
   * reads them as color, not movement.</p>
   */
  private static void registerParticles() {
    ClientTickEvents.END_CLIENT_TICK.register(client -> {
      if (client.level == null || client.isPaused()) return;
      if (!ChampionsConfig.showParticles) return;

      client.level.entitiesForRendering().forEach(entity -> {
        if (!(entity instanceof LivingEntity living)) return;
        // Stagger: one particle per entity per tick, offset by entity id
        if ((living.tickCount + living.getId()) % 2 != 0) return;

        ChampionsApi.get().getChampion(living).ifPresent(champion -> {
          ChampionTier tier = champion.tier();
          int color = tier.display().color();
          float r = FastColor.ARGB32.red(color)   / 255.0F;
          float g = FastColor.ARGB32.green(color) / 255.0F;
          float b = FastColor.ARGB32.blue(color)  / 255.0F;

          double w = living.getBbWidth();
          double h = living.getBbHeight();
          client.level.addParticle(
              ModParticles.MAGIC,
              living.getX() + (client.level.random.nextDouble() - 0.5D) * w,
              living.getY() + client.level.random.nextDouble() * h,
              living.getZ() + (client.level.random.nextDouble() - 0.5D) * w,
              r, g, b   // RankParticle.RankFactory reads these as color
          );
        });
      });
    });
  }

  // ── HUD ───────────────────────────────────────────────────────────────────

  /**
   * Registers the champion health overlay.
   *
   * <p>{@link ChampionsOverlay} implements {@code LayeredDraw.Layer} whose
   * {@code render(GuiGraphics, DeltaTracker)} signature matches the
   * {@code HudRenderCallback} lambda exactly, so we delegate directly.</p>
   */
  private static void registerHud() {
    HudRenderCallback.EVENT.register(OVERLAY::render);
  }
}
