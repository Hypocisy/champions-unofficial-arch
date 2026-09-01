package top.theillusivec4.champions.common.integration.jade;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;
import top.theillusivec4.champions.api.ChampionsApi;
import top.theillusivec4.champions.api.affix.AffixInstance;
import top.theillusivec4.champions.api.champion.ChampionTier;
import top.theillusivec4.champions.common.config.ChampionsClientConfig;

public enum ChampionComponentProvider implements IEntityComponentProvider {
    INSTANCE;

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath("champions", "enable_affix_compact");

    private static final int STAR_SPACING = 1;

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        ChampionsApi.get().getChampion((LivingEntity) accessor.getEntity()).ifPresent(champion -> {
            ChampionTier tier = champion.tier();
            int color = tier.display().color();

            // Replace entity name with colored champion title
            Component name = Component
                    .translatableWithFallback("rank.champions.title." + tier.level(), "Tier " + tier.level())
                    .append(" ")
                    .append(accessor.getEntity().getName())
                    .withColor(color);
            tooltip.replace(JadeIds.CORE_OBJECT_NAME, name);

            // Star row below the name
            tooltip.add(1, StarElement.of(tier.level(), color, ChampionsClientConfig.jadeStarSpacing));

            // One line per affix with strength
            for (AffixInstance instance : champion.affixes()) {
                ChampionsApi.get().getAffixTypeId(instance.type()).ifPresent(id -> {
                    Component affixName = Component.translatableWithFallback(
                            "affix." + id.getNamespace() + "." + id.getPath() + ".name",
                            capitalize(id.getPath()));
                    Component line = affixName
                            .copy()
                            .append(" ")
                            .append(Component.literal("(" + instance.strength() + ")"));
                    tooltip.add(line);
                });
            }
        });
    }

    @Override
    public ResourceLocation getUid() {
        return ID;
    }

    private static String capitalize(String s) {
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
