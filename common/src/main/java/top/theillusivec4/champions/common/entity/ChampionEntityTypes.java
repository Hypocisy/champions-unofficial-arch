package top.theillusivec4.champions.common.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import java.util.function.Supplier;

/**
 * Holds the two custom entity types used by Champions bullets.
 *
 * <p>Platform modules call {@link #register(Supplier, Supplier)} at startup.
 * Affix code references these suppliers instead of platform registries directly.</p>
 */
public final class ChampionEntityTypes {


	private static Supplier<EntityType<ArcticBulletEntity>> arcticBulletSupplier = null;
	private static Supplier<EntityType<EnkindlingBulletEntity>> enkindlingBulletSupplier = null;
	public static Supplier<EntityType<ArcticBulletEntity>> ARCTIC_BULLET =
			() -> arcticBulletSupplier.get();
	public static Supplier<EntityType<EnkindlingBulletEntity>> ENKINDLING_BULLET =
			() -> enkindlingBulletSupplier.get();
	private ChampionEntityTypes() {
	}

	public static void register(
			Supplier<EntityType<ArcticBulletEntity>> arctic,
			Supplier<EntityType<EnkindlingBulletEntity>> enkindling) {
		arcticBulletSupplier = arctic;
		enkindlingBulletSupplier = enkindling;
	}

	public static EntityType<ArcticBulletEntity> buildArcticType() {
		return EntityType.Builder.<ArcticBulletEntity>of(
						ArcticBulletEntity::new, MobCategory.MISC)
				.sized(0.3125f, 0.3125f)
				.clientTrackingRange(8)
				.build("arctic_bullet");
	}

	// ── Default EntityType specs (shared by both platforms) ───────────────────

	public static EntityType<EnkindlingBulletEntity> buildEnkindlingType() {
		return EntityType.Builder.<EnkindlingBulletEntity>of(
						EnkindlingBulletEntity::new, MobCategory.MISC)
				.sized(0.3125f, 0.3125f)
				.clientTrackingRange(8)
				.build("enkindling_bullet");
	}

	public static class Tags {
		public static final TagKey<EntityType<?>> IS_ENDER = create("is_ender");
		public static final TagKey<EntityType<?>> ALLOW_CHAMPIONS = create("allow_champions");

		private static TagKey<EntityType<?>> create(String name) {
			return TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath("champions", name));
		}
	}
}
