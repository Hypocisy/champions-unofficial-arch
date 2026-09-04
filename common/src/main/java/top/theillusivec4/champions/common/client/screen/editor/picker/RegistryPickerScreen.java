package top.theillusivec4.champions.common.client.screen.editor.picker;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import top.theillusivec4.champions.common.client.screen.editor.EditorLang;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Generic searchable single/multi-select picker used for entity types, attributes,
 * mob effects, mod namespaces, affixes, filter types, etc.
 *
 * <p>Click toggles selection (single mode replaces the selection). Done applies
 * {@code onCommit} with the selected ids and returns to the editor via {@code back}.</p>
 */
public final class RegistryPickerScreen extends Screen {

    private static final int PAD = 6;
    private static final int ENTRY_H = 12;
    private static final int FOOT_H = 26;

    private final List<PickerEntry> all;
    private final boolean multi;
    private final Set<String> selected;
    private final Consumer<Set<String>> onCommit;
    private final Runnable back;

    private EditBox search;
    private List<PickerEntry> filtered = new ArrayList<>();
    private int scroll;

    private int x0, y0, x1, y1;

    private RegistryPickerScreen(String title, List<PickerEntry> entries, boolean multi,
                                 Set<String> preselected, Consumer<Set<String>> onCommit,
                                 Runnable back) {
        super(Component.literal(title));
        this.all = entries;
        this.multi = multi;
        this.selected = new LinkedHashSet<>(preselected);
        this.onCommit = onCommit;
        this.back = back;
    }

    public static RegistryPickerScreen create(String title, List<PickerEntry> entries,
                                              boolean multi, Set<String> preselected,
                                              Consumer<Set<String>> onCommit, Runnable back) {
        return new RegistryPickerScreen(title, entries, multi, preselected, onCommit, back);
    }

    @Override
    protected void init() {
        // Full-screen-width modal: the old centered 340px-capped window read as
        // "selection area truncated to half the screen". Span edge to edge with
        // a small margin on both axes so rows and buttons are easy to hit.
        int w = Math.max(200, width - 30);
        int h = Math.max(120, height - 20);
        x0 = (width - w) / 2;
        y0 = (height - h) / 2;
        x1 = x0 + w;
        y1 = y0 + h;

        search = new EditBox(Minecraft.getInstance().font,
                x0 + PAD, y0 + 26, w - PAD * 2, 18, Component.literal("search"));
        search.setMaxLength(256);
        search.setHint(Component.translatable("gui.champions.picker.search_hint"));
        search.setResponder(q -> { recompute(); scroll = 0; });
        addRenderableWidget(search);

        int btnY = y1 - FOOT_H + 4;
        addRenderableWidget(Button.builder(Component.translatable("gui.champions.picker.done"), b -> finish(true))
                .bounds(x1 - 160, btnY, 76, 18).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.champions.picker.cancel"), b -> finish(false))
                .bounds(x1 - 80, btnY, 76, 18).build());

        recompute();
        this.setInitialFocus(search);
    }

    private void finish(boolean apply) {
        if (apply) onCommit.accept(new LinkedHashSet<>(selected));
        back.run();
    }

    private void recompute() {
        String q = search == null ? "" : search.getValue().toLowerCase().trim();
        filtered = new ArrayList<>();
        for (PickerEntry e : all) {
            if (q.isEmpty()
                    || e.id().toLowerCase().contains(q)
                    || e.display().getString().toLowerCase().contains(q)) {
                filtered.add(e);
            }
        }
    }

    /**
     * Chrome layer — replaces vanilla's blur + gradient background. Called by
     * {@code Screen.render} BEFORE widgets, so the window frame sits under the
     * search box / buttons while our opaque fills suppress the vanilla menu blur.
     */
    @Override
    public void renderBackground(GuiGraphics g) {
        // opaque backdrop (no world/shader blur bleeding through)
        g.fill(0, 0, width, height, 0xFF0D1014);
        // window frame
        g.fill(x0 - 1, y0 - 1, x1 + 1, y1 + 1, 0xFF2B3442);
        g.fill(x0, y0, x1, y1, 0xFF12161F);
        // title strip (title INSIDE the window)
        g.fill(x0, y0, x1, y0 + 22, 0xFF1A2029);
        g.fill(x0, y0, x0 + 2, y0 + 22, 0xFFB98A38);
        g.drawString(font, getTitle(), x0 + 8, y0 + 7, 0xFFE3B557, false);
        g.fill(x0, y0 + 22, x1, y0 + 23, 0xFF2B3442);
        // list area + footer backgrounds
        g.fill(x0 + 1, listY(), x1 - 1, listY() + listH(), 0xFF0C0F14);
        g.fill(x0, y1 - FOOT_H, x1, y1, 0xFF12161F);
        g.fill(x0, y1 - FOOT_H, x1, y1 - FOOT_H + 1, 0xFF2B3442);
    }

    /**
     * Widget + text layers: {@code super.render} paints the chrome via
     * {@link #renderBackground} and then the widgets (search box, Done/Cancel);
     * afterwards the list rows go on top.
     */
    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        // 1. chrome (renderBackground) + widgets
        super.render(g, mx, my, pt);

        // 2. list rows — scissored to the list area
        int ly = listY();
        int lh = listH();
        int vis = Math.max(1, lh / ENTRY_H);
        int max = Math.max(0, filtered.size() - vis);
        scroll = Math.min(scroll, max);

        g.enableScissor(x0 + 1, ly, x1 - 1, ly + lh);
        for (int i = scroll; i < Math.min(filtered.size(), scroll + vis); i++) {
            PickerEntry e = filtered.get(i);
            int iy = ly + (i - scroll) * ENTRY_H;
            boolean sel = selected.contains(e.id());
            boolean hover = mx >= x0 && mx <= x1 && my >= iy && my < iy + ENTRY_H;

            if (sel) g.fill(x0 + 1, iy, x1 - 1, iy + ENTRY_H, 0xFF31445C);
            else if (hover) g.fill(x0 + 1, iy, x1 - 1, iy + ENTRY_H, 0x22FFFFFF);

            String box = multi ? (sel ? "☑ " : "☐ ") : (sel ? "● " : "○ ");
            String name = e.display().getString();
            int nameColor = sel ? 0xFFE3B557 : 0xFFDCDCDC;
            String idTxt = e.id();
            int idW = Math.min(120, font.width(idTxt) + 4);
            String clippedName = font.plainSubstrByWidth(
                    box + name, (x1 - x0) - PAD * 2 - idW);
            g.drawString(font, clippedName, x0 + PAD, iy + 2, nameColor, false);
            g.drawString(font, "§8" + font.plainSubstrByWidth(idTxt, idW),
                    x1 - PAD - idW, iy + 2, 0xFF777777, false);
        }
        g.disableScissor();

        // 3. scrollbar + footer info
        if (filtered.size() > vis && max > 0) {
            int barH = Math.max(8, lh * vis / filtered.size());
            int barY = ly + (lh - barH) * scroll / max;
            g.fill(x1 - 2, barY, x1, barY + barH, 0xFF566070);
        }
        Component info = Component.translatable("gui.champions.picker.count", filtered.size(), all.size());
        if (multi) {
            info = Component.translatable("gui.champions.picker.selected", selected.size())
                    .append(" ").append(info);
        }
        g.drawString(font, info, x0 + PAD, y1 - FOOT_H + 6, 0xFF79808B, false);
    }

    private int listY() { return y0 + 48; }
    private int listH() { return (y1 - FOOT_H) - listY() - 2; }

    private static String tr(String key, Object... args) {
        return EditorLang.tr(key, args);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        int ly = listY();
        int lh = listH();
        if (mx >= x0 && mx <= x1 && my >= ly && my < ly + lh) {
            int idx = ((int) my - ly) / ENTRY_H + scroll;
            if (idx >= 0 && idx < filtered.size()) {
                String id = filtered.get(idx).id();
                if (multi) {
                    if (!selected.remove(id)) selected.add(id);
                } else {
                    selected.clear();
                    selected.add(id);
                }
                return true;
            }
        }
        return super.mouseClicked(mx, my, btn);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double delta) {
        int ly = listY();
        int lh = listH();
        if (mx >= x0 && mx <= x1 && my >= ly && my < ly + lh) {
            int vis = Math.max(1, lh / ENTRY_H);
            int max = Math.max(0, filtered.size() - vis);
            scroll = (int) Math.max(0, Math.min(max, scroll - delta));
            return true;
        }
        return super.mouseScrolled(mx, my, delta);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
