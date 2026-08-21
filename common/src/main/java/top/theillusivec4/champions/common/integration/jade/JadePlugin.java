package top.theillusivec4.champions.common.integration.jade;

import net.minecraft.world.entity.LivingEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import top.theillusivec4.champions.api.ChampionsApi;
import top.theillusivec4.champions.common.config.ChampionsConfig;

@WailaPlugin
public class JadePlugin implements IWailaPlugin {

    // Bottom edge of the Champions HUD health bar area (barY=21, affix line ~27, + margin)
    private static final int HUD_BOTTOM_Y = 38;

    @Override
    public void register(IWailaCommonRegistration registration) {
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerEntityComponent(ChampionComponentProvider.INSTANCE, LivingEntity.class);

        // Push Jade tooltip below our HUD health bar when hovering a champion.
        // Use an absolute floor rather than a relative offset to avoid accumulation
        // across multiple callback invocations per frame.
        registration.addBeforeRenderCallback((box, rect, gui, accessor) -> {
            if (!ChampionsConfig.showHud) return false;
            if (!(accessor.getTarget() instanceof LivingEntity living)) return false;
            if (!ChampionsApi.get().isChampion(living)) return false;
            if (rect.rect.getY() < HUD_BOTTOM_Y) {
                rect.rect.setY(HUD_BOTTOM_Y);
            }
            return false;
        });
    }
}
