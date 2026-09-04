package top.theillusivec4.champions.neoforge.registry;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;
import top.theillusivec4.champions.api.affix.AffixType;
import top.theillusivec4.champions.common.registry.AffixTypeRegistry;
import top.theillusivec4.champions.neoforge.platform.NeoForgeAffixBootstrap;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * NeoForge implementation of {@link AffixTypeRegistry}.
 *
 * <p>Backed by a custom NeoForge {@link Registry}. Affix types are registered via
 * {@link #register(String, Supplier)} from each affix module's registration class,
 * mirroring NeoForge's standard {@code DeferredRegister} pattern.</p>
 *
 * <h3>Registering a new affix type:</h3>
 * <pre>{@code
 * public class ChampionAffixes {
 *     public static final DeferredHolder<AffixType<?>, AdaptableAffix> ADAPTABLE =
 *         NeoForgeAffixTypeRegistry.register("adaptable", AdaptableAffix::new);
 * }
 * }</pre>
 */
public final class NeoForgeAffixTypeRegistry implements AffixTypeRegistry {

    public static final String MOD_ID = "champions";

    public static final ResourceKey<Registry<AffixType<?>>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(
                    ResourceLocation.fromNamespaceAndPath(MOD_ID, "affix_type")
            );

    // The backing NeoForge registry — built lazily when first accessed after mod init
    private static Supplier<IForgeRegistry<AffixType<?>>> REGISTRY;

    private static final DeferredRegister<AffixType<?>> DEFERRED =
            DeferredRegister.create(REGISTRY_KEY, MOD_ID);

    // ── Public registration API ───────────────────────────────────────────────

    /**
     * Register an affix type.
     *
     * <p>Must be called before {@link #bootstrap(IEventBus)} — i.e. during mod static
     * initialisation, not at runtime.</p>
     *
     * @param name    the path component of the registry id (namespace is always "champions")
     * @param factory supplier that creates the singleton affix type instance
     */
    public static <T extends AffixType<?>> RegistryObject<T>
    register(String name, Supplier<T> factory) {
        return DEFERRED.register(name, factory);
    }

    /**
     * Attach this registry to the mod event bus.
     * Call once from the mod constructor.
     */
    public static void bootstrap(IEventBus modBus) {
        // Create the custom registry and attach it
        DeferredRegister<AffixType<?>> deferred = DEFERRED;
        REGISTRY = deferred.makeRegistry(() ->
                new RegistryBuilder<AffixType<?>>()
                        .disableSync()
                        .onAdd((owner, stage, id, key, value, oldValue) -> {
                            value.initHandlers();
                        })
        );
        deferred.register(modBus);
        NeoForgeAffixBootstrap.bootstrap();
    }

    // ── AffixTypeRegistry impl ────────────────────────────────────────────────

    @Override
    public Optional<AffixType<?>> get(ResourceLocation id) {
        return Optional.ofNullable(REGISTRY.get().getValue(id));
    }

    @Override
    public Optional<ResourceLocation> getId(AffixType<?> type) {
        return Optional.ofNullable(REGISTRY.get().getKey(type));
    }

    @Override
    public Collection<AffixType<?>> getAll() {
        return REGISTRY.get().getValues().stream().toList();
    }
}
