package top.theillusivec4.champions.neoforge.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;
import top.theillusivec4.champions.api.ChampionsApi;
import top.theillusivec4.champions.api.affix.AffixType;
import top.theillusivec4.champions.common.datagen.ChampionLanguageProvider;
import top.theillusivec4.champions.common.registry.ModDamageTypes;

/**
 * NeoForge language provider.
 *
 * <p>All locale translations live in the common {@link ChampionLanguageProvider} base class.
 * This subclass only adds entries that depend on NeoForge registry objects
 * (mob effects, entity types, damage types, items) which do not yet exist in the
 * new project. Add them in {@link #addPlatformEntries()} once those registries are wired.</p>
 */
public class NeoForgeLanguageProvider extends ChampionLanguageProvider {

    public NeoForgeLanguageProvider(PackOutput output, String locale) {
        super(output, locale);
    }

    // ── Platform-specific entries ─────────────────────────────────────────────

    /**
     * Add registry-dependent entries here once the matching objects are registered.
     * Example:
     * <pre>{@code
     * add(ModMobEffects.PARALYSIS_EFFECT_TYPE.get(), "Paralysis");
     * add(ModMobEffects.WOUND_EFFECT_TYPE.get(),     "Wound");
     * addDamageType(ModDamageTypes.ENKINDLING_BULLET,
     *     "%1$s was struck by flames",
     *     "%1$s was struck by flames whilst fighting %2$s");
     * addDamageType(ModDamageTypes.REFLECTION_DAMAGE,
     *     "%1$s got a taste of their own medicine", "");
     * }</pre>
     */
    @Override
    protected void addPlatformEntries() {
        if (!"en_us".equals(getLocale())) return;
        // Damage type death messages
        addDamageType(ModDamageTypes.REFLECTION,
                "%1$s got a taste of their own medicine",
                "%1$s got a taste of their own medicine whilst fighting %2$s");
    }

    // ── Convenience overloads kept for backward compat ────────────────────────

    /** Resolve an affix translation key from the live registry object. */
    public void addAffix(AffixType<?> affix, String name) {
        ChampionsApi.get().getAffixTypeId(affix)
                .ifPresent(id -> addAffix(id, name));
    }

    /** Damage type helper that accepts a ResourceKey. */
    public void addDamageType(ResourceKey<DamageType> key,
                              String death, String deathByPlayer) {
        addDamageType(key.location(), death, deathByPlayer);
    }

    /** Legacy 3-arg add used by old subclass code. */
    public void add(String prefix, ResourceLocation id, String value) {
        add(id.toLanguageKey(prefix), value);
    }
}
