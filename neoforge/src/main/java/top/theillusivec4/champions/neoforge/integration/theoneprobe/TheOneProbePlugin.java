package top.theillusivec4.champions.neoforge.integration.theoneprobe;

import mcjty.theoneprobe.api.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import top.theillusivec4.champions.api.ChampionsApi;
import top.theillusivec4.champions.neoforge.ChampionsNeoForge;

import java.util.function.Function;

public final class TheOneProbePlugin implements IProbeInfoEntityProvider {

    @Override
    public String getID() {
        return ChampionsNeoForge.MOD_ID + ":entity.champion";
    }

    @Override
    public void addProbeEntityInfo(ProbeMode mode, IProbeInfo info, Player player,
                                   Level level, Entity entity,
                                   IProbeHitEntityData data) {
        ChampionsApi.get().getChampion((LivingEntity) entity).ifPresent(champion -> {
            var tier = champion.tier();
            int color = tier.display().color();
            int r = FastColor.ARGB32.red(color);
            int g = FastColor.ARGB32.green(color);
            int b = FastColor.ARGB32.blue(color);

            Color rankColor = new Color(r, g, b);
            IProbeInfo vertical = info.vertical(
                    info.defaultLayoutStyle().borderColor(rankColor).spacing(3).padding(3));

            vertical.mcText(
                    Component.translatableWithFallback(
                                    "rank.champions.title." + tier.level(),
                                    "Tier " + tier.level())
                            .append(" (" + tier.level() + ")")
                            .setStyle(Style.EMPTY.withUnderlined(true).withColor(color)));

            for (var instance : champion.affixes()) {
                ChampionsApi.get().getAffixTypeId(instance.type()).ifPresent(id -> {
                    IProbeInfo row = vertical.horizontal();
                    row.mcText(Component.translatableWithFallback(
                            "affix." + id.getNamespace() + "." + id.getPath() + ".name",
                            id.getPath()));
                });
            }
        });
    }

    public static final class RegisterFunction implements Function<ITheOneProbe, Void> {
        @Override
        public Void apply(ITheOneProbe probe) {
            probe.registerEntityProvider(new TheOneProbePlugin());
            return null;
        }
    }
}
