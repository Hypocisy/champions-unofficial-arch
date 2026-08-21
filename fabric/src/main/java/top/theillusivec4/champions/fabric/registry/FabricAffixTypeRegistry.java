package top.theillusivec4.champions.fabric.registry;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import top.theillusivec4.champions.api.affix.AffixType;
import top.theillusivec4.champions.common.registry.AffixTypeRegistry;

import java.util.Collection;
import java.util.Optional;

/**
 * Fabric implementation of {@link AffixTypeRegistry}.
 *
 * <p>Backed by a custom {@link Registry} built via Fabric's {@link FabricRegistryBuilder}.
 * The registry key is identical to the NeoForge side so datapack-referenced ids
 * are the same across both platforms.</p>
 *
 * <h3>Registering an affix type on Fabric:</h3>
 * <pre>{@code
 * Registry.register(
 *     FabricAffixTypeRegistry.REGISTRY,
 *     ResourceLocation.fromNamespaceAndPath("champions", "adaptable"),
 *     new AdaptableAffix()
 * );
 * }</pre>
 *
 * <p>With Architectury (planned), this registration call becomes identical on both
 * platforms via {@code DeferredRegister}, eliminating the need for this class.</p>
 */
public final class FabricAffixTypeRegistry implements AffixTypeRegistry {

    public static final ResourceKey<Registry<AffixType<?>>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(
                    ResourceLocation.fromNamespaceAndPath("champions", "affix_type")
            );

    /**
     * The built registry. Available after {@link #bootstrap()} is called from the
     * mod initializer — before that, this field is null.
     */
    public static Registry<AffixType<?>> REGISTRY;

    /**
     * Build and register the custom registry.
     * Must be called before any affix types are registered.
     */
    public static void bootstrap() {
        REGISTRY = FabricRegistryBuilder
                .createSimple(REGISTRY_KEY)
                .buildAndRegister();

        // Fire initHandlers for each affix type as it is registered
        // Fabric fires a RegistryEntryAddedCallback equivalent via the sync registry event
        RegistryEntryAddedCallback
                .event(REGISTRY)
                .register((rawId, id, value) -> value.initHandlers());
    }

    // ── AffixTypeRegistry ─────────────────────────────────────────────────────

    @Override
    public Optional<AffixType<?>> get(ResourceLocation id) {
        return REGISTRY.getOptional(id);
    }

    @Override
    public Optional<ResourceLocation> getId(AffixType<?> type) {
        return Optional.ofNullable(REGISTRY.getKey(type));
    }

    @Override
    public Collection<AffixType<?>> getAll() {
        return REGISTRY.stream().toList();
    }
}
