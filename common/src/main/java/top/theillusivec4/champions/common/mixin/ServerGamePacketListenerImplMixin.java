package top.theillusivec4.champions.common.mixin;

import net.minecraft.network.protocol.game.ServerboundPickItemFromEntityPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.theillusivec4.champions.api.ChampionsApi;
import top.theillusivec4.champions.common.champion.ChampionData;
import top.theillusivec4.champions.common.champion.ChampionView;
import top.theillusivec4.champions.common.item.ChampionEggItem;

import java.util.List;

import static top.theillusivec4.champions.common.champion.ChampionBuilder.toChampionData;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {
	@Shadow
	public ServerPlayer player;

	@Shadow
	protected abstract void tryPickItem(ItemStack stack);

	@Inject(
			method = "handlePickItemFromEntity",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;tryPickItem(Lnet/minecraft/world/item/ItemStack;)V"),
			cancellable = true
	)
	private void modifyItemStack(ServerboundPickItemFromEntityPacket pickItemFromEntityPacket, CallbackInfo ci) {
		var serverLevel = this.player.level();
		var pickedEntity = serverLevel.getEntity(pickItemFromEntityPacket.id());
		if (pickedEntity instanceof LivingEntity pickedLiving && player != null) {
			var championOptional = ChampionsApi.get().getChampion(pickedLiving);
			if (championOptional.isPresent()) {
				var champion = championOptional.get();

				if (ChampionsApi.get().isChampion(champion.entity())) {
					var type = champion.entity().getType();
					// Preserve the archetype and already-triggered phases so the egg respawns a
					// champion that still runs its phases and keeps fired phase effects.
					List<Identifier> triggered = List.of();
					Identifier archetypeId = null;
					if (champion instanceof ChampionView.Server server) {
						triggered = server.getTriggeredPhaseIds().stream()
								.map(Identifier::parse)
								.toList();
						archetypeId = server.archetypeId().orElse(Identifier.fromNamespaceAndPath("champions", "zombie_line"));
					}
					ChampionData preset = toChampionData(
							champion.tier(), champion.affixes(),
							archetypeId, triggered);
					var createdEgg = ChampionEggItem.createPreset(type, preset);
					// overwrite original tryPickItem invoke
					this.tryPickItem(createdEgg);
					ci.cancel();
				}
			}
		}
	}
}