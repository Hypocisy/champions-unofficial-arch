package top.theillusivec4.champions.common.datagen;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.champions.common.entity.ChampionEntityTypes;

import java.util.concurrent.CompletableFuture;


public class ModEntityTypeTagsProvider extends TagsProvider<EntityType<?>> {


	public ModEntityTypeTagsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> completableFuture) {
		super(packOutput, Registries.ENTITY_TYPE, completableFuture);
	}

	@Override
	protected void addTags(@NotNull HolderLookup.Provider provider) {
		tag(ChampionEntityTypes.Tags.IS_ENDER).add(lookup(provider, "ender_dragon"));
		tag(ChampionEntityTypes.Tags.IS_ENDER).add(lookup(provider, "endermite"));
		tag(ChampionEntityTypes.Tags.IS_ENDER).add(lookup(provider, "enderman"));
		tag(ChampionEntityTypes.Tags.IS_ENDER).add(lookup(provider, "shulker"));
		// add champion allow list
		lookUpMonster(provider).listElements().forEach(this::addEntity);
	}

	void addEntity(Holder.Reference<EntityType<?>> entityType) {
		tag(ChampionEntityTypes.Tags.ALLOW_CHAMPIONS).add(entityType.key());
	}

	private HolderLookup.RegistryLookup<EntityType<?>> lookUpMonster(@NotNull HolderLookup.Provider provider) {
		return provider.lookupOrThrow(Registries.ENTITY_TYPE).filterElements(entityType -> entityType.getCategory() == MobCategory.MONSTER);
	}

	private ResourceKey<EntityType<?>> create(String name) {
		return ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.parse(name));
	}

	private ResourceKey<EntityType<?>> lookup(HolderLookup.Provider provider, String name) {
		return provider.lookupOrThrow(Registries.ENTITY_TYPE).getOrThrow(create(name)).key();
	}
}
