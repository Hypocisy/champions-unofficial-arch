package top.theillusivec4.champions.neoforge.kubejs;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import top.theillusivec4.champions.neoforge.registry.NeoForgeAffixTypeRegistry;

/**
 * KubeJS plugin entry point (KubeJS 6 / Minecraft 1.20.1).
 *
 * <p>Registers the {@link ScriptableAffixType.Builder} so KubeJS's
 * {@code StartupEvents.registry('champions:affix_type', ...)} can create custom
 * affix types from scripts, and wires the Champions JS events
 * ({@link ChampionsJsEvents}) that scripts can listen to.</p>
 *
 * <h3>KubeJS usage (JS):</h3>
 * <pre>{@code
 * StartupEvents.registry('champions:affix_type', event => {
 *     event.create('myscript:shock_wave')
 *         .onAttack((champion, strength, evt) => { ... })
 *         .onHurt((champion, strength, evt) => { ... });
 * });
 * }</pre>
 *
 * <h3>Declared in {@code kubejs.plugins.txt}:</h3>
 * <pre>
 * top.theillusivec4.champions.neoforge.kubejs.ChampionsKubeJSPlugin champions
 * </pre>
 */
public final class ChampionsKubeJSPlugin extends KubeJSPlugin {

    /** Exposes the champions:affix_type registry to KubeJS registry events. */
    public static final RegistryInfo<ScriptableAffixType> AFFIX_TYPE =
            RegistryInfo.of(NeoForgeAffixTypeRegistry.REGISTRY_KEY, ScriptableAffixType.class);

    @Override
    public void registerEvents() {
        // "affix" is the (default) builder type id used by event.create(id) in scripts
        AFFIX_TYPE.addType("affix", ScriptableAffixType.Builder.class, ScriptableAffixType.Builder::new, true);
        ChampionsJsEvents.register();
    }
}

