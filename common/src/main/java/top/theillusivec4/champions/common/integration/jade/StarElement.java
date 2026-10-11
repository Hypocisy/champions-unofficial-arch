package top.theillusivec4.champions.common.integration.jade;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;
import snownee.jade.api.ui.Element;
import top.theillusivec4.champions.common.client.HUDHelper;
import top.theillusivec4.champions.common.config.ChampionsClientConfig;

public final class StarElement extends Element {

    private final int starCount;
    private final int spacing;
    private final int argbColor;

    private StarElement(int starCount, int argbColor, int spacing) {
        this.starCount = starCount;
        this.spacing = spacing;
        this.argbColor = argbColor;

        this.width = starCount * 9 + (starCount - 1) * spacing;
        this.height = 9 + ChampionsClientConfig.jadeStarBottomPadding;
    }

    public static StarElement of(int starCount, int argbColor, int spacing) {
        return new StarElement(starCount, argbColor, spacing);
    }


    @Override
    public @Nullable Component getNarration() {
        return null;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor gui,int mouseX, int mouseY, float partialTicks) {
        for (int i = 0; i < starCount; i++) {
            gui.blit(RenderPipelines.GUI_TEXTURED, HUDHelper.getGuiStar(),
                    this.getX() + mouseX + i * (9 + spacing),
                    this.getY() + mouseY,
                    0, 0, 9, 9, 9, 9, this.argbColor);
        }
    }
}
