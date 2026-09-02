package top.theillusivec4.champions.common.client.screen.editor.pane;

import com.google.gson.JsonObject;
import top.theillusivec4.champions.common.client.screen.editor.EditorLang;
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

        fb.header(tr("header.modifier_setting"));
        fb.textWithPick(tr("label.attribute"), "attributeType",
                "minecraft:generic.max_health", PickerSources.attributes());
        fb.toggle(tr("label.enable"), "enable", true);

        fb.header(tr("header.modifier"));
        fb.text(tr("label.value"), "modifier.value", "0.0");
        fb.cycle(tr("label.operation"), "modifier.operation", "add_value", OPERATIONS);

        fb.header(tr("header.conditions"));
        fb.text(tr("label.tier_min"), "conditions.tier.min", "1", 1);
        fb.text(tr("label.tier_max"), "conditions.tier.max", "5", 1);

        String valuesPath = "conditions.affixes.values";
        List<String> current = JsonPathOps.stringsIn(root, valuesPath);
        fb.action(tr("action.pick_affix_values", current.size()), 1, () ->
                fb.openPicker(tr("gui.champions.picker.title.affix_values"), PickerSources.affixes(), true,
                        new LinkedHashSet<>(current), ids -> {
                            JsonPathOps.setStringArray(root, valuesPath, List.copyOf(ids));
                            fb.rebuild();
                        }));

        fb.header(tr("header.entity_filter"), 1);
        FilterEditor.build(fb, "conditions.entity_filter", 1);

        fb.hint(tr("hint.json_view_only"), 1);
    }

    private static String tr(String key, Object... args) {
        return EditorLang.tr(key, args);
    }
}
