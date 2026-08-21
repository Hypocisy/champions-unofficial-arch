package top.theillusivec4.champions.common.registry;

import net.minecraft.resources.ResourceLocation;
import top.theillusivec4.champions.api.affix.AffixType;

import java.util.Collection;
import java.util.Optional;

/**
 * Query interface for the affix type registry.
 *
 * <p>The backing registry is a NeoForge/Architectury {@code DeferredRegister} on the
 * implementation side. This interface only exposes read operations so the rest of
 * {@code common/} stays platform-agnostic.</p>
 */
public interface AffixTypeRegistry {

    Optional<AffixType<?>> get(ResourceLocation id);

    Optional<ResourceLocation> getId(AffixType<?> type);

    /**
     * All registered affix types. Unmodifiable.
     */
    Collection<AffixType<?>> getAll();

    /**
     * True if {@code type} is present in the registry.
     */
    default boolean contains(AffixType<?> type) {
        return getId(type).isPresent();
    }
}
