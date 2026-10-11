package top.theillusivec4.champions.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.minecraft.client.renderer.entity.EntityRenderers;
import top.theillusivec4.champions.common.client.renderer.ColorizedBulletRenderer;
import top.theillusivec4.champions.common.particle.RankParticle;
import top.theillusivec4.champions.fabric.event.FabricClientRenderer;
import top.theillusivec4.champions.fabric.network.FabricPacketHandler;
import top.theillusivec4.champions.fabric.platform.FabricAttachmentProvider;
import top.theillusivec4.champions.fabric.registry.ModEntityTypes;
import top.theillusivec4.champions.fabric.registry.ModParticles;
import top.theillusivec4.champions.platform.ChampionAttachmentProvider;

public final class ChampionsFabricClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		FabricPacketHandler.registerClientHandlers((FabricAttachmentProvider) ChampionAttachmentProvider.Holder.get());
		FabricClientRenderer.register();
		ParticleProviderRegistry.getInstance().register(
				ModParticles.MAGIC,
				RankParticle.RankFactory::new
		);

		// Register bullet entity renderers.
		// Bullets are invisible (no model) — their visual is the trail particle
		// emitted in BaseBulletEntity#tick on the client. Without a registered
		// renderer the EntityRenderDispatcher returns null and crashes at
		// EntityRenderDispatcher#shouldRender when the entity enters render range.
		EntityRenderers.register(ModEntityTypes.ARCTIC_BULLET, (renderManager) -> new ColorizedBulletRenderer<>(renderManager, 0x42F5E3));
		EntityRenderers.register(ModEntityTypes.ENKINDLING_BULLET, (renderManager) -> new ColorizedBulletRenderer<>(renderManager, 0xFC5A03));
	}
}
