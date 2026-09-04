package top.theillusivec4.champions.common.client;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.LivingEntity;
import top.theillusivec4.champions.common.config.ChampionsConfig;

import java.util.Optional;

/**
 * Mouseover champion health bar. Loader-neutral: platform modules register
 * this via their HUD hook (Forge IGuiOverlay / Fabric HudRenderCallback)
 * and forward the partial-tick value.
 */
public final class ChampionsOverlay {

  /** Set to true while the overlay is rendering — lets other systems check state. */
  public static boolean isRendering = false;

  public void render(GuiGraphics gui, float partialTick) {
    if (!ChampionsConfig.showHud) {
      isRendering = false;
      return;
    }

    Minecraft mc = Minecraft.getInstance();
    Optional<LivingEntity> champion =
        MouseHelper.getMouseOverChampion(mc, partialTick);

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
    String id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
    return ChampionsConfig.bossBarBlacklist.contains(id);
  }
}
