package top.theillusivec4.champions.common.client;
import top.theillusivec4.champions.common.utils.Utils;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.champions.api.ChampionsApi;
import top.theillusivec4.champions.api.affix.AffixInstance;
import top.theillusivec4.champions.api.champion.ChampionTier;
import top.theillusivec4.champions.common.config.ChampionsClientConfig;

import java.util.List;
import java.util.stream.Collectors;

public final class HUDHelper {

    private static final ResourceLocation GUI_BARS =
            Utils.key("textures/gui/bars.png");
    private static final ResourceLocation GUI_STAR =
            Utils.key("textures/gui/staricon.png");

    private HUDHelper() {
    }

    /**
     * Renders the champion health bar, tier stars, name, and affix list.
     *
     * @return true if something was rendered
     */
    public static boolean renderHealthBar(GuiGraphics gui, LivingEntity entity) {
        return ChampionsApi.get().getChampion(entity).map(champion -> {
            ChampionTier tier = champion.tier();
            List<AffixInstance> affixes = champion.affixes();
            if (affixes.isEmpty() && tier.level() < 1) return false;

            Minecraft mc = Minecraft.getInstance();
            int screenW = mc.getWindow().getGuiScaledWidth();
            int barX = screenW / 2 - 91;             // same anchor as vanilla XP bar
            int barY = 21;
            int xOff = ChampionsClientConfig.hudXOffset;
            int yOff = ChampionsClientConfig.hudYOffset;

            int color = tier.display().color();
            float r = FastColor.ARGB32.red(color) / 255.0F;
            float g = FastColor.ARGB32.green(color) / 255.0F;
            float b = FastColor.ARGB32.blue(color) / 255.0F;

            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(r, g, b, 1.0F);
            RenderSystem.enableBlend();

            // ── Health bar background ─────────────────────────────────────────────
            gui.blit(GUI_BARS, xOff + barX, yOff + barY, 0, 60, 182, 5, 256, 256);

            // ── Health fill ───────────────────────────────────────────────────────
            int fillW = (int) ((entity.getHealth() / entity.getMaxHealth()) * 183.0F);
            if (fillW > 0) {
                gui.blit(GUI_BARS, xOff + barX, yOff + barY, 0, 65, fillW, 5, 256, 256);
            }

            // ── Tier stars ────────────────────────────────────────────────────────
            int level = tier.level();
            if (level <= 18) {
                int starsX = xOff + screenW / 2 - 5 - 5 * (level - 1);
                for (int i = 0; i < level; i++) {
                    gui.blit(GUI_STAR, starsX, yOff + 1, 0, 0, 9, 9, 9, 9);
                    starsX += 10;
                }
            } else {
                // Too many stars — show "★ xN" instead
                int starsX = xOff + screenW / 2 - 5;
                String countStr = "x" + level;
                gui.blit(GUI_STAR, starsX - mc.font.width(countStr) / 2, yOff + 1,
                        0, 0, 9, 9, 9, 9);
                drawString(gui, mc.font, countStr,
                        starsX + 10 - mc.font.width(countStr) / 2.0F, yOff + 2,
                        0xFFFFFF, true);
            }

            // ── Champion name ─────────────────────────────────────────────────────
            String name;
            if (entity.getCustomName() != null) {
                name = entity.getCustomName().getString();
            } else {
                // "Tier 3 Zombie" style — fall back to translation key if present
                String tierTitle = Component
                        .translatableWithFallback("rank.champions.title." + level, "Tier " + level)
                        .getString();
                name = tierTitle + " " + entity.getName().getString();
            }
            drawString(gui, mc.font, name,
                    xOff + (float) (screenW / 2 - mc.font.width(name) / 2),
                    yOff + (float) (barY - 9),
                    color, true);

            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            // ── Affix names ───────────────────────────────────────────────────────
            String affixLine = affixes.stream()
                    .flatMap(inst -> ChampionsApi.get().getAffixTypeId(inst.type())
                            .map(id -> Component.translatableWithFallback(
                                    "affix." + id.getNamespace() + "." + id.getPath() + ".name",
                                    capitalize(id.getPath())).getString())
                            .stream())
                    .collect(Collectors.joining(" "));

            if (!affixLine.isBlank()) {
                drawString(gui, mc.font, affixLine,
                        xOff + (float) (screenW / 2 - mc.font.width(affixLine) / 2),
                        yOff + (float) (barY + 6),
                        0xFFFFFF, true);
            }

            RenderSystem.disableBlend();
            return true;
        }).orElse(false);
    }

    public static ResourceLocation getGuiStar() {
        return GUI_STAR;
    }

    private static String capitalize(String s) {
        if (s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    public static int drawString(GuiGraphics graphics, Font p_283343_, @Nullable String p_281896_, float p_283569_, float p_283418_, int p_281560_, boolean p_282130_) {
        if (p_281896_ == null) {
            return 0;
        } else {
            int i = p_283343_.drawInBatch(p_281896_, p_283569_, p_283418_, p_281560_, p_282130_, graphics.pose().last().pose(), graphics.bufferSource(), Font.DisplayMode.NORMAL, 0, 15728880, p_283343_.isBidirectional());
            graphics.flush();
            return i;
        }
    }
}