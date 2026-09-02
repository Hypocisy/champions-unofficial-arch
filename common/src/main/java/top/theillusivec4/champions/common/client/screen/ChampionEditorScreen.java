package top.theillusivec4.champions.common.client.screen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import top.theillusivec4.champions.common.client.screen.editor.EditorSession;
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
 *
 * <p>Rendering contract: backgrounds/strips first, then {@code super.render}
 * (widgets), then text layers drawn inside scissored regions so scrolled
 * content clips cleanly at panel borders. Widgets are only shown when fully
 * inside the panel — no half-cut boxes overlapping borders.</p>
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

    // ── Layout ─────────────────────────────────────────────────────────────────

    private static final int TAB_H     = 18;
    private static final int STRIP_H   = 24;
    private static final int LIST_W    = 138;
    private static final int ENTRY_H   = 13;
    private static final int PAD       = 5;
    private static final int FIELD_ADV = 21;
    private static final int HEAD_ADV  = 20;
    private static final int INDENT_PX = 11;
    private static final int VALIDATOR_H = 46;

    // ── Palette — dark blue-gray + gold accent (fully opaque: no world/shader
    // blur bleeding through translucent fills) ──────────────────────────────

    private static final int C_BACKDROP  = 0xFF0D1014;
    private static final int C_PANEL     = 0xFF121620;
    private static final int C_BORDER    = 0xFF2B3442;
    private static final int C_STRIP     = 0xFF181D26;
    private static final int C_HEADBG    = 0xFF1A2029;
    private static final int C_SELECT    = 0xFF31445C;
    private static final int C_HOVER     = 0x22FFFFFF;
    private static final int C_ZEBRA     = 0x07FFFFFF;
    private static final int C_ACCENT    = 0xFFE3B557;
    private static final int C_ACCENT_BAR= 0xFFB98A38;
    private static final int C_LABEL     = 0xFFC9CFD8;
    private static final int C_HINT      = 0xFF79808B;
    private static final int C_SCROLL    = 0xFF566070;

    private static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().create();

    // ── State ──────────────────────────────────────────────────────────────────

    private final EditorSession session;
    private final List<Row> rows = new ArrayList<>();
    private JsonObject liveJson = null;
    private MultiLineEditBox rawEditor = null;
    private List<String> validationLines = List.of();
    private String saveError = null;
    private int labelW = 96;

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

    /** S2C entry: refresh packs in place if the editor is already open. */
    public static void receivePayload(EditorPayload payload) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof ChampionEditorScreen open && open.session != null) {
            open.session.packsSnapshot = payload.packs();
            open.rebuildForm();
        } else {
            EditorSession s = new EditorSession(payload, saveCallback);
            mc.setScreen(new ChampionEditorScreen(s));
        }
    }

    // ── Geometry ───────────────────────────────────────────────────────────────

    private int panelX()  { return LIST_W + PAD * 2; }
    private int panelY()  { return STRIP_H + PAD; }
    private int panelW()  { return width - panelX() - PAD; }
    private int panelH()  { return height - panelY() - STRIP_H - PAD; }
    private int listTop() { return STRIP_H + PAD; }
    private int listH()   { return height - listTop() - STRIP_H - PAD; }
    private int barY()    { return height - STRIP_H; }
    private int formBottom() { return panelY() + panelH(); }

    // ── Init ───────────────────────────────────────────────────────────────────

    @Override
    protected void init() {
        // Tab row — space for the Form/JSON toggle is always reserved so tab
        // positions never change between tabs.
        int avail = width - PAD * 2 - 100;
        int tabW = Math.max(46, (avail - 2 * 4) / 5);
        tabArchetypes = tab("Archetypes", EditorSession.Tab.ARCHETYPES, PAD, tabW);
        tabTiers      = tab("Tiers",      EditorSession.Tab.TIERS,      PAD + (tabW + 2), tabW);
        tabModifiers  = tab("Modifiers",  EditorSession.Tab.MODIFIERS,  PAD + (tabW + 2) * 2, tabW);
        tabConfig     = tab("Config",     EditorSession.Tab.CONFIG,     PAD + (tabW + 2) * 3, tabW);
        tabPacks      = tab("Packs",      EditorSession.Tab.PACKS,      PAD + (tabW + 2) * 4, tabW);

        int toggleX = width - PAD - 92;
        btnForm = addRenderableWidget(Button.builder(
                        Component.literal("Form"), b -> setRawMode(false))
                .bounds(toggleX, 3, 44, TAB_H).build());
        btnJson = addRenderableWidget(Button.builder(
                        Component.literal("JSON"), b -> setRawMode(true))
                .bounds(toggleX + 46, 3, 44, TAB_H).build());

        int botY = barY() + 3;
        btnNew = addRenderableWidget(Button.builder(
                Component.literal("§a+ New"), b -> onNew()).bounds(PAD, botY, 46, 18).build());
        btnDelete = addRenderableWidget(Button.builder(
                Component.literal("§cDelete"), b -> onDelete())
                .bounds(PAD + 50, botY, 54, 18).build());
        btnSave = addRenderableWidget(Button.builder(
                Component.literal("Save & Reload"), b -> onSave())
                .bounds(width - 192, botY, 102, 18).build());
        btnClose = addRenderableWidget(Button.builder(
                Component.literal("Close"), b -> onClose())
                .bounds(width - 86, botY, 82, 18).build());

        // Raw editor survives resize — re-add + reposition
        if (rawEditor != null) {
            addRenderableWidget(rawEditor);
            positionRawEditor();
        }

        refreshTabLabels();
        if (session.selectedId == null) selectFirst();
        else applySelection(session.selectedId);
    }

    private Button tab(String label, EditorSession.Tab tab, int x, int w) {
        return addRenderableWidget(Button.builder(
                Component.literal(label), b -> switchTab(tab)).bounds(x, 3, w, TAB_H).build());
    }

    // ── Tabs / view mode ───────────────────────────────────────────────────────

    private void switchTab(EditorSession.Tab tab) {
        if (!commitRawIfNeeded()) return;
        session.activeTab = tab;
        session.selectedId = null;
        session.listScroll = 0;
        session.formScrollY = 0;
        saveError = null;
        if (session.rawMode && (tab == EditorSession.Tab.CONFIG
                || tab == EditorSession.Tab.PACKS)) {
            session.rawMode = false;
            if (rawEditor != null) rawEditor.visible = false;
        }
        clearForm();
        refreshTabLabels();
        selectFirst();
    }

    private void refreshTabLabels() {
        var t = session.activeTab;
        tabArchetypes.setMessage(mark("Archetypes", t == EditorSession.Tab.ARCHETYPES));
        tabTiers     .setMessage(mark("Tiers",      t == EditorSession.Tab.TIERS));
        tabModifiers .setMessage(mark("Modifiers",  t == EditorSession.Tab.MODIFIERS));
        tabConfig    .setMessage(mark("Config",     t == EditorSession.Tab.CONFIG));
        tabPacks     .setMessage(mark("Packs",      t == EditorSession.Tab.PACKS));

        boolean hasToggle = t != EditorSession.Tab.CONFIG && t != EditorSession.Tab.PACKS;
        btnForm.visible = hasToggle;
        btnJson.visible = hasToggle;
        btnForm.setMessage(session.rawMode ? Component.literal("Form")
                : Component.literal("§e▸ Form"));
        btnJson.setMessage(session.rawMode ? Component.literal("§e▸ JSON")
                : Component.literal("JSON"));
        btnNew.visible = pane().newEntryPrefix() != null;
    }

    private static Component mark(String text, boolean active) {
        return active ? Component.literal("§e▸ " + text) : Component.literal(text);
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
        if (rawEditor != null) {
            if (!children().contains(rawEditor)) addRenderableWidget(rawEditor);
            positionRawEditor();
            return;
        }
        rawEditor = new MultiLineEditBox(font, 0, 0, 10, 10,
                Component.empty(), Component.literal("{}"));
        rawEditor.setValueListener(this::updateValidation);
        addRenderableWidget(rawEditor);
        positionRawEditor();
    }

    private void positionRawEditor() {
        if (rawEditor == null) return;
        rawEditor.setX(panelX());
        rawEditor.setY(panelY());
        rawEditor.setWidth(panelW());
        rawEditor.setHeight(Math.max(20, panelH() - VALIDATOR_H));
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

        boolean needsSelection = session.activeTab != EditorSession.Tab.CONFIG
                && session.activeTab != EditorSession.Tab.PACKS;
        if (session.rawMode) return;
        if (needsSelection && session.selectedId == null) return;

        String id = session.selectedId;
        JsonObject target = liveJson != null ? liveJson : new JsonObject();
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
        // dynamic, scale-aware label column
        int max = 0;
        for (Row r : rows) {
            if (!r.header && r.label != null) max = Math.max(max, font.width(r.label));
        }
        labelW = Math.max(60, Math.min(170, max + 10));

        int ex = panelX();
        int rightEdge = width - PAD;
        int top = panelY() + 4;
        int bottomLimit = formBottom() - 3;
        int y = top - session.formScrollY;

        for (Row r : rows) {
            r.y = y;
            if (r.header) {
                if (r.trailingButton != null) {
                    boolean inView = y >= top - HEAD_ADV && y + Row.HEADER_H <= bottomLimit;
                    r.trailingButton.setX(rightEdge - r.trailingButton.getWidth() - 6);
                    r.trailingButton.setY(y + 1);
                    r.trailingButton.visible = inView;
                }
                y += HEAD_ADV;
            } else {
                // fully-inside rule: no half-cut widgets overlapping the border
                boolean inView = y >= top - FIELD_ADV && y + Row.FIELD_H <= bottomLimit;
                int labelX = ex + 8 + r.indent * INDENT_PX;
                int fieldX = r.label != null ? labelX + labelW + 6 : labelX;
                int right = rightEdge - 8;
                for (int i = r.widgets.size() - 1; i >= 1; i--) {
                    AbstractWidget wgt = r.widgets.get(i);
                    wgt.setX(right - wgt.getWidth());
                    wgt.setY(y);
                    right -= wgt.getWidth() + 3;
                }
                if (!r.widgets.isEmpty()) {
                    AbstractWidget primary = r.widgets.get(0);
                    primary.setX(fieldX);
                    primary.setY(y);
                    primary.setWidth(Math.max(24, right - fieldX));
                }
                for (AbstractWidget w : r.widgets) {
                    if (!inView && getFocused() == w) setFocused(null);
                    w.visible = inView;
                }
                y += FIELD_ADV;
            }
        }
        formContentH = y - (top - session.formScrollY);
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
        if (session.activeTab == EditorSession.Tab.PACKS) return;
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

    // ── Rendering ──────────────────────────────────────────────────────────────

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        // 1. Backdrop & static chrome
        g.fill(0, 0, width, height, C_BACKDROP);
        g.fill(0, 0, width, STRIP_H, C_STRIP);
        g.fill(0, 0, width, 1, C_BORDER);
        g.fill(0, barY(), width, height, C_STRIP);
        g.fill(0, barY(), width, 1, C_BORDER);

        // list panel + header
        frame(g, PAD, listTop(), LIST_W, listH());
        g.fill(PAD + 1, listTop() + 1, PAD + LIST_W - 1, listTop() + 13, C_HEADBG);
        g.drawString(font, "§8ENTRIES §7" + session.currentMap().size(),
                PAD + 6, listTop() + 3, C_HINT, false);

        // form panel
        frame(g, panelX(), panelY(), panelW(), panelH());

        // 2. Widgets
        super.render(g, mx, my, pt);

        // 3. Scissored text layers — scrolled content clips at panel borders
        g.enableScissor(PAD + 1, listTop() + 14, LIST_W - 2, listH() - 15);
        renderEntryList(g, mx, my);
        g.disableScissor();

        if (!session.rawMode) {
            g.enableScissor(panelX() + 1, panelY() + 1, panelW() - 2, panelH() - 2);
            renderForm(g);
            g.disableScissor();
        }

        // 4. Validation strip (JSON mode)
        if (session.rawMode && rawEditor != null) {
            int vy = formBottom() - VALIDATOR_H + 4;
            g.fill(panelX() + 1, vy - 4, width - PAD - 1, formBottom() - 1, 0xE60C0F14);
            g.fill(panelX() + 1, vy - 4, width - PAD - 1, vy - 3, C_BORDER);
            for (int i = 0; i < Math.min(3, validationLines.size()); i++) {
                g.drawString(font, clip(font, validationLines.get(i), panelW() - 14),
                        panelX() + 8, vy + i * 10, 0xFFD5D9E0, false);
            }
        }

        // bottom bar status
        if (saveError != null) {
            g.drawString(font, clip(font, "§c" + saveError, width / 2),
                    PAD + 112, barY() + 8, 0xFFFF6B6B, false);
        } else if (!session.dirtyIds.isEmpty()) {
            g.drawString(font, "§e● " + session.dirtyIds.size() + " unsaved",
                    PAD + 112, barY() + 8, C_ACCENT, false);
        }
    }

    private void frame(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x - 1, y - 1, x + w + 1, y + h + 1, C_BORDER);
        g.fill(x, y, x + w, y + h, C_PANEL);
    }

    private void renderEntryList(GuiGraphics g, int mx, int my) {
        int lx = PAD, ly = listTop() + 14;
        int lh = listH() - 15;
        int visCount = lh / ENTRY_H;

        List<String> keys = new ArrayList<>(session.currentMap().keySet());
        int endIdx = Math.min(session.listScroll + visCount, keys.size());

        for (int i = session.listScroll; i < endIdx; i++) {
            String key = keys.get(i);
            int iy = ly + (i - session.listScroll) * ENTRY_H;
            boolean sel = key.equals(session.selectedId);
            boolean hover = mx >= lx && mx < lx + LIST_W && my >= iy && my < iy + ENTRY_H;

            if (sel) g.fill(lx + 1, iy, lx + LIST_W - 1, iy + ENTRY_H, C_SELECT);
            else if (hover) g.fill(lx + 1, iy, lx + LIST_W - 1, iy + ENTRY_H, C_HOVER);
            else if ((i & 1) == 1) g.fill(lx + 1, iy, lx + LIST_W - 1, iy + ENTRY_H, C_ZEBRA);
            if (sel) g.fill(lx + 1, iy, lx + 3, iy + ENTRY_H, C_ACCENT_BAR);

            String json = session.currentMap().getOrDefault(key, "{}");
            boolean disabled = isEntryDisabled(json)
                    || (session.activeTab == EditorSession.Tab.PACKS && "disabled".equals(json));
            boolean builtin = session.builtinIds.contains(key);
            int color;
            if      (disabled) color = 0xFF9A6B6B;
            else if (builtin)  color = 0xFF8FB4D9;
            else               color = 0xFFDCDCDC;

            String lbl = (session.dirtyIds.contains(key) ? "* " : "") + shortId(key);
            if (disabled) lbl = "§m" + lbl;
            g.drawString(font, clip(font, lbl, LIST_W - 10), lx + 6,
                    iy + (ENTRY_H - 8) / 2, color, false);
        }

        if (keys.size() > visCount && keys.size() - visCount > 0) {
            int barH = Math.max(8, lh * visCount / keys.size());
            int barY = ly + (lh - barH) * session.listScroll / (keys.size() - visCount);
            g.fill(lx + LIST_W - 2, barY, lx + LIST_W, barY + barH, C_SCROLL);
        }
    }

    private void renderForm(GuiGraphics g) {
        int ex = panelX();
        int right = width - PAD - 1;
        for (Row r : rows) {
            int x = ex + 8 + r.indent * INDENT_PX;
            if (r.header) {
                if (r.label == null) continue;
                boolean hint = r.label.startsWith("§7") || r.label.startsWith("§8");
                if (hint) {
                    g.drawString(font, clip(font, r.label, right - x), x, r.y + 3,
                            C_HINT, false);
                } else {
                    // section header: dark strip + gold accent bar + title
                    g.fill(x - 4, r.y - 2, right, r.y + Row.HEADER_H - 3, C_HEADBG);
                    g.fill(x - 4, r.y - 2, x - 2, r.y + Row.HEADER_H - 3, C_ACCENT_BAR);
                    g.drawString(font, clip(font, r.label, right - x - 4), x + 2, r.y + 2,
                            C_ACCENT, false);
                }
            } else if (r.label != null) {
                g.drawString(font, clip(font, r.label, labelW), x, r.y + 5,
                        C_LABEL, false);
            }
        }
    }

    // ── Mouse ──────────────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        int ly = listTop() + 14;
        int lh = listH() - 15;
        if (mx >= PAD && mx < PAD + LIST_W && my >= ly && my < ly + lh) {
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
        int ly = listTop() + 14;
        int lh = listH() - 15;
        if (mx >= PAD && mx < PAD + LIST_W && my >= ly && my < ly + lh) {
            int visCount = lh / ENTRY_H;
            int maxScroll = Math.max(0, session.currentMap().size() - visCount);
            session.listScroll = (int) Math.max(0,
                    Math.min(maxScroll, session.listScroll - scrollY));
            return true;
        }
        if (!session.rawMode && mx >= panelX() && my >= panelY() && my < formBottom()) {
            int maxScroll = Math.max(0, formContentH - panelH());
            session.formScrollY = (int) Math.max(0,
                    Math.min(maxScroll, session.formScrollY - scrollY * ENTRY_H));
            layoutForm();
            return true;
        }
        return super.mouseScrolled(mx, my, scrollX, scrollY);
    }

    // ── Utility ────────────────────────────────────────────────────────────────

    private static String clip(Font font, String s, int maxW) {
        return font.plainSubstrByWidth(s, Math.max(0, maxW));
    }

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
