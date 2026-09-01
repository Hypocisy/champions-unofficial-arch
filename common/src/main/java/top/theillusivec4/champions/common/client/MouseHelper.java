package top.theillusivec4.champions.common.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import top.theillusivec4.champions.api.ChampionsApi;
import top.theillusivec4.champions.common.config.ChampionsClientConfig;

import java.util.Optional;

public final class MouseHelper {

	private MouseHelper() {
	}

	/**
	 * Raycasts from the camera entity and returns the first champion entity hit,
	 * up to {@link Attributes#ENTITY_INTERACTION_RANGE} blocks away.
	 *
	 * <p>Uses the same entity hit detection as vanilla crosshair targeting,
	 * so range is always based on actual line-of-sight, not bounding-box distance.</p>
	 */
	public static Optional<LivingEntity> getMouseOverChampion(Minecraft mc, float partialTick) {
		Entity camera = mc.getCameraEntity();
		if (!(camera instanceof LivingEntity)) return Optional.empty();
		if (mc.level == null) return Optional.empty();

		mc.getProfiler().push("mouse_champion");

		double range = ((LivingEntity) camera).getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE);
		// if set, use hudRange instead entity_interaction_range
		if (ChampionsClientConfig.hudRange > 0) {
			range = ChampionsClientConfig.hudRange;
		}

		HitResult blockHit = camera.pick(range, partialTick, false);
		Vec3 eye = camera.getEyePosition(partialTick);
		// Only consider up to the nearest block hit so walls occlude champions
		double blockDist = blockHit.getLocation().distanceToSqr(eye);

		Vec3 viewDir = camera.getViewVector(1.0F);
		Vec3 end = eye.add(viewDir.x * range, viewDir.y * range, viewDir.z * range);
		AABB searchBox = camera.getBoundingBox()
				.expandTowards(viewDir.scale(range))
				.inflate(1.0D, 1.0D, 1.0D);

		EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
				camera, eye, end, searchBox,
				e -> !e.isSpectator() && e.isPickable(),
				blockDist);

		mc.getProfiler().pop();

		if (entityHit == null || !(entityHit.getEntity() instanceof LivingEntity living) || !ChampionsApi.get().isChampion(living)) {
			return Optional.empty();
		}

		return Optional.of(living);
	}
}