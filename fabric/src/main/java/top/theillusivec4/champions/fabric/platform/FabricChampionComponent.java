package top.theillusivec4.champions.fabric.platform;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.Component;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import top.theillusivec4.champions.common.champion.ChampionData;

/**
 * Cardinal Components {@link Component} that holds {@link ChampionData} on a
 * {@link LivingEntity}.
 *
 * <p>Two separate keys are used — one for server data (persisted), one for client data
 * (transient, rebuilt from sync) — mirroring the NeoForge two-attachment design.</p>
 *
 * <p>To activate, your mod's {@code fabric.mod.json} must list
 * {@code top.theillusivec4.champions.fabric.platform.FabricChampionComponent}
 * as an entrypoint under {@code "cardinal-components"}.</p>
 */
public final class FabricChampionComponent implements Component, EntityComponentInitializer {

    // ── Component keys ────────────────────────────────────────────────────────

    /**
     * Server-side key — persisted to NBT via {@link ChampionData#CODEC}.
     */
    public static final ComponentKey<FabricChampionComponent> SERVER =
            ComponentRegistry.getOrCreate(
                    ResourceLocation.fromNamespaceAndPath("champions", "champion_data"),
                    FabricChampionComponent.class
            );

    /**
     * Client-side key — transient, rebuilt from sync packets.
     */
    public static final ComponentKey<FabricChampionComponent> CLIENT =
            ComponentRegistry.getOrCreate(
                    ResourceLocation.fromNamespaceAndPath("champions", "champion_data_client"),
                    FabricChampionComponent.class
            );

    // ── EntityComponentInitializer entrypoint ─────────────────────────────────

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        // Attach to all LivingEntities — provider decides whether to populate
        registry.registerFor(
                LivingEntity.class,
                SERVER,
                entity -> new FabricChampionComponent()
        );

        registry.registerFor(
                LivingEntity.class,
                CLIENT,
                entity -> new FabricChampionComponent()
        );
    }

    // ── Component data ────────────────────────────────────────────────────────

    private ChampionData data = ChampionData.EMPTY;

    public ChampionData getData() {
        return data;
    }

    public void setData(ChampionData data) {
        this.data = data;
    }

    public boolean isPresent() {
        return data != ChampionData.EMPTY && data.tierId() != null;
    }

    // ── Component NBT (Cardinal Components contract) ─────────────────────────


    @Override
    public void readFromNbt(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registryLookup) {
        ChampionData.CODEC.parse(NbtOps.INSTANCE, tag)
                .resultOrPartial(err ->
                        System.err.println("[Champions] Failed to read champion component: " + err))
                .ifPresent(parsed -> this.data = parsed);
    }

    @Override
    public void writeToNbt(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registryLookup) {
        ChampionData.CODEC.encodeStart(NbtOps.INSTANCE, data)
                .resultOrPartial(err ->
                        System.err.println("[Champions] Failed to write champion component: " + err))
                .ifPresent(encoded -> {
                    if (encoded instanceof CompoundTag compound) {
                        tag.merge(compound);
                    }
                });
    }
}
