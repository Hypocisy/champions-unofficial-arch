package top.theillusivec4.champions.common.client.screen.editor.picker;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

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
    private static final int ENTRY_H = 14;
    private static final int FOOT_H = 30;

    private final List<PickerEntry> all;
    private final boolean multi;
    private final Set<String> selected;
    private final Consumer<Set<String>> onCommit;
    private final Runnable back;

    private EditBox search;
    private List<PickerEntry> filtered = new ArrayList<>();
    private int scroll;

    private RegistryPickerScreen(String title, List<PickerEntry> entries, boolean multi,
                                 Set<String> preselected, Consumer<Set<String>> onCommit,
                                 Runnable back) {
        super(Component.literal(title));
        this.all = entries;
        this.multi = multi;
        this.selected = new LinkedHashSet<>(preselected);
        this.onCommit = onCommit;
        this.back = back;
        recompute();
    }

    public static RegistryPickerScreen create(String title, List<PickerEntry> entries,
                                              boolean multi, Set<String> preselected,
                                              Consumer<Set<String>> onCommit, Runnable back) {
        return new RegistryPickerScreen(title, entries, multi, preselected, onCommit, back);
    }

    @Override
    protected void init() {
        int w = Math.min(320, width - 40);
        int h = Math.min(340, height - 40);
        // centered window
        x0 = (width - w) / 2;
        y0 = (height - h) / 2;
        x1 = x0 + w;
        y1 = y0 + h;

        search = new EditBox(Minecraft.getInstance().font,
                x0 + PAD, y0 + PAD, w - PAD * 2, 16, Component.literal("search"));
        search.setMaxLength(256);
        search.setHint(Component.literal("Search…"));
        search.setResponder(q -> { recompute(); scroll = 0; });
        addRenderableWidget(search);

        int btnY = y1 - FOOT_H + 6;
        addRenderableWidget(Button.builder(Component.literal("Done"), b -> finish(true))
                .bounds(x1 - 150, btnY, 70, 18).build());
        addRenderableWidget(Button.builder(Component.literal("Cancel"), b -> finish(false))
                .bounds(x1 - 74, btnY, 70, 18).build());

        this.setInitialFocus(search);
    }

    private int x0, y0, x1, y1;

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

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        super.render(g, mx, my, pt);
        // window frame
        g.fill(x0 - 1, y0 - 1, x1 + 1, y1 + 1, 0xFF000000);
        g.fill(x0, y0, x1, y1, 0xFF2B2B2B);
        g.drawString(font, getTitle(), x0 + PAD, y0 - 12, 0xFFFFFFCC, false);

        int ly = y0 + PAD + 20;
        int lh = (y1 - FOOT_H) - ly;
        int vis = lh / ENTRY_H;
        g.fill(x0 + 1, ly, x1 - 1, ly + lh, 0xFF1A1A1A);

        int max = Math.max(0, filtered.size() - vis);
        scroll = Math.min(scroll, max);

        for (int i = scroll; i < Math.min(filtered.size(), scroll + vis); i++) {
            PickerEntry e = filtered.get(i);
            int iy = ly + (i - scroll) * ENTRY_H;
            boolean sel = selected.contains(e.id());
            boolean hover = mx >= x0 && mx <= x1 && my >= iy && my < iy + ENTRY_H;

            if (sel) g.fill(x0 + 1, iy, x1 - 1, iy + ENTRY_H, 0xFF3D4C5C);
            else if (hover) g.fill(x0 + 1, iy, x1 - 1, iy + ENTRY_H, 0xFF333333);

            String box = multi ? (sel ? "☑ " : "☐ ") : (sel ? "● " : "○ ");
            String line = box + e.display().getString();
            int boxColor = sel ? 0xFF9CDBFF : 0xFFAAAAAA;
            g.drawString(font, line, x0 + PAD, iy + (ENTRY_H - 8) / 2, boxColor, false);
            // id on the right, gray, clipped
            String idTxt = "§8" + e.id();
            int iw = font.width(idTxt);
            if (iw < 130) {
                g.drawString(font, idTxt, x1 - PAD - iw, iy + (ENTRY_H - 8) / 2,
                        0xFF777777, false);
            }
        }

        // footer info + scrollbar
        String info = (multi ? "Selected: " + selected.size() + "  ·  " : "")
                + filtered.size() + " / " + all.size();
        g.drawString(font, info, x0 + PAD, y1 - FOOT_H + 8, 0xFF888888, false);
        if (filtered.size() > vis && max > 0) {
            int barH = Math.max(10, lh * vis / filtered.size());
            int barY = ly + (lh - barH) * scroll / max;
            g.fill(x1 - 3, barY, x1 - 1, barY + barH, 0xFF777777);
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        int ly = y0 + PAD + 20;
        int lh = (y1 - FOOT_H) - ly;
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
    public boolean mouseScrolled(double mx, double my, double scrollX, double scrollY) {
        int ly = y0 + PAD + 20;
        int lh = (y1 - FOOT_H) - ly;
        if (mx >= x0 && mx <= x1 && my >= ly && my < ly + lh) {
            int vis = lh / ENTRY_H;
            int max = Math.max(0, filtered.size() - vis);
            scroll = (int) Math.max(0, Math.min(max, scroll - scrollY));
            return true;
        }
        return super.mouseScrolled(mx, my, scrollX, scrollY);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
