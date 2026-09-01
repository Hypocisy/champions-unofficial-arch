package top.theillusivec4.champions.common.client.screen.editor.picker;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import top.theillusivec4.champions.api.ChampionsApi;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * Client-side sources feeding {@link RegistryPickerScreen}. All are computed from
 * vanilla registries / the champions affix registry and are safe to call on the
 * client thread.
 */
public final class PickerSources {

    private PickerSources() {}

    // ── Entities ──────────────────────────────────────────────────────────────

    public static List<PickerEntry> entityTypes() {
        List<PickerEntry> out = new ArrayList<>();
        BuiltInRegistries.ENTITY_TYPE.keySet().forEach(id -> {
            var type = BuiltInRegistries.ENTITY_TYPE.get(id);
            out.add(new PickerEntry(id.toString(),
                    Component.translatable(type.getDescriptionId())));
        });
        out.sort((a, b) -> a.id().compareTo(b.id()));
        return out;
    }

    // ── Attributes ────────────────────────────────────────────────────────────

    public static List<PickerEntry> attributes() {
        List<PickerEntry> out = new ArrayList<>();
        BuiltInRegistries.ATTRIBUTE.keySet().forEach(id -> {
            var attr = BuiltInRegistries.ATTRIBUTE.get(id);
            out.add(new PickerEntry(id.toString(),
                    Component.translatable(attr.getDescriptionId())));
        });
        out.sort((a, b) -> a.id().compareTo(b.id()));
        return out;
    }

    // ── Mob effects ───────────────────────────────────────────────────────────

    public static List<PickerEntry> mobEffects() {
        List<PickerEntry> out = new ArrayList<>();
        BuiltInRegistries.MOB_EFFECT.keySet().forEach(id -> {
            var effect = BuiltInRegistries.MOB_EFFECT.get(id);
            out.add(new PickerEntry(id.toString(),
                    Component.translatable(effect.getDescriptionId())));
        });
        out.sort((a, b) -> a.id().compareTo(b.id()));
        return out;
    }

    // ── Mob categories ────────────────────────────────────────────────────────

    public static List<PickerEntry> mobCategories() {
        List<PickerEntry> out = new ArrayList<>();
        for (var cat : net.minecraft.world.entity.MobCategory.values()) {
            out.add(PickerEntry.of(cat.getName()));
        }
        return out;
    }

    // ── Mod namespaces (derived from the entity type registry) ────────────────

    public static List<PickerEntry> modNamespaces() {
        TreeSet<String> ns = new TreeSet<>();
        BuiltInRegistries.ENTITY_TYPE.keySet().forEach(id -> ns.add(id.getNamespace()));
        List<PickerEntry> out = new ArrayList<>();
        ns.forEach(n -> out.add(PickerEntry.of(n)));
        return out;
    }

    // ── Affixes ───────────────────────────────────────────────────────────────

    public static List<PickerEntry> affixes() {
        List<PickerEntry> out = new ArrayList<>();
        try {
            for (var type : ChampionsApi.get().getAffixTypes()) {
                ChampionsApi.get().getAffixTypeId(type)
                        .ifPresent(rl -> out.add(PickerEntry.of(rl.toString())));
            }
        } catch (IllegalStateException ignored) {
            // API not injected yet — return empty, editor still works
        }
        out.sort((a, b) -> a.id().compareTo(b.id()));
        return out;
    }

    // ── Entity filter types ───────────────────────────────────────────────────

    public static List<PickerEntry> filterTypes() {
        List<PickerEntry> out = new ArrayList<>();
        out.add(PickerEntry.of("any",         "any — matches everything"));
        out.add(PickerEntry.of("all_of",      "all_of — AND (nested list)"));
        out.add(PickerEntry.of("any_of",      "any_of — OR (nested list)"));
        out.add(PickerEntry.of("entity_type", "entity_type — by entity id"));
        out.add(PickerEntry.of("entity_tag",  "entity_tag — by tag id"));
        out.add(PickerEntry.of("mod_id",      "mod_id — by namespace"));
        out.add(PickerEntry.of("mob_category","mob_category — by category"));
        out.add(PickerEntry.of("attribute",   "attribute — by attribute value"));
        return out;
    }
}
