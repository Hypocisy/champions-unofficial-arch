package top.theillusivec4.champions.neoforge.event;

import net.minecraft.client.Minecraft;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.champions.api.ChampionsApi;
import top.theillusivec4.champions.api.champion.ChampionTier;
import top.theillusivec4.champions.common.client.ChampionsOverlay;
import top.theillusivec4.champions.common.client.renderer.ColorizedBulletRenderer;
import top.theillusivec4.champions.common.config.ChampionsConfig;
import top.theillusivec4.champions.common.particle.RankParticle;
import top.theillusivec4.champions.neoforge.ChampionsNeoForge;
import top.theillusivec4.champions.neoforge.registry.ModEntityTypes;
import top.theillusivec4.champions.neoforge.registry.ModParticleTypes;

import static net.minecraft.resources.ResourceLocation.fromNamespaceAndPath;

@SuppressWarnings("removal")
public final class ClientModEventHandler {

  private static final ChampionsOverlay OVERLAY = new ChampionsOverlay();

  // ── MOD bus ───────────────────────────────────────────────────────────────

  @Mod.EventBusSubscriber(
          modid = ChampionsNeoForge.MOD_ID,
          bus   = Mod.EventBusSubscriber.Bus.MOD,
          value = Dist.CLIENT
  )
  public static final class ModBus {

    @SubscribeEvent
    public static void onRegisterGuiOverlays(RegisterGuiOverlaysEvent evt) {
      // Register BELOW the vanilla boss overlay — our bar is at the hotbar,
      // so there's no visual conflict, but keeping this order lets other mods
      // that hook boss overlay see a clean state.
      evt.registerBelow(
		      VanillaGuiOverlay.BOSS_EVENT_PROGRESS.id(),
              fromNamespaceAndPath(ChampionsNeoForge.MOD_ID, "health_overlay").getPath(),
              (gui, guiGraphics, partialTick, screenWidth, screenHeight) ->
                      OVERLAY.render(guiGraphics, partialTick)
      );
    }

    @SubscribeEvent
    public static void onRegisterParticleProviders(RegisterParticleProvidersEvent evt) {
      evt.registerSpriteSet(ModParticleTypes.RANK.get(), RankParticle.RankFactory::new);
    }

    @SubscribeEvent
    public static void onRegisterEntityRenderers(EntityRenderersEvent.RegisterRenderers evt) {
      // Bullets are invisible (no model) — their visual is the trail particle.
      // Without a registered renderer the game crashes when the entity enters view range.
      evt.registerEntityRenderer(ModEntityTypes.ARCTIC_BULLET.get(),
              (renderManager) -> new ColorizedBulletRenderer(renderManager, 0x42F5E3));
      evt.registerEntityRenderer(ModEntityTypes.ENKINDLING_BULLET.get(),
              (renderManager) -> new ColorizedBulletRenderer(renderManager, 0xFC5A03));
    }
  }

  // ── GAME bus ──────────────────────────────────────────────────────────────

  @Mod.EventBusSubscriber(
          modid = ChampionsNeoForge.MOD_ID,
          bus   = Mod.EventBusSubscriber.Bus.FORGE,
          value = Dist.CLIENT
  )
  public static final class GameBus {

    /**
     * Cancel the vanilla boss bar while our mouseover HUD is rendering,
     * so entities with their own boss bar don't double up.
     * Lives in the client-only subscriber class — registering it from a
     * common @EventBusSubscriber class would crash dedicated servers.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBossBar(CustomizeGuiOverlayEvent.BossEventProgress event) {
      if (ChampionsOverlay.isRendering) {
        event.setCanceled(true);
      }
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
      if (event.phase != TickEvent.Phase.END) return;
      Level level = event.level;
      if (!level.isClientSide()) return;

      Minecraft mc = Minecraft.getInstance();
      if (mc.level == null || mc.level != level) return;
      if (mc.isPaused()) return;
      if (!ChampionsConfig.showParticles) return;

      mc.level.entitiesForRendering().forEach(entity -> {
        if (!(entity instanceof LivingEntity living)) return;
        // Stagger spawning: one particle per entity per tick, offset by id
        if ((living.tickCount + living.getId()) % 2 != 0) return;

        ChampionsApi.get().getChampion(living).ifPresent(champion -> {
          ChampionTier tier = champion.tier();
          int color = tier.display().color();
          float r = FastColor.ARGB32.red(color)   / 255.0F;
          float g = FastColor.ARGB32.green(color) / 255.0F;
          float b = FastColor.ARGB32.blue(color)  / 255.0F;

          double w = living.getBbWidth();
          double h = living.getBbHeight();
          // Random position within entity bounding box (original behaviour)
          mc.level.addParticle(
                  ModParticleTypes.RANK.get(),
                  living.getX() + (mc.level.random.nextDouble() - 0.5D) * w,
                  living.getY() + mc.level.random.nextDouble() * h,
                  living.getZ() + (mc.level.random.nextDouble() - 0.5D) * w,
                  r, g, b   // color passed as velocity — RankParticle.RankFactory reads them
          );
        });
      });
    }
  }
}
