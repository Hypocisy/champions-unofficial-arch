package top.theillusivec4.champions.neoforge.event;

import net.minecraft.client.Minecraft;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
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

  // ── MOD bus ───────────────────────────────────────────────────────────────

  @EventBusSubscriber(
          modid = ChampionsNeoForge.MOD_ID,
          bus   = EventBusSubscriber.Bus.MOD,
          value = Dist.CLIENT
  )
  public static final class ModBus {

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent evt) {
      // Register BELOW the vanilla boss overlay — our bar is at the hotbar,
      // so there's no visual conflict, but keeping this order lets other mods
      // that hook boss overlay see a clean state.
      evt.registerBelow(
              VanillaGuiLayers.BOSS_OVERLAY,
              fromNamespaceAndPath(ChampionsNeoForge.MOD_ID, "health_overlay"),
              new ChampionsOverlay()
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

  @EventBusSubscriber(
          modid = ChampionsNeoForge.MOD_ID,
          bus   = EventBusSubscriber.Bus.GAME,
          value = Dist.CLIENT
  )
  public static final class GameBus {

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level == null || mc.level != event.getLevel()) return;
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