package top.theillusivec4.champions.common.client.screen.editor.filter;

import com.google.gson.JsonObject;
import top.theillusivec4.champions.common.client.screen.editor.json.JsonPathOps;
import top.theillusivec4.champions.common.client.screen.editor.picker.PickerSources;
import top.theillusivec4.champions.common.client.screen.editor.widget.FormBuilder;
import top.theillusivec4.champions.common.client.screen.editor.widget.FormBuilder.CycleOption;

import java.util.LinkedHashSet;
import java.util.List;

/**
 * Visual editor for the {@link top.theillusivec4.champions.common.filter.EntityFilter}
 * tree. Recursively renders composite nodes ({@code all_of}/{@code any_of}) and
 * per-type config rows for every leaf filter.
 *
 * <p>The same editor is reused for archetype {@code entity_filter} and modifier
 * {@code conditions.entity_filter} — only the base path differs.</p>
 */
public final class FilterEditor {

    private FilterEditor() {}

    private static final List<CycleOption> TYPES = List.of(
            new CycleOption("any",          "any"),
            new CycleOption("all_of",       "all_of (AND)"),
            new CycleOption("any_of",       "any_of (OR)"),
            new CycleOption("entity_type",  "entity_type"),
            new CycleOption("entity_tag",   "entity_tag"),
            new CycleOption("mod_id",       "mod_id"),
            new CycleOption("mob_category", "mob_category"),
            new CycleOption("attribute",    "attribute"));

    /** Builds the rows for the filter node at {@code basePath} with the given indent. */
    public static void build(FormBuilder fb, String basePath, int indent) {
        JsonObject root = fb.root();
        JsonObject node = JsonPathOps.obj(root, basePath);

        if (node == null) {
            fb.action("+ Add filter", indent, () -> fb.openPicker(
                    "Filter type", PickerSources.filterTypes(), false, java.util.Set.of(),
                    ids -> {
                        if (!ids.isEmpty()) {
                            JsonPathOps.set(root, basePath,
                                    defaultFilter(ids.iterator().next()));
                            fb.rebuild();
                        }
                    }));
            return;
        }

        String type = JsonPathOps.str(root, JsonPathOps.child(basePath, "type"), "any");

        // Type switcher — structural: replaces the whole node with the new default
        fb.cycle("type", JsonPathOps.child(basePath, "type"), type, TYPES, indent,
                newType -> {
                    JsonPathOps.set(root, basePath, defaultFilter(newType));
                    fb.rebuild();
                });

        // Per-type config rows
        switch (type) {
            case "any" -> fb.hint("matches every entity", indent + 1);
            case "all_of", "any_of" -> buildComposite(fb, basePath, indent, type);
            case "entity_type" -> buildEntityType(fb, basePath, indent);
            case "entity_tag" -> buildEntityTag(fb, basePath, indent);
            case "mod_id" -> buildModId(fb, basePath, indent);
            case "mob_category" -> buildMobCategory(fb, basePath, indent);
            case "attribute" -> buildAttribute(fb, basePath, indent);
            default -> fb.hint("§cunknown filter type: " + type, indent + 1);
        }
    }

    // ── Composite: all_of / any_of ─────────────────────────────────────────────

    private static void buildComposite(FormBuilder fb, String basePath, int indent, String type) {
        JsonObject root = fb.root();
        fb.hint(type.equals("all_of") ? "child filters are ANDed" : "child filters are ORed",
                indent + 1);

        String childrenPath = JsonPathOps.child(basePath, "filters");
        int n = JsonPathOps.size(root, childrenPath);
        for (int i = 0; i < n; i++) {
            String childPath = JsonPathOps.index(childrenPath, i);
            fb.headerWithRemove("Filter " + (i + 1), indent + 1,
                    () -> { JsonPathOps.remove(root, childPath); fb.rebuild(); });
            build(fb, childPath, indent + 2);
        }
        fb.action("+ Add child filter", indent + 1, () -> {
            JsonPathOps.append(root, childrenPath, defaultFilter("any"));
            fb.rebuild();
        });
    }

    // ── Leaves ────────────────────────────────────────────────────────────────

    private static void buildEntityType(FormBuilder fb, String basePath, int indent) {
        JsonObject root = fb.root();
        String typesPath = JsonPathOps.child(basePath, "types");
        List<String> current = JsonPathOps.stringsIn(root, typesPath);

        fb.toggle("whitelist", JsonPathOps.child(basePath, "whitelist"), true, indent + 1);
        fb.action("Entity types… (" + current.size() + " selected)", indent + 1, () ->
                fb.openPicker("Entity types", PickerSources.entityTypes(), true,
                        new LinkedHashSet<>(current), ids -> {
                            JsonPathOps.setStringArray(root, typesPath, List.copyOf(ids));
                            fb.rebuild();
                        }));
        if (!current.isEmpty()) {
            fb.hint("§8" + String.join(", ", current), indent + 1);
        }
    }

    private static void buildEntityTag(FormBuilder fb, String basePath, int indent) {
        fb.textRaw("tag", JsonPathOps.child(basePath, "tag"), "minecraft:undead", indent + 1);
        fb.toggle("whitelist", JsonPathOps.child(basePath, "whitelist"), true, indent + 1);
    }

    private static void buildModId(FormBuilder fb, String basePath, int indent) {
        JsonObject root = fb.root();
        String path = JsonPathOps.child(basePath, "mod_ids");
        List<String> current = JsonPathOps.stringsIn(root, path);

        fb.toggle("whitelist", JsonPathOps.child(basePath, "whitelist"), true, indent + 1);
        fb.action("Mod namespaces… (" + current.size() + " selected)", indent + 1, () ->
                fb.openPicker("Mod namespaces", PickerSources.modNamespaces(), true,
                        new LinkedHashSet<>(current), ids -> {
                            JsonPathOps.setStringArray(root, path, List.copyOf(ids));
                            fb.rebuild();
                        }));
        if (!current.isEmpty()) {
            fb.hint("§8" + String.join(", ", current), indent + 1);
        }
    }

    private static void buildMobCategory(FormBuilder fb, String basePath, int indent) {
        JsonObject root = fb.root();
        String path = JsonPathOps.child(basePath, "categories");
        List<String> current = JsonPathOps.stringsIn(root, path);

        fb.action("Categories… (" + current.size() + " selected)", indent + 1, () ->
                fb.openPicker("Mob categories", PickerSources.mobCategories(), true,
                        new LinkedHashSet<>(current), ids -> {
                            JsonPathOps.setStringArray(root, path, List.copyOf(ids));
                            fb.rebuild();
                        }));
        if (!current.isEmpty()) {
            fb.hint("§8" + String.join(", ", current), indent + 1);
        }
    }

    private static void buildAttribute(FormBuilder fb, String basePath, int indent) {
        fb.textWithPick("attribute", JsonPathOps.child(basePath, "attribute"),
                "minecraft:generic.max_health", PickerSources.attributes());
        fb.text("min", JsonPathOps.child(basePath, "min"), "0", indent + 1);
        fb.text("max", JsonPathOps.child(basePath, "max"), "1000000", indent + 1);
    }

    // ── Defaults when adding / switching type ─────────────────────────────────

    public static JsonObject defaultFilter(String type) {
        JsonObject o = new JsonObject();
        o.addProperty("type", type);
        switch (type) {
            case "all_of", "any_of" -> {
                com.google.gson.JsonArray arr = new com.google.gson.JsonArray();
                arr.add(defaultFilter("any"));
                o.add("filters", arr);
            }
            case "entity_type" -> {
                o.addProperty("whitelist", true);
                o.add("types", stringArray("minecraft:zombie"));
            }
            case "entity_tag" -> {
                o.addProperty("whitelist", true);
                o.addProperty("tag", "minecraft:undead");
            }
            case "mod_id" -> {
                o.addProperty("whitelist", true);
                o.add("mod_ids", stringArray("minecraft"));
            }
            case "mob_category" -> o.add("categories", stringArray("monster"));
            case "attribute" -> {
                o.addProperty("attribute", "minecraft:generic.max_health");
                o.addProperty("min", 0);
                o.addProperty("max", 1000000);
            }
            default -> { /* any: just the type */ }
        }
        return o;
    }

    private static com.google.gson.JsonArray stringArray(String... values) {
        com.google.gson.JsonArray arr = new com.google.gson.JsonArray();
        for (String v : values) arr.add(v);
        return arr;
    }
}
