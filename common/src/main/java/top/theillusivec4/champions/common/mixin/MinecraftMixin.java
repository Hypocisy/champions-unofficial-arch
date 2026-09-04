package top.theillusivec4.champions.common.mixin;
import top.theillusivec4.champions.common.utils.Utils;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import top.theillusivec4.champions.api.ChampionsApi;
import top.theillusivec4.champions.common.champion.ChampionData;
import top.theillusivec4.champions.common.champion.ChampionView;
import top.theillusivec4.champions.common.item.ChampionEggItem;

import java.util.List;

import static top.theillusivec4.champions.common.champion.ChampionBuilder.toChampionData;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

	@ModifyVariable(
			method = "pickBlock", // 目标方法
			at = @At(value = "STORE", target = "Lnet/minecraft/world/entity/player/Inventory;setPickedItem(Lnet/minecraft/world/item/ItemStack;)V")
	)
	private ItemStack modifyItemStack(ItemStack original) {
		var pickedEntity = Minecraft.getInstance().crosshairPickEntity;
		var player = Minecraft.getInstance().player;
		var gameMode = Minecraft.getInstance().gameMode;
		if (pickedEntity instanceof LivingEntity pickedLiving && player != null && gameMode != null) {
			var championOptional = ChampionsApi.get().getChampion(pickedLiving);
			if (championOptional.isPresent()) {
				var champion = championOptional.get();

				if (ChampionsApi.get().isChampion(champion.entity())) {
					var type = champion.entity().getType();
					// Preserve the archetype and already-triggered phases so the egg respawns a
					// champion that still runs its phases and keeps fired phase effects.
					List<ResourceLocation> triggered = List.of();
					ResourceLocation archetypeId = null;
					if (champion instanceof ChampionView.Server server) {
						triggered = server.getTriggeredPhaseIds().stream()
								.map(ResourceLocation::new)
								.toList();
						archetypeId = server.archetypeId().orElse(Utils.key("zombie_line"));
					}
					ChampionData preset = toChampionData(
							champion.tier(), champion.affixes(),
							archetypeId, triggered);
					return ChampionEggItem.createPreset(type, preset);
				}
			}
		}
		return original;
	}
}
