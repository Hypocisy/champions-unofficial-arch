package top.theillusivec4.champions.fabric.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.damagesource.DamageEffects;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageType;
import top.theillusivec4.champions.common.registry.ModDamageTypes;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

// register damage type
public class FabricDamageTypeProvider extends FabricDynamicRegistryProvider {

	public FabricDamageTypeProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
		super(output, registriesFuture);
	}

	@Override
	@Nonnull
	public String getName() {
		return "Champions Damage Types";
	}

	@Override
	protected void configure(HolderLookup.Provider registries, Entries entries) {
		entries.add(ModDamageTypes.REFLECTION, new DamageType("reflection", DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER, 0.1F));
		entries.add(ModDamageTypes.ENKINDLING_BULLET, new DamageType("enkindling_bullet", DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER, 0.1F, DamageEffects.BURNING));
	}
}