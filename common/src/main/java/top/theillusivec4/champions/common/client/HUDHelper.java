package top.theillusivec4.champions.common.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import top.theillusivec4.champions.api.ChampionsApi;
import top.theillusivec4.champions.api.affix.AffixInstance;
import top.theillusivec4.champions.api.champion.ChampionTier;
import top.theillusivec4.champions.common.config.ChampionsClientConfig;

import java.util.List;
import java.util.stream.Collectors;

public final class HUDHelper {

    private static final Identifier GUI_BARS =
            Identifier.fromNamespaceAndPath("champions", "textures/gui/bars.png");
    private static final Identifier GUI_STAR =
            Identifier.fromNamespaceAndPath("champions", "textures/gui/staricon.png");

    private HUDHelper() {
    }

    /**
     * Renders the champion health bar, tier stars, name, and affix list.
     *
     * @return true if something was rendered
     */
    public static boolean renderHealthBar(GuiGraphicsExtractor gui, LivingEntity entity) {
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

            // ── Health bar background ─────────────────────────────────────────────
            gui.blit(RenderPipelines.GUI_TEXTURED, GUI_BARS, xOff + barX, yOff + barY, 0, 60, 182, 5, 256, 256, color);

            // ── Health fill ───────────────────────────────────────────────────────
            int fillW = (int) ((entity.getHealth() / entity.getMaxHealth()) * 183.0F);
            if (fillW > 0) {
                gui.blit(RenderPipelines.GUI_TEXTURED, GUI_BARS, xOff + barX, yOff + barY, 0, 65, fillW, 5, 256, 256, color);
            }

            // ── Tier stars ────────────────────────────────────────────────────────
            int level = tier.level();
            if (level <= 18) {
                int starsX = xOff + screenW / 2 - 5 - 5 * (level - 1);
                for (int i = 0; i < level; i++) {
                    gui.blit(RenderPipelines.GUI_TEXTURED, GUI_STAR, starsX, yOff + 1, 0, 0, 9, 9, 9, 9, color);
                    starsX += 10;
                }
            } else {
                // Too many stars — show "★ xN" instead
                int starsX = xOff + screenW / 2 - 5;
                String countStr = "x" + level;
                gui.blit(RenderPipelines.GUI_TEXTURED,GUI_STAR, starsX - mc.font.width(countStr) / 2, yOff + 1,
                        0, 0, 9, 9, 9, 9, color);
                gui.text(mc.font, countStr,
		                starsX + 10 - mc.font.width(countStr) / 2, yOff + 2,
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
            gui.text(mc.font,
                    name,
                    xOff + screenW / 2 - mc.font.width(name) / 2,
                    yOff + barY - 9,
                    color,
                    true);

            // ── Affix names ───────────────────────────────────────────────────────
            String affixLine = affixes.stream()
                    .flatMap(inst -> ChampionsApi.get().getAffixTypeId(inst.type())
                            .map(id -> Component.translatableWithFallback(
                                    "affix." + id.getNamespace() + "." + id.getPath() + ".name",
                                    capitalize(id.getPath())).getString())
                            .stream())
                    .collect(Collectors.joining(" "));

            if (!affixLine.isBlank()) {
                gui.text(mc.font, affixLine,
                        xOff + screenW / 2 - mc.font.width(affixLine) / 2,
                        yOff + barY + 6,
                        0xFFFFFFFF, true);
            }
            return true;
        }).orElse(false);
    }

    public static Identifier getGuiStar() {
        return GUI_STAR;
    }

    private static String capitalize(String s) {
        if (s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}