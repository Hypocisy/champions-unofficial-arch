package top.theillusivec4.champions.common.client.screen.editor.widget;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import top.theillusivec4.champions.common.client.screen.editor.EditorLang;
import top.theillusivec4.champions.common.client.screen.editor.EditorSession;
import top.theillusivec4.champions.common.client.screen.editor.json.JsonPathOps;
import top.theillusivec4.champions.common.client.screen.editor.picker.PickerEntry;
import top.theillusivec4.champions.common.client.screen.editor.picker.RegistryPickerScreen;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Row-based form DSL used by every editor pane. Rows are declared declaratively;
 * the host screen handles layout, scrolling and rendering of labels/headers.
 *
 * <p>All writes go through {@link #changed()} which pretty-prints the live JSON
 * back into the session map and marks the entry dirty. Structural changes
 * (add/remove nodes, switch types) additionally call {@link #rebuild()}.</p>
 */
public final class FormBuilder {

    public record CycleOption(String value, String display) {}

    private static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().create();

    private final Font font;
    private final JsonObject root;
    private final EditorSession session;
    private final String selectedId;
    private final Runnable requestRebuild;
    private final List<Row> rows = new ArrayList<>();

    public FormBuilder(Font font, JsonObject root, EditorSession session,
                       String selectedId, Runnable requestRebuild) {
        this.font = font;
        this.root = root;
        this.session = session;
        this.selectedId = selectedId;
        this.requestRebuild = requestRebuild;
    }

    public JsonObject root() { return root; }
    public Font font() { return font; }
    public EditorSession session() { return session; }
    public List<Row> rows() { return rows; }

    // ── Change plumbing ───────────────────────────────────────────────────────

    /** Commit live JSON into the session map + mark dirty. */
    public void changed() {
        if (selectedId != null) session.commit(selectedId, PRETTY.toJson(root));
    }

    /** Structural change: commit then rebuild the whole form. */
    public void rebuild() {
        changed();
        requestRebuild.run();
    }

    // ── Headers & gaps ────────────────────────────────────────────────────────

    public FormBuilder header(String label) { return header(label, 0); }

    public FormBuilder header(String label, int indent) {
        rows.add(Row.header(label, indent, null));
        return this;
    }

    /** Header with a small trailing button (typically "✕" to remove the block). */
    public FormBuilder headerWithRemove(String label, int indent, Runnable onRemove) {
        Button btn = Button.builder(Component.literal("§c✕"), b -> onRemove.run())
                .bounds(0, 0, 20, Row.FIELD_H).build();
        rows.add(Row.header(label, indent, btn));
        return this;
    }

    public FormBuilder hint(String text, int indent) {
        rows.add(Row.header("§7" + text, indent, null));
        return this;
    }

    public FormBuilder gap() { return gap(Row.GAP_H); }

    public FormBuilder gap(int px) {
        rows.add(Row.gap(px));
        return this;
    }

    // ── Text fields ───────────────────────────────────────────────────────────

    /** Smart text field: numbers/bools written with proper JSON types. */
    public FormBuilder text(String label, String path, String def) {
        return text(label, path, def, 0, true);
    }

    /** Smart text field with indent. */
    public FormBuilder text(String label, String path, String def, int indent) {
        return text(label, path, def, indent, true);
    }

    /** Raw text field: value always written as a string. */
    public FormBuilder textRaw(String label, String path, String def) {
        return text(label, path, def, 0, false);
    }

    /** Raw text field with indent. */
    public FormBuilder textRaw(String label, String path, String def, int indent) {
        return text(label, path, def, indent, false);
    }

    public FormBuilder text(String label, String path, String def, int indent, boolean smart) {
        EditBox box = new EditBox(font, 0, 0, 120, Row.FIELD_H, Component.literal(label));
        box.setMaxLength(1024);
        box.setValue(displayValue(path, def));
        // responder AFTER setValue so the initial display doesn't rewrite the JSON
        box.setResponder(v -> {
            if (smart) JsonPathOps.setSmart(root, path, v);
            else JsonPathOps.setString(root, path, v);
            changed();
        });
        rows.add(Row.field(label, indent, List.of(box)));
        return this;
    }

    /** Raw text field + trailing "…" button opening a single-select picker. */
    public FormBuilder textWithPick(String label, String path, String def,
                                    List<PickerEntry> source) {
        EditBox box = new EditBox(font, 0, 0, 120, Row.FIELD_H, Component.literal(label));
        box.setMaxLength(1024);
        box.setValue(displayValue(path, def));
        box.setResponder(v -> { JsonPathOps.setString(root, path, v); changed(); });
        Button pick = Button.builder(Component.literal("…"), b ->
                        openPicker(label, source, false, pickedSingle(path),
                                ids -> {
                                    if (!ids.isEmpty()) {
                                        JsonPathOps.setString(root, path, ids.iterator().next());
                                        rebuild();
                                    }
                                }))
                .bounds(0, 0, 20, Row.FIELD_H).build();
        rows.add(Row.field(label, 0, List.of(box, pick)));
        return this;
    }

    /** Text field whose value is handled by a custom consumer (config values etc.). */
    public FormBuilder direct(String label, String value, Consumer<String> onEdit) {
        EditBox box = new EditBox(font, 0, 0, 120, Row.FIELD_H, Component.literal(label));
        box.setMaxLength(2048);
        box.setValue(value);
        box.setResponder(onEdit);
        rows.add(Row.field(label, 0, List.of(box)));
        return this;
    }

    // ── Toggle ────────────────────────────────────────────────────────────────

    public FormBuilder toggle(String label, String path, boolean def) {
        return toggle(label, path, def, 0);
    }

    public FormBuilder toggle(String label, String path, boolean def, int indent) {
        boolean[] current = {readBool(path, def)};
        Button btn = Button.builder(
                        Component.literal(labelValue(current[0])),
                        b -> {
                            current[0] = !current[0];
                            JsonPathOps.set(root, path,
                                    new com.google.gson.JsonPrimitive(current[0]));
                            b.setMessage(Component.literal(labelValue(current[0])));
                            changed();
                        })
                .bounds(0, 0, 120, Row.FIELD_H).build();
        btn.setMessage(Component.literal(labelValue(current[0])));
        rows.add(Row.field(label, indent, List.of(btn)));
        return this;
    }

    // ── Cycle ─────────────────────────────────────────────────────────────────

    /** Cycle among string values, writing the raw value to {@code path}. */
    public FormBuilder cycle(String label, String path, String def, List<CycleOption> options) {
        return cycle(label, path, def, options, 0, null);
    }

    /**
     * Cycle among string values with an optional custom select handler. When
     * {@code onSelect} is non-null it fully owns the write (used for structural
     * switches such as filter/condition/effect type changes).
     */
    public FormBuilder cycle(String label, String path, String def,
                             List<CycleOption> options, int indent,
                             Consumer<String> onSelect) {
        int idx = indexOf(options, JsonPathOps.str(root, path, def));
        int[] cursor = {Math.max(0, idx)};
        Button btn = Button.builder(
                        Component.literal("§o" + options.get(cursor[0]).display() + " §8⇄"),
                        b -> {
                            cursor[0] = (cursor[0] + 1) % options.size();
                            String next = options.get(cursor[0]).value();
                            if (onSelect != null) onSelect.accept(next);
                            else { JsonPathOps.setString(root, path, next); changed(); }
                            b.setMessage(Component.literal(
                                    "§o" + options.get(cursor[0]).display() + " §8⇄"));
                        })
                .bounds(0, 0, 120, Row.FIELD_H).build();
        btn.setMessage(Component.literal("§o" + options.get(cursor[0]).display() + " §8⇄"));
        rows.add(Row.field(label, indent, List.of(btn)));
        return this;
    }

    // ── Action rows ───────────────────────────────────────────────────────────

    public FormBuilder action(String buttonText, Runnable onClick) {
        return action(buttonText, 0, onClick);
    }

    public FormBuilder action(String buttonText, int indent, Runnable onClick) {
        String styled = buttonText.startsWith("+") ? "§a" + buttonText : buttonText;
        Button btn = Button.builder(Component.literal(styled), b -> onClick.run())
                .bounds(0, 0, 120, Row.FIELD_H).build();
        rows.add(Row.field(null, indent, List.of(btn)));
        return this;
    }

    /** Field row with label + arbitrary widget list (escape hatch). */
    public FormBuilder custom(String label, int indent, List<AbstractWidget> widgets) {
        rows.add(Row.field(label, indent, widgets));
        return this;
    }

    // ── Picker host ───────────────────────────────────────────────────────────

    /**
     * Opens the searchable registry picker as a modal. On Done the selection set is
     * handed to {@code onCommit}, then the editor re-opens and the form rebuilds.
     */
    public void openPicker(String title, List<PickerEntry> entries, boolean multi,
                           Set<String> preselected, Consumer<Set<String>> onCommit) {
        Minecraft mc = Minecraft.getInstance();
        mc.setScreen(RegistryPickerScreen.create(
                title, entries, multi, new LinkedHashSet<>(preselected),
                onCommit, session::reopen));
    }

    // ── Internals ─────────────────────────────────────────────────────────────

    private String displayValue(String path, String def) {
        return JsonPathOps.str(root, path, def);
    }

    private boolean readBool(String path, boolean def) {
        var e = JsonPathOps.get(root, path);
        if (e != null && e.isJsonPrimitive()) {
            try { return e.getAsBoolean(); } catch (Exception ignored) {}
        }
        return def;
    }

    private static String labelValue(boolean v) {
        return v ? EditorLang.tr("gui.champions.editor.toggle.true")
                 : EditorLang.tr("gui.champions.editor.toggle.false");
    }

    private static int indexOf(List<CycleOption> options, String value) {
        for (int i = 0; i < options.size(); i++) {
            if (options.get(i).value().equals(value)) return i;
        }
        return -1;
    }

    private Set<String> pickedSingle(String path) {
        String v = JsonPathOps.str(root, path, "");
        return v.isEmpty() ? Set.of() : Set.of(v);
    }
}
