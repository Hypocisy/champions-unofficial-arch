package top.theillusivec4.champions.common.client.screen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import top.theillusivec4.champions.common.client.screen.editor.EditorSession;
import top.theillusivec4.champions.common.client.screen.editor.json.JsonPathOps;
import top.theillusivec4.champions.common.client.screen.editor.pane.ArchetypePane;
import top.theillusivec4.champions.common.client.screen.editor.pane.ConfigPane;
import top.theillusivec4.champions.common.client.screen.editor.pane.EditorPane;
import top.theillusivec4.champions.common.client.screen.editor.pane.ModifierPane;
import top.theillusivec4.champions.common.client.screen.editor.pane.PacksPane;
import top.theillusivec4.champions.common.client.screen.editor.pane.TierPane;
import top.theillusivec4.champions.common.client.screen.editor.validate.JsonValidator;
import top.theillusivec4.champions.common.client.screen.editor.widget.FormBuilder;
import top.theillusivec4.champions.common.client.screen.editor.widget.Row;
import top.theillusivec4.champions.common.network.EditorPackActionPacket;
import top.theillusivec4.champions.common.network.EditorPayload;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * In-game Champions editor shell: tab bar, entry list, raw-JSON view with live
 * validation, and the form panel delegated to per-tab {@link EditorPane}s.
 */
public final class ChampionEditorScreen extends Screen {

    // ── Platform wiring ────────────────────────────────────────────────────────

    private static Consumer<EditorPayload> saveCallback;
    public static void setSaveCallback(Consumer<EditorPayload> cb) { saveCallback = cb; }

    private static Consumer<EditorPackActionPacket> packActionCallback;
    public static void setPackActionCallback(Consumer<EditorPackActionPacket> cb) {
        packActionCallback = cb;
    }

    public static void sendPackAction(EditorPackActionPacket packet) {
        if (packActionCallback != null) packActionCallback.accept(packet);
    }

    // ── Layout constants ───────────────────────────────────────────────────────

    private static final int TAB_H   = 20;
    private static final int LIST_W  = 150;
    private static final int ENTRY_H = 14;
    private static final int PAD     = 4;
    private static final int BOT_H   = 28;
    private static final int LABEL_W = 110;
    private static final int INDENT_PX = 12;
    private static final int VALIDATOR_H = 48;

    private static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().create();

    // ── State ──────────────────────────────────────────────────────────────────

    private final EditorSession session;
    private final List<Row> rows = new ArrayList<>();
    private JsonObject liveJson = null;
    private MultiLineEditBox rawEditor = null;
    private List<String> validationLines = List.of();
    private String saveError = null;

    private Button tabArchetypes, tabTiers, tabModifiers, tabConfig, tabPacks;
    private Button btnNew, btnDelete, btnSave, btnClose;
    private Button btnForm, btnJson;

    // ── Entry / platform API ───────────────────────────────────────────────────

    public ChampionEditorScreen(EditorSession session) {
        super(Component.literal("Champions Editor"));
        this.session = session;
    }

    public static void open(EditorPayload payload) {
        EditorSession s = new EditorSession(payload, saveCallback);
        Minecraft.getInstance().setScreen(new ChampionEditorScreen(s));
    }

    /**
     * S2C entry: called when the server pushes a fresh editor payload. If the
     * editor is already open (e.g. after a pack toggle/import), refresh the packs
     * list in place instead of replacing the screen.
     */
    public static void receivePayload(EditorPayload payload) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof ChampionEditorScreen open
                && open.session != null) {
            open.session.packsSnapshot = payload.packs();
            open.rebuildForm();
        } else {
            EditorSession s = new EditorSession(payload, saveCallback);
            mc.setScreen(new ChampionEditorScreen(s));
        }
    }

    // ── Init ───────────────────────────────────────────────────────────────────

    @Override
    protected void init() {
        int w = width;
        int tw = 78;
        tabArchetypes = tab("Archetypes", EditorSession.Tab.ARCHETYPES, PAD, tw);
        tabTiers       = tab("Tiers",      EditorSession.Tab.TIERS, PAD + (tw + 2), tw);
        tabModifiers   = tab("Modifiers",  EditorSession.Tab.MODIFIERS, PAD + (tw + 2) * 2, tw);
        tabConfig      = tab("Config",     EditorSession.Tab.CONFIG, PAD + (tw + 2) * 3, tw);
        tabPacks       = tab("Packs",      EditorSession.Tab.PACKS, PAD + (tw + 2) * 4, tw);

        int toggleX = w - PAD - 90;
        btnForm = addRenderableWidget(Button.builder(
                        Component.literal("Form"), b -> setRawMode(false))
                .bounds(toggleX, PAD, 44, TAB_H).build());
        btnJson = addRenderableWidget(Button.builder(
                        Component.literal("JSON"), b -> setRawMode(true))
                .bounds(toggleX + 46, PAD, 44, TAB_H).build());

        int botY = height - BOT_H + 4;
        btnNew = addRenderableWidget(Button.builder(
                Component.literal("New"), b -> onNew()).bounds(PAD, botY, 50, 20).build());
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
        if (session.selectedId == null) selectFirst();
        else applySelection(session.selectedId);
    }

    private Button tab(String label, EditorSession.Tab tab, int x, int w) {
        return addRenderableWidget(Button.builder(
                Component.literal(label), b -> switchTab(tab)).bounds(x, PAD, w, TAB_H).build());
    }

    // ── Tabs / view mode ───────────────────────────────────────────────────────

    private void switchTab(EditorSession.Tab tab) {
        if (!commitRawIfNeeded()) return;
        session.activeTab = tab;
        session.selectedId = null;
        session.listScroll = 0;
        session.formScrollY = 0;
        saveError = null;
        clearForm();
        // CONFIG / PACKS have no JSON view — leave raw mode before switching
        if (session.rawMode && (tab == EditorSession.Tab.CONFIG
                || tab == EditorSession.Tab.PACKS)) {
            session.rawMode = false;
            if (rawEditor != null) rawEditor.visible = false;
        }
        refreshTabLabels();
        selectFirst();
    }

    private void refreshTabLabels() {
        var t = session.activeTab;
        tabArchetypes.active = t != EditorSession.Tab.ARCHETYPES;
        tabTiers      .active = t != EditorSession.Tab.TIERS;
        tabModifiers  .active = t != EditorSession.Tab.MODIFIERS;
        tabConfig     .active = t != EditorSession.Tab.CONFIG;
        tabPacks      .active = t != EditorSession.Tab.PACKS;
        boolean hasToggle = t != EditorSession.Tab.CONFIG && t != EditorSession.Tab.PACKS;
        btnForm.visible = hasToggle;
        btnJson.visible = hasToggle;
        btnForm.active = !session.rawMode;
        btnJson.active = session.rawMode;
        btnNew.visible = pane().newEntryPrefix() != null;
    }

    private EditorPane pane() {
        return switch (session.activeTab) {
            case ARCHETYPES -> new ArchetypePane();
            case TIERS      -> new TierPane();
            case MODIFIERS  -> new ModifierPane();
            case CONFIG     -> new ConfigPane();
            case PACKS      -> new PacksPane();
        };
    }

    private void setRawMode(boolean raw) {
        if (session.rawMode == raw) return;
        if (raw) {
            session.rawMode = true;
            ensureRawEditor();
            rawEditor.setValue(liveJson != null ? PRETTY.toJson(liveJson) : "");
            setFormWidgetsVisible(false);
            rawEditor.visible = true;
            updateValidation(rawEditor.getValue());
        } else {
            if (!commitRawIfNeeded()) return;
            session.rawMode = false;
            if (rawEditor != null) rawEditor.visible = false;
            rebuildForm();
            setFormWidgetsVisible(true);
        }
        refreshTabLabels();
    }

    private boolean commitRawIfNeeded() {
        if (!session.rawMode || rawEditor == null || session.selectedId == null) return true;
        String text = rawEditor.getValue().trim();
        if (text.isEmpty()) return true;
        try {
            var parsed = JsonParser.parseString(text);
            if (!parsed.isJsonObject()) {
                saveError = "JSON must be an object";
                return false;
            }
            liveJson = parsed.getAsJsonObject();
            session.commit(session.selectedId, PRETTY.toJson(liveJson));
            saveError = null;
            return true;
        } catch (JsonSyntaxException e) {
            saveError = "Invalid JSON: " + e.getMessage();
            return false;
        }
    }

    private void ensureRawEditor() {
        if (rawEditor != null) return;
        int ex = panelX(), ey = panelY();
        int ew = width - ex - PAD;
        int eh = panelH() - VALIDATOR_H;
        rawEditor = new MultiLineEditBox(font, ex, ey, ew, eh,
                Component.empty(), Component.literal("{}"));
        addRenderableWidget(rawEditor);
        rawEditor.setValueListener(this::updateValidation);
    }

    private void updateValidation(String text) {
        validationLines = JsonValidator.validate(text,
                session.activeTab == EditorSession.Tab.ARCHETYPES,
                session.activeTab == EditorSession.Tab.MODIFIERS);
    }

    // ── Selection ──────────────────────────────────────────────────────────────

    private void selectFirst() {
        var map = session.currentMap();
        if (map.isEmpty()) { applySelection(null); return; }
        applySelection(map.keySet().iterator().next());
    }

    private void select(String id) {
        if (!commitRawIfNeeded()) return;
        applySelection(id);
    }

    private void applySelection(String id) {
        session.selectedId = id;
        session.formScrollY = 0;
        saveError = null;
        clearForm();

        liveJson = null;
        if (id != null && session.activeTab != EditorSession.Tab.PACKS) {
            String json = session.currentMap().getOrDefault(id, "{}");
            if (!isEntryDisabled(json)) {
                try { liveJson = JsonParser.parseString(json).getAsJsonObject(); }
                catch (Exception e) { liveJson = new JsonObject(); }
            }
        }

        if (session.rawMode) {
            ensureRawEditor();
            rawEditor.setValue(liveJson != null ? PRETTY.toJson(liveJson) : "");
            rawEditor.visible = true;
            updateValidation(rawEditor.getValue());
        } else {
            if (rawEditor != null) rawEditor.visible = false;
            rebuildForm();
        }
    }

    // ── Form lifecycle ─────────────────────────────────────────────────────────

    private void clearForm() {
        for (Row r : rows) {
            for (AbstractWidget w : r.widgets) removeWidget(w);
            if (r.trailingButton != null) removeWidget(r.trailingButton);
        }
        rows.clear();
    }

    private void setFormWidgetsVisible(boolean visible) {
        for (Row r : rows) {
            for (AbstractWidget w : r.widgets) w.visible = visible;
            if (r.trailingButton != null) r.trailingButton.visible = visible;
        }
    }

    private void rebuildForm() {
        clearForm();
        if (session.selectedId == null || session.rawMode) return;

        String id = session.selectedId;
        boolean editableJson = session.activeTab != EditorSession.Tab.PACKS
                && session.activeTab != EditorSession.Tab.CONFIG;
        JsonObject target = liveJson;
        if (!editableJson) target = new JsonObject(); // config/packs use direct writes

        FormBuilder fb = new FormBuilder(font, target, session, id, this::rebuildForm);
        pane().buildForm(fb, id);
        rows.addAll(fb.rows());
        for (Row r : rows) {
            for (AbstractWidget w : r.widgets) addRenderableWidget(w);
            if (r.trailingButton != null) addRenderableWidget(r.trailingButton);
        }
        layoutForm();
    }

    private void layoutForm() {
        int ex = panelX(), ey = panelY();
        int rightEdge = width - PAD;
        int y = ey - session.formScrollY;
        for (Row r : rows) {
            r.y = y;
            if (r.header) {
                if (r.trailingButton != null) {
                    r.trailingButton.setX(rightEdge - r.trailingButton.getWidth());
                    r.trailingButton.setY(y - 1);
                }
                y += Row.HEADER_H;
            } else {
                int labelX = ex + PAD + r.indent * INDENT_PX;
                int fieldX = labelX + LABEL_W + PAD;
                // trailing (fixed-width) widgets first, from the right
                int right = rightEdge - PAD;
                for (int i = r.widgets.size() - 1; i >= 1; i--) {
                    AbstractWidget wgt = r.widgets.get(i);
                    wgt.setX(right - wgt.getWidth());
                    right -= wgt.getWidth() + 2;
                }
                if (!r.widgets.isEmpty()) {
                    AbstractWidget primary = r.widgets.get(0);
                    primary.setX(fieldX);
                    primary.setY(y);
                    primary.setWidth(Math.max(20, right - fieldX));
                }
                y += Row.FIELD_H;
            }
        }
        formContentH = y - (ey - session.formScrollY);
    }

    private int formContentH = 0;

    // ── New / Delete / Save / Close ────────────────────────────────────────────

    private void onNew() {
        if (!commitRawIfNeeded()) return;
        String prefix = pane().newEntryPrefix();
        if (prefix == null) return;
        String base = prefix;
        int n = 1;
        while (session.currentMap().containsKey(base)) base = prefix + "_" + (n++);
        session.currentMap().put(base, "{}");
        session.markDirty(base);
        applySelection(base);
    }

    private void onDelete() {
        if (session.selectedId == null) return;
        if (session.activeTab == EditorSession.Tab.PACKS) return; // packs: use toggle
        if (!commitRawIfNeeded()) return;
        String id = session.selectedId;
        if (session.builtinIds.contains(id)) {
            session.currentMap().put(id, "{\"disabled\":true}");
            session.markDirty(id);
            applySelection(id);
        } else {
            session.currentMap().remove(id);
            session.dirtyIds.remove(id);
            clearForm();
            selectFirst();
        }
    }

    private void onSave() {
        if (!commitRawIfNeeded()) return;
        if (saveCallback != null) saveCallback.accept(session.toPayload());
        session.dirtyIds.clear();
    }

    @Override
    public void onClose() {
        commitRawIfNeeded();
        if (minecraft != null) minecraft.setScreen(null);
    }

    // ── Geometry helpers ───────────────────────────────────────────────────────

    private int panelX() { return LIST_W + PAD * 2; }
    private int panelY() { return TAB_H + PAD * 2; }
    private int panelH() { return height - panelY() - BOT_H; }

    // ── Rendering ──────────────────────────────────────────────────────────────

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        super.render(g, mx, my, pt);

        // Panels
        g.fill(PAD - 1, TAB_H + PAD - 1, PAD + LIST_W + 1, height - BOT_H + 1, 0xFF333333);
        int ex = panelX(), ey = panelY();
        g.fill(ex - 1, ey - 1, width - PAD + 1, height - BOT_H + 1, 0xFF222222);

        renderEntryList(g, mx, my);
        if (!session.rawMode) renderForm(g);

        // Validation (JSON mode) — bottom strip of the editor panel
        if (session.rawMode && rawEditor != null) {
            int vy = ey + panelH() - VALIDATOR_H + 4;
            for (int i = 0; i < Math.min(3, validationLines.size()); i++) {
                g.drawString(font, validationLines.get(i), ex + 4, vy + i * 11,
                        0xFFDDDDDD, false);
            }
        }

        if (saveError != null) {
            g.drawString(font, "§c" + saveError, ex + 2, height - BOT_H - 12,
                    0xFFFF5555, false);
        }
    }

    private void renderEntryList(GuiGraphics g, int mx, int my) {
        int lx = PAD, ly = TAB_H + PAD;
        int lh = height - ly - BOT_H;
        int visCount = lh / ENTRY_H;

        List<String> keys = new ArrayList<>(session.currentMap().keySet());
        int startIdx = session.listScroll;
        int endIdx = Math.min(startIdx + visCount, keys.size());

        for (int i = startIdx; i < endIdx; i++) {
            String key = keys.get(i);
            int iy = ly + (i - startIdx) * ENTRY_H;
            boolean sel = key.equals(session.selectedId);
            if (sel) g.fill(lx, iy, lx + LIST_W, iy + ENTRY_H, 0xFF555577);

            String json = session.currentMap().getOrDefault(key, "{}");
            boolean disabled = isEntryDisabled(json)
                    || (session.activeTab == EditorSession.Tab.PACKS && "disabled".equals(json));
            boolean builtin = session.builtinIds.contains(key);
            boolean dirty = session.dirtyIds.contains(key);
            int color;
            if      (disabled) color = sel ? 0xFFFF8888 : 0xFF885555;
            else if (builtin)  color = sel ? 0xFFCCDDFF : 0xFF7799BB;
            else               color = sel ? 0xFFFFFFCC : 0xFFAAAAAA;

            String lbl = (dirty ? "* " : "") + shortId(key);
            if (disabled) lbl = "§m" + lbl;
            g.drawString(font, lbl, lx + 2, iy + (ENTRY_H - 8) / 2, color, false);
        }

        if (keys.size() > visCount) {
            int barH = Math.max(10, lh * visCount / keys.size());
            int maxScroll = Math.max(1, keys.size() - visCount);
            int barY = ly + (lh - barH) * session.listScroll / maxScroll;
            g.fill(lx + LIST_W - 3, barY, lx + LIST_W, barY + barH, 0xFF888888);
        }
    }

    private void renderForm(GuiGraphics g) {
        int ey = panelY();
        for (Row r : rows) {
            if (r.y < ey - Row.HEADER_H || r.y >= height - BOT_H) continue;
            if (r.header) {
                g.drawString(font, r.label, panelX() + PAD + r.indent * INDENT_PX,
                        r.y + 3, 0xFFFFFF44, false);
            } else if (r.label != null) {
                g.drawString(font, r.label, panelX() + PAD + r.indent * INDENT_PX,
                        r.y + 5, 0xFFDDDDDD, false);
            }
        }
    }

    // ── Mouse ──────────────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        int lx = PAD, ly = TAB_H + PAD;
        int lh = height - ly - BOT_H;
        if (mx >= lx && mx < lx + LIST_W && my >= ly && my < ly + lh) {
            int idx = ((int) my - ly) / ENTRY_H + session.listScroll;
            List<String> keys = new ArrayList<>(session.currentMap().keySet());
            if (idx >= 0 && idx < keys.size()) {
                if (!keys.get(idx).equals(session.selectedId)) select(keys.get(idx));
                return true;
            }
        }
        return super.mouseClicked(mx, my, btn);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double scrollX, double scrollY) {
        int lx = PAD, ly = TAB_H + PAD;
        int lh = height - ly - BOT_H;
        if (mx >= lx && mx < lx + LIST_W && my >= ly && my < ly + lh) {
            int visCount = lh / ENTRY_H;
            int maxScroll = Math.max(0, session.currentMap().size() - visCount);
            session.listScroll = (int) Math.max(0,
                    Math.min(maxScroll, session.listScroll - scrollY));
            return true;
        }
        int ex = panelX(), ey = TAB_H + PAD;
        if (!session.rawMode && mx >= ex && my >= ey && my < height - BOT_H) {
            int panelH = panelH();
            int maxScroll = Math.max(0, formContentH - panelH);
            session.formScrollY = (int) Math.max(0,
                    Math.min(maxScroll, session.formScrollY - scrollY * ENTRY_H));
            layoutForm();
            return true;
        }
        return super.mouseScrolled(mx, my, scrollX, scrollY);
    }

    // ── Utility ────────────────────────────────────────────────────────────────

    private static boolean isEntryDisabled(String json) {
        if (json == null || json.isBlank()) return false;
        try {
            var el = JsonParser.parseString(json);
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

    @Override
    public boolean isPauseScreen() { return false; }
}
