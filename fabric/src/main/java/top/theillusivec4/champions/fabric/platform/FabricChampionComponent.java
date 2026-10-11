package top.theillusivec4.champions.fabric.platform;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.ladysnake.cca.api.v3.component.Component;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v8.component.CardinalComponent;
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
public final class FabricChampionComponent implements CardinalComponent, EntityComponentInitializer {

    // ── Component keys ────────────────────────────────────────────────────────

    /**
     * Server-side key — persisted to NBT via {@link ChampionData#CODEC}.
     */
    public static final ComponentKey<FabricChampionComponent> SERVER =
            ComponentRegistry.getOrCreate(
                    Identifier.fromNamespaceAndPath("champions", "champion_data"),
                    FabricChampionComponent.class
            );

    /**
     * Client-side key — transient, rebuilt from sync packets.
     */
    public static final ComponentKey<FabricChampionComponent> CLIENT =
            ComponentRegistry.getOrCreate(
                    Identifier.fromNamespaceAndPath("champions", "champion_data_client"),
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
    public void readData(ValueInput readView) {
        readView.read("data",ChampionData.CODEC).ifPresentOrElse(championData -> {
            this.data = championData;
        }, () -> this.data=ChampionData.EMPTY);
    }

    @Override
    public void writeData(ValueOutput writeView) {
        writeView.store("data", ChampionData.CODEC, data);
    }
}
