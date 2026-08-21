package top.theillusivec4.champions.common.client.screen;

import com.google.gson.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import top.theillusivec4.champions.common.network.EditorPayload;

import java.util.*;
import java.util.function.Consumer;

public final class ChampionEditorScreen extends Screen {

    // ── Save callback ─────────────────────────────────────────────────────────
    private static Consumer<EditorPayload> saveCallback;
    public static void setSaveCallback(Consumer<EditorPayload> cb) { saveCallback = cb; }

    // ── Tabs ──────────────────────────────────────────────────────────────────
    private enum Tab { ARCHETYPES, TIERS, MODIFIERS, CONFIG }
    private Tab activeTab = Tab.ARCHETYPES;

    // ── Data (string maps, canonical source) ──────────────────────────────────
    private final Map<String, String> archetypeJsons;
    private final Map<String, String> tierJsons;
    private final Map<String, String> configValues;
    private final Map<String, String> modifierJsons;

    // ── Built-in / dirty tracking ─────────────────────────────────────────────
    /** ids that come from a jar pack (blue in list, delete = disabled-override). */
    private final Set<String> builtinIds;
    /** ids modified this session (shown with * prefix). */
    private final Set<String> dirtyIds = new LinkedHashSet<>();

    // ── Current selection ─────────────────────────────────────────────────────
    private String selectedId   = null;
    /** Live mutable JSON for the currently selected entry (form mode). */
    private JsonObject liveJson = null;

    // ── Raw JSON view ─────────────────────────────────────────────────────────
    /** true = raw JSON editor visible, false = form view. */
    private boolean rawMode = false;
    /** Lazy-created multiline text editor for raw JSON view. */
    private MultiLineEditBox rawEditor = null;

    // ── Layout ────────────────────────────────────────────────────────────────
    private static final int TAB_H   = 20;
    private static final int LIST_W  = 140;
    private static final int ENTRY_H = 14;
    private static final int PAD     = 4;
    private static final int BOT_H   = 28;
    private static final int ROW_H   = 22;
    private static final int FIELD_H = 18;
    private static final int LABEL_W = 106;
    private static final int SMBN_W  = 20;  // small ± button width

    // ── Scroll ────────────────────────────────────────────────────────────────
    private int listScroll   = 0;
    private int formScrollY  = 0;
    private int formContentH = 0;

    // ── Form rows ─────────────────────────────────────────────────────────────
    /** Immutable descriptor for one visual row in the right panel. */
    private static final class FormEntry {
        final String  label;
        final EditBox box;      // null → header row
        final String  jsonPath; // dot/array path, null for headers and config
        Button plusBtn;   // optional inline action buttons
        Button minusBtn;

        FormEntry(String label, EditBox box, String jsonPath) {
            this.label    = label;
            this.box      = box;
            this.jsonPath = jsonPath;
        }
    }
    private final List<FormEntry> formEntries = new ArrayList<>();

    // ── Top/bottom widgets ────────────────────────────────────────────────────
    private Button tabArchetypes, tabTiers, tabModifiers, tabConfig;
    private Button btnNew, btnDelete, btnSave, btnClose;
    private Button btnForm, btnJson; // view toggle
    private String saveError = null;

    // ── Constructor ───────────────────────────────────────────────────────────
    private ChampionEditorScreen(EditorPayload payload) {
        super(Component.literal("Champions Editor"));
        archetypeJsons = new LinkedHashMap<>(payload.archetypeJsons());
        tierJsons      = new LinkedHashMap<>(payload.tierJsons());
        configValues   = new LinkedHashMap<>(payload.configValues());
        modifierJsons  = new LinkedHashMap<>(payload.modifierJsons());
        builtinIds     = new LinkedHashSet<>(payload.builtinIds());
    }

    public static void open(EditorPayload payload) {
        Minecraft.getInstance().setScreen(new ChampionEditorScreen(payload));
    }

    // ── Init ──────────────────────────────────────────────────────────────────
    @Override
    protected void init() {
        int w = width, h = height;
        int tw = 82;
        tabArchetypes = addRenderableWidget(Button.builder(
                Component.literal("Archetypes"), b -> switchTab(Tab.ARCHETYPES))
                .bounds(PAD, PAD, tw, TAB_H).build());
        tabTiers = addRenderableWidget(Button.builder(
                Component.literal("Tiers"), b -> switchTab(Tab.TIERS))
                .bounds(PAD + tw + 2, PAD, tw, TAB_H).build());
        tabModifiers = addRenderableWidget(Button.builder(
                Component.literal("Modifiers"), b -> switchTab(Tab.MODIFIERS))
                .bounds(PAD + (tw + 2) * 2, PAD, tw, TAB_H).build());
        tabConfig = addRenderableWidget(Button.builder(
                Component.literal("Config"), b -> switchTab(Tab.CONFIG))
                .bounds(PAD + (tw + 2) * 3, PAD, tw, TAB_H).build());

        // View-mode toggle (Form / JSON) — top-right of editor panel
        int toggleX = w - PAD - 90;
        btnForm = addRenderableWidget(Button.builder(
                Component.literal("Form"), b -> setRawMode(false))
                .bounds(toggleX, PAD, 44, TAB_H).build());
        btnJson = addRenderableWidget(Button.builder(
                Component.literal("JSON"), b -> setRawMode(true))
                .bounds(toggleX + 46, PAD, 44, TAB_H).build());

        int botY = h - BOT_H + 4;
        btnNew = addRenderableWidget(Button.builder(
                Component.literal("New"), b -> onNew())
                .bounds(PAD, botY, 50, 20).build());
        btnDelete = addRenderableWidget(Button.builder(
                Component.literal("Delete"), b -> onDelete())
                .bounds(PAD + 54, botY, 60, 20).build());
        btnSave = addRenderableWidget(Button.builder(
                Component.literal("Save & Reload"), b -> onSave())
                .bounds(w - 218, botY, 110, 20).build());
        btnClose = addRenderableWidget(Button.builder(
                Component.literal("Close"), b -> onClose())
                .bounds(w - 104, botY, 100, 20).build());

        refreshTabLabels();
        listScroll = 0;
        selectFirst();
    }

    // ── Tab switching ─────────────────────────────────────────────────────────
    private void switchTab(Tab tab) {
        if (!commitRawIfNeeded()) return; // block switch if JSON is invalid
        activeTab = tab;
        refreshTabLabels();
        listScroll = 0;
        formScrollY = 0;
        selectedId = null;
        liveJson   = null;
        clearFormWidgets();
        formEntries.clear();
        removeRawEditor();
        saveError = null;
        selectFirst();
    }

    private void refreshTabLabels() {
        tabArchetypes.active = activeTab != Tab.ARCHETYPES;
        tabTiers     .active = activeTab != Tab.TIERS;
        tabModifiers .active = activeTab != Tab.MODIFIERS;
        tabConfig    .active = activeTab != Tab.CONFIG;
        boolean hasToggle = activeTab != Tab.CONFIG;
        btnForm.visible = hasToggle;
        btnJson.visible = hasToggle;
        if (btnForm != null) {
            btnForm.active = rawMode;   // Form active when not in rawMode
            btnJson.active = !rawMode;  // JSON active when not already rawMode
        }
    }

    // ── Raw mode toggle ───────────────────────────────────────────────────────
    private void setRawMode(boolean raw) {
        if (rawMode == raw) return;
        if (raw) {
            // FORM → JSON: flush form fields → liveJson → pretty text
            if (selectedId != null) commitFormToLiveJson();
            rawMode = true;
            ensureRawEditor();
            if (liveJson != null) {
                rawEditor.setValue(prettyJson(liveJson));
            } else {
                rawEditor.setValue("");
            }
            // hide form widgets, show raw editor
            setFormWidgetsVisible(false);
            rawEditor.visible = true;
        } else {
            // JSON → FORM: parse raw text → liveJson → rebuild form
            if (!commitRawIfNeeded()) return; // stay in JSON mode on parse error
            rawMode = false;
            if (rawEditor != null) rawEditor.visible = false;
            rebuildForm();
            setFormWidgetsVisible(true);
        }
        refreshTabLabels();
    }

    private boolean commitRawIfNeeded() {
        if (!rawMode || rawEditor == null || selectedId == null) return true;
        String text = rawEditor.getValue().trim();
        if (text.isEmpty()) return true;
        try {
            JsonElement parsed = JsonParser.parseString(text);
            if (!parsed.isJsonObject()) {
                saveError = "JSON must be an object";
                return false;
            }
            liveJson = parsed.getAsJsonObject();
            currentMap().put(selectedId, prettyJson(liveJson));
            dirtyIds.add(selectedId);
            saveError = null;
            return true;
        } catch (JsonSyntaxException e) {
            saveError = "Invalid JSON: " + e.getMessage();
            return false;
        }
    }

    private void ensureRawEditor() {
        if (rawEditor != null) return;
        int ex = LIST_W + PAD * 2;
        int ey = TAB_H + PAD * 2;
        int ew = width  - ex - PAD;
        int eh = height - ey - BOT_H - PAD;
        rawEditor = new MultiLineEditBox(font, ex, ey, ew, eh,
                Component.empty(), Component.literal("{}"));
        addRenderableWidget(rawEditor);
    }

    private void removeRawEditor() {
        if (rawEditor != null) {
            removeWidget(rawEditor);
            rawEditor = null;
        }
        rawMode = false;
    }

    // ── Selection ─────────────────────────────────────────────────────────────
    private void selectFirst() {
        Map<String, String> map = currentMap();
        if (map.isEmpty()) { select(null); return; }
        select(map.keySet().iterator().next());
    }

    private void select(String id) {
        selectedId = id;
        liveJson   = null;
        formScrollY = 0;
        clearFormWidgets();
        formEntries.clear();
        saveError = null;

        if (id != null) {
            String json = currentMap().getOrDefault(id, "{}");
            if (!isEntryDisabled(json)) {
                try { liveJson = JsonParser.parseString(json).getAsJsonObject(); }
                catch (Exception e) { liveJson = new JsonObject(); }
            }
        }

        if (rawMode) {
            ensureRawEditor();
            rawEditor.setValue(liveJson != null ? prettyJson(liveJson) : "");
            rawEditor.visible = true;
        } else {
            if (rawEditor != null) rawEditor.visible = false;
            rebuildForm();
        }
    }

    private Map<String, String> currentMap() {
        return switch (activeTab) {
            case ARCHETYPES -> archetypeJsons;
            case TIERS      -> tierJsons;
            case MODIFIERS  -> modifierJsons;
            case CONFIG     -> configValues;
        };
    }

    // ── Form widget lifecycle ─────────────────────────────────────────────────
    private void clearFormWidgets() {
        for (FormEntry fe : formEntries) {
            if (fe.box     != null) removeWidget(fe.box);
            if (fe.plusBtn != null) removeWidget(fe.plusBtn);
            if (fe.minusBtn!= null) removeWidget(fe.minusBtn);
        }
    }

    private void setFormWidgetsVisible(boolean visible) {
        for (FormEntry fe : formEntries) {
            if (fe.box     != null) fe.box.visible     = visible;
            if (fe.plusBtn != null) fe.plusBtn.visible = visible;
            if (fe.minusBtn!= null) fe.minusBtn.visible= visible;
        }
    }

    private void rebuildForm() {
        clearFormWidgets();
        formEntries.clear();
        if (selectedId == null) { formContentH = 0; return; }
        switch (activeTab) {
            case ARCHETYPES -> buildArchetypeForm();
            case TIERS      -> buildTierForm();
            case MODIFIERS  -> buildModifierForm();
            case CONFIG     -> buildConfigForm();
        }
        layoutFormEntries();
    }

    // ── New / Delete / Save / Close ───────────────────────────────────────────
    private void onNew() {
        if (!commitRawIfNeeded()) return;
        String prefix = switch (activeTab) {
            case ARCHETYPES -> "champions:new_archetype";
            case TIERS      -> "champions:new_tier";
            case MODIFIERS  -> "champions:new_modifier";
            case CONFIG     -> null;
        };
        if (prefix == null) return;
        // Find a unique id
        String base = prefix;
        int n = 1;
        while (currentMap().containsKey(base)) base = prefix + "_" + (n++);
        currentMap().put(base, "{}");
        dirtyIds.add(base);
        select(base);
    }

    private void onDelete() {
        if (selectedId == null) return;
        if (!commitRawIfNeeded()) return;
        if (builtinIds.contains(selectedId)) {
            // Write a disabled override instead of removing
            currentMap().put(selectedId, "{\"disabled\":true}");
            dirtyIds.add(selectedId);
            select(selectedId);
        } else {
            currentMap().remove(selectedId);
            dirtyIds.remove(selectedId);
            selectedId = null;
            liveJson   = null;
            clearFormWidgets();
            formEntries.clear();
            if (rawEditor != null) { rawEditor.setValue(""); rawEditor.visible = false; }
            selectFirst();
        }
    }

    private void onSave() {
        if (!commitRawIfNeeded()) return;
        if (saveCallback != null) {
            saveCallback.accept(new EditorPayload(
                    Map.copyOf(tierJsons),
                    Map.copyOf(archetypeJsons),
                    Map.copyOf(configValues),
                    Map.copyOf(modifierJsons),
                    Set.of())); // builtinIds S2C only, not sent back
        }
        dirtyIds.clear();
    }

    public void onClose() {
        if (commitRawIfNeeded()) onPress(); // commit then close
        else onPress(); // close anyway even if JSON invalid
    }

    private void onPress() {
        if (minecraft != null) minecraft.setScreen(null);
    }

    // ── Form builders ─────────────────────────────────────────────────────────

    private void buildTierForm() {
        if (liveJson == null) return;
        addHeader("Tier");
        addField("level", "level", liveJson.has("level") ? liveJson.get("level").getAsString() : "1");
        addHeader("Display");
        JsonObject display = liveJson.has("display") ? liveJson.getAsJsonObject("display") : new JsonObject();
        addField("color", "display.color", display.has("color") ? display.get("color").getAsString() : "#FFFFFF");
        addField("icon",  "display.icon",  display.has("icon")  ? display.get("icon").getAsString()  : "champions:textures/gui/tier1.png");
    }

    private void buildArchetypeForm() {
        if (liveJson == null) return;
        addHeader("Archetype");
        addField("id",     "id",     liveJson.has("id")     ? liveJson.get("id").getAsString()     : selectedId);
        addField("weight", "weight", liveJson.has("weight") ? liveJson.get("weight").getAsString() : "10");
        // tier_range
        addHeader("Tier Range");
        JsonObject tierRange = liveJson.has("tier_range") ? liveJson.getAsJsonObject("tier_range") : new JsonObject();
        addField("min", "tier_range.min", tierRange.has("min") ? tierRange.get("min").getAsString() : "1");
        addField("max", "tier_range.max", tierRange.has("max") ? tierRange.get("max").getAsString() : "5");
        // entity_filter label
        addHeader("(use JSON view for entity_filter, affix_pools, phases)");
    }

    private void buildModifierForm() {
        if (liveJson == null) return;
        addHeader("Modifier Setting");
        addField("attribute",  "attributeType", liveJson.has("attributeType") ? liveJson.get("attributeType").getAsString() : "minecraft:generic.max_health");
        addField("enable",     "enable",         liveJson.has("enable")        ? liveJson.get("enable").getAsString()        : "true");
        // setting array [value, operation]
        String value = "0.0", operation = "ADD_VALUE";
        if (liveJson.has("setting") && liveJson.get("setting").isJsonArray()) {
            var arr = liveJson.getAsJsonArray("setting");
            if (arr.size() > 0) value     = arr.get(0).getAsString();
            if (arr.size() > 1) operation = arr.get(1).getAsString();
        }
        addHeader("Modifier");
        addField("value",     "setting.0", value);
        addField("operation", "setting.1", operation);
        addHeader("(use JSON view for modifierCondition)");
    }

    private void buildConfigForm() {
        for (Map.Entry<String, String> e : configValues.entrySet()) {
            addConfigField(e.getKey(), e.getValue());
        }
    }

    // ── Form entry helpers ────────────────────────────────────────────────────

    private void addHeader(String label) {
        formEntries.add(new FormEntry(label, null, null));
    }

    private void addField(String label, String jsonPath, String value) {
        EditBox box = new EditBox(font, 0, 0, 120, FIELD_H, Component.literal(label));
        box.setMaxLength(512);
        box.setValue(value);
        box.setResponder(v -> onFieldChanged(jsonPath, v));
        addRenderableWidget(box);
        formEntries.add(new FormEntry(label, box, jsonPath));
    }

    private void addConfigField(String key, String value) {
        EditBox box = new EditBox(font, 0, 0, 120, FIELD_H, Component.literal(key));
        box.setMaxLength(512);
        box.setValue(value);
        box.setResponder(v -> { configValues.put(key, v); dirtyIds.add(key); });
        addRenderableWidget(box);
        formEntries.add(new FormEntry(key, box, null));
    }

    private void onFieldChanged(String jsonPath, String value) {
        if (selectedId == null || liveJson == null || jsonPath == null) return;
        setJsonPath(liveJson, jsonPath, value);
        currentMap().put(selectedId, prettyJson(liveJson));
        dirtyIds.add(selectedId);
    }

    /** Write a dot-path like "display.color" or "setting.0" into a JsonObject. */
    private static void setJsonPath(JsonObject root, String path, String value) {
        String[] parts = path.split("\\.");
        JsonObject obj = root;
        for (int i = 0; i < parts.length - 1; i++) {
            String part = parts[i];
            // Handle array index
            try {
                int idx = Integer.parseInt(parts[i + 1]);
                if (!obj.has(part) || !obj.get(part).isJsonArray()) {
                    obj.add(part, new JsonArray());
                }
                JsonArray arr = obj.getAsJsonArray(part);
                while (arr.size() <= idx) arr.add(JsonNull.INSTANCE);
                arr.set(idx, new JsonPrimitive(value));
                return;
            } catch (NumberFormatException ignored) {}

            if (!obj.has(part) || !obj.get(part).isJsonObject()) {
                obj.add(part, new JsonObject());
            }
            obj = obj.getAsJsonObject(part);
        }
        String last = parts[parts.length - 1];
        try {
            // Try number first
            obj.addProperty(last, Double.parseDouble(value));
        } catch (NumberFormatException e) {
            if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
                obj.addProperty(last, Boolean.parseBoolean(value));
            } else {
                obj.addProperty(last, value);
            }
        }
    }

    /** Layout form entries into the right panel starting at formScrollY offset. */
    private void layoutFormEntries() {
        int ex = LIST_W + PAD * 2;
        int ey = TAB_H + PAD * 2;
        int ew = width - ex - PAD;
        int y  = ey - formScrollY;
        for (FormEntry fe : formEntries) {
            if (fe.box == null) {
                // header row — no widget, just text
                y += ROW_H - 4;
            } else {
                int bx = ex + LABEL_W + PAD;
                int bw = ew - LABEL_W - PAD;
                fe.box.setX(bx);
                fe.box.setY(y);
                fe.box.setWidth(bw);
                fe.box.visible = true;
                y += ROW_H;
            }
        }
        formContentH = y - (ey - formScrollY);
    }

    /** Commit form EditBox values back into liveJson. */
    private void commitFormToLiveJson() {
        if (liveJson == null) return;
        for (FormEntry fe : formEntries) {
            if (fe.box != null && fe.jsonPath != null) {
                setJsonPath(liveJson, fe.jsonPath, fe.box.getValue());
            }
        }
        if (selectedId != null) {
            currentMap().put(selectedId, prettyJson(liveJson));
            dirtyIds.add(selectedId);
        }
    }

    // ── Rendering ─────────────────────────────────────────────────────────────

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        super.render(g, mx, my, pt);

        int w = width, h = height;
        // Panel backgrounds
        // Left list panel
        g.fill(PAD - 1, TAB_H + PAD - 1, PAD + LIST_W + 1, h - BOT_H + 1, 0xFF333333);
        // Right editor panel
        int ex = LIST_W + PAD * 2;
        int ey = TAB_H + PAD * 2;
        g.fill(ex - 1, ey - 1, w - PAD + 1, h - BOT_H + 1, 0xFF222222);

        // List entries
        renderEntryList(g, mx, my);

        // Form (rendered by widget system) — render headers manually
        if (!rawMode) {
            renderFormHeaders(g);
        }

        // Save error message
        if (saveError != null) {
            g.drawString(font, "§c" + saveError, ex + 2, h - BOT_H - 12, 0xFFFF5555, false);
        }
    }

    private void renderEntryList(GuiGraphics g, int mx, int my) {
        int lx = PAD;
        int ly = TAB_H + PAD;
        int lh = height - ly - BOT_H;
        int visCount = lh / ENTRY_H;

        List<String> keys = new ArrayList<>(currentMap().keySet());
        int startIdx = listScroll;
        int endIdx   = Math.min(startIdx + visCount, keys.size());

        for (int i = startIdx; i < endIdx; i++) {
            String key = keys.get(i);
            int iy = ly + (i - startIdx) * ENTRY_H;
            boolean sel = key.equals(selectedId);

            // Background highlight for selected
            if (sel) g.fill(lx, iy, lx + LIST_W, iy + ENTRY_H, 0xFF555577);

            // Determine color
            String json      = currentMap().getOrDefault(key, "{}");
            boolean disabled = isEntryDisabled(json);
            boolean builtin  = builtinIds.contains(key);
            boolean dirty    = dirtyIds.contains(key);
            int color;
            if      (disabled) color = sel ? 0xFFFF8888 : 0xFF885555;
            else if (builtin)  color = sel ? 0xFFCCDDFF : 0xFF7799BB;
            else               color = sel ? 0xFFFFFFCC : 0xFFAAAAAA;

            String lbl = (dirty ? "* " : "") + shortId(key);
            if (disabled) lbl = "§m" + lbl;

            g.drawString(font, lbl, lx + 2, iy + (ENTRY_H - 8) / 2, color, false);
        }

        // Scrollbar indicator
        if (keys.size() > visCount) {
            int barH = Math.max(10, lh * visCount / keys.size());
            int barY = ly + (lh - barH) * listScroll / Math.max(1, keys.size() - visCount);
            g.fill(lx + LIST_W - 3, barY, lx + LIST_W, barY + barH, 0xFF888888);
        }
    }

    private void renderFormHeaders(GuiGraphics g) {
        int ex = LIST_W + PAD * 2;
        int ey = TAB_H + PAD * 2;
        int y  = ey - formScrollY;
        for (FormEntry fe : formEntries) {
            if (fe.box == null) {
                // Header row
                if (y >= ey - ROW_H && y < height - BOT_H) {
                    g.drawString(font, "§e" + fe.label, ex + PAD, y + 2, 0xFFFFFF44, false);
                }
                y += ROW_H - 4;
            } else {
                // Field label
                if (y >= ey - ROW_H && y < height - BOT_H) {
                    g.drawString(font, fe.label, ex + PAD, y + (FIELD_H - 8) / 2 + 1, 0xFFDDDDDD, false);
                }
                y += ROW_H;
            }
        }
    }

    // ── Mouse ─────────────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        // List click
        int lx = PAD;
        int ly = TAB_H + PAD;
        int lh = height - ly - BOT_H;
        if (mx >= lx && mx < lx + LIST_W && my >= ly && my < ly + lh) {
            int idx = ((int) my - ly) / ENTRY_H + listScroll;
            List<String> keys = new ArrayList<>(currentMap().keySet());
            if (idx >= 0 && idx < keys.size()) {
                String clicked = keys.get(idx);
                if (!clicked.equals(selectedId)) {
                    if (!commitRawIfNeeded()) return true; // block selection change on invalid JSON
                    select(clicked);
                }
                return true;
            }
        }
        return super.mouseClicked(mx, my, btn);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double scrollX, double scrollY) {
        int lx = PAD;
        int ly = TAB_H + PAD;
        int lh = height - ly - BOT_H;

        if (mx >= lx && mx < lx + LIST_W && my >= ly && my < ly + lh) {
            // Scroll the list
            int visCount = lh / ENTRY_H;
            int maxScroll = Math.max(0, currentMap().size() - visCount);
            listScroll = (int) Math.max(0, Math.min(maxScroll, listScroll - scrollY));
            return true;
        }

        int ex = LIST_W + PAD * 2;
        int ey = TAB_H + PAD;
        if (!rawMode && mx >= ex && my >= ey && my < height - BOT_H) {
            // Scroll the form
            int panelH = height - ey - BOT_H;
            int maxScroll = Math.max(0, formContentH - panelH);
            formScrollY = (int) Math.max(0, Math.min(maxScroll, formScrollY - scrollY * ENTRY_H));
            layoutFormEntries();
            return true;
        }

        return super.mouseScrolled(mx, my, scrollX, scrollY);
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    private static boolean isEntryDisabled(String json) {
        if (json == null || json.isBlank()) return false;
        try {
            JsonElement el = JsonParser.parseString(json);
            return el.isJsonObject()
                    && el.getAsJsonObject().has("disabled")
                    && el.getAsJsonObject().get("disabled").getAsBoolean();
        } catch (Exception e) {
            return false;
        }
    }

    private static String shortId(String fullId) {
        int colon = fullId.lastIndexOf(':');
        return colon >= 0 ? fullId.substring(colon + 1) : fullId;
    }

    private static final Gson PRETTY_GSON = new GsonBuilder().setPrettyPrinting().create();

    private static String prettyJson(JsonObject obj) {
        return PRETTY_GSON.toJson(obj);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
