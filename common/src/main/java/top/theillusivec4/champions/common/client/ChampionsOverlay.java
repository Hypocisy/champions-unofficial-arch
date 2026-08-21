package top.theillusivec4.champions.common.client;


import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.champions.common.config.ChampionsConfig;

import java.util.Optional;

public final class ChampionsOverlay implements LayeredDraw.Layer {

  /** Set to true while the overlay is rendering — lets other systems check state. */
  public static boolean isRendering = false;

  @Override
  public void render(@NotNull GuiGraphics gui, @NotNull DeltaTracker delta) {
    if (!ChampionsConfig.showHud) {
      isRendering = false;
      return;
    }

    Minecraft mc = Minecraft.getInstance();
    Optional<LivingEntity> champion =
        MouseHelper.getMouseOverChampion(mc, delta.getGameTimeDeltaTicks());

    isRendering = champion
        .filter(e -> !isOnBossBarBlacklist(e))
        .map(e -> HUDHelper.renderHealthBar(gui, e))
        .orElse(false);
  }

  /**
   * Returns true for entities that are on the config boss-bar blacklist.
   * Those entities have their own vanilla boss bar — our HUD would overlap.
   */
  private static boolean isOnBossBarBlacklist(LivingEntity entity) {
    String id = entity.getType().builtInRegistryHolder().key().location().toString();
    return ChampionsConfig.bossBarBlacklist.contains(id);
  }
}
