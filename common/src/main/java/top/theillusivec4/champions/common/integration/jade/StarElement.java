package top.theillusivec4.champions.common.integration.jade;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.FastColor;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.ui.Element;
import top.theillusivec4.champions.common.client.HUDHelper;
import top.theillusivec4.champions.common.config.ChampionsClientConfig;

public final class StarElement extends Element {

    private final int starCount;
    private final int spacing;
    private final float r, g, b;

    private StarElement(int starCount, int argbColor, int spacing) {
        this.starCount = starCount;
        this.spacing = spacing;
        this.r = FastColor.ARGB32.red(argbColor) / 255.0F;
        this.g = FastColor.ARGB32.green(argbColor) / 255.0F;
        this.b = FastColor.ARGB32.blue(argbColor) / 255.0F;
    }

    public static StarElement of(int starCount, int argbColor, int spacing) {
        return new StarElement(starCount, argbColor, spacing);
    }

    @Override
    public Vec2 getSize() {
        return new Vec2(starCount * 9 + (starCount - 1) * spacing, 9 + ChampionsClientConfig.jadeStarBottomPadding);
    }

    @Override
    public void render(GuiGraphics gui, float x, float y, float maxX, float maxY) {
        RenderSystem.setShaderColor(r, g, b, 1.0F);
        for (int i = 0; i < starCount; i++) {
            gui.blit(HUDHelper.getGuiStar(),
                    (int) (x + i * (9 + spacing)), (int) y,
                    0, 0, 9, 9, 9, 9);
        }
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
    }
}
