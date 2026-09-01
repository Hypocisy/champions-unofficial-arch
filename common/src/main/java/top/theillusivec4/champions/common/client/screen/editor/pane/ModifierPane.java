package top.theillusivec4.champions.common.client.screen.editor.pane;

import com.google.gson.JsonObject;
import top.theillusivec4.champions.common.client.screen.editor.filter.FilterEditor;
import top.theillusivec4.champions.common.client.screen.editor.json.JsonPathOps;
import top.theillusivec4.champions.common.client.screen.editor.picker.PickerSources;
import top.theillusivec4.champions.common.client.screen.editor.widget.FormBuilder;
import top.theillusivec4.champions.common.client.screen.editor.widget.FormBuilder.CycleOption;

import java.util.LinkedHashSet;
import java.util.List;

/**
 * Modifier tab — {@code modifier_setting} files, including the nested
 * {@code conditions} block (entity filter tree, tier bounds, affix values).
 */
public final class ModifierPane implements EditorPane {

    private static final List<CycleOption> OPERATIONS = List.of(
            new CycleOption("add_value",            "add_value"),
            new CycleOption("add_multiplied_base",  "add_multiplied_base"),
            new CycleOption("add_multiplied_total", "add_multiplied_total"));

    @Override
    public String newEntryPrefix() { return "champions:new_modifier"; }

    @Override
    public void buildForm(FormBuilder fb, String selectedId) {
        JsonObject root = fb.root();

        fb.header("Modifier Setting");
        fb.textWithPick("attribute", "attributeType",
                "minecraft:generic.max_health", PickerSources.attributes());
        fb.toggle("enable", "enable", true);

        fb.header("Modifier");
        fb.text("value", "modifier.value", "0.0");
        fb.cycle("operation", "modifier.operation", "add_value", OPERATIONS);

        fb.header("Conditions");
        fb.text("tier min", "conditions.tier.min", "1", 1);
        fb.text("tier max", "conditions.tier.max", "5", 1);

        String valuesPath = "conditions.affixes.values";
        List<String> current = JsonPathOps.stringsIn(root, valuesPath);
        fb.action("Affix values… (" + current.size() + " selected)", 1, () ->
                fb.openPicker("Affix values", PickerSources.affixes(), true,
                        new LinkedHashSet<>(current), ids -> {
                            JsonPathOps.setStringArray(root, valuesPath, List.copyOf(ids));
                            fb.rebuild();
                        }));

        fb.header("Entity Filter", 1);
        FilterEditor.build(fb, "conditions.entity_filter", 1);

        fb.hint("affixes.matches / affixes.count: use JSON view", 1);
    }
}
