package top.theillusivec4.champions.neoforge.kubejs;

import dev.latvian.mods.kubejs.event.EventGroupRegistry;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.registry.BuilderTypeRegistry;

/**
 * KubeJS plugin entry point.
 *
 * <p>Registers the {@link ScriptableAffixType.Builder} so KubeJS's {@code StartupEvents.registry}
 * can create custom affix types from script files. Also registers Champions-specific
 * JS events ({@link ChampionsJsEvents}) that scripts can listen to.</p>
 *
 * <h3>Must be declared in {@code kubejs.plugins} file:</h3>
 * <pre>
 * top.theillusivec4.champions.integration.kubejs.ChampionsKubeJSPlugin
 * </pre>
 */
public final class ChampionsKubeJSPlugin implements KubeJSPlugin {

    @Override
    public void registerBuilderTypes(BuilderTypeRegistry registry) {
        // Register our builder as the default handler for the affix_type registry.
        // Scripts using StartupEvents.registry('champions:affix_type', ...) will receive
        // a ScriptableAffixType.Builder instance.
//        registry.of(
//                NeoForgeAffixTypeRegistry.REGISTRY_KEY,
//                reg -> reg.addDefault(ScriptableAffixType.Builder.class, ScriptableAffixType.Builder::new)
//        );
    }

    @Override
    public void registerEvents(EventGroupRegistry registry) {
        registry.register(ChampionsJsEvents.GROUP);
    }
}
