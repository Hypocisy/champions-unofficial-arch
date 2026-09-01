package top.theillusivec4.champions.common.client.screen.editor.widget;

import net.minecraft.client.gui.components.AbstractWidget;

import java.util.List;

/**
 * One visual row in the form panel. Headers render as section titles (optionally
 * with a small trailing button like "✕"); field rows have a label column and one
 * or more widgets (primary flexible-width widget + fixed-width trailing buttons).
 */
public final class Row {

    public static final int FIELD_H  = 18;
    public static final int HEADER_H = 16;
    public static final int GAP_H    = 4;

    public final String label;              // null for pure-widget rows
    public final boolean header;
    public final int indent;
    public final int height;
    public final List<AbstractWidget> widgets; // empty for headers
    public AbstractWidget trailingButton;      // header ✕ etc.

    /** Y position assigned during layout (render pass mirrors it). */
    public int y;

    private Row(String label, boolean header, int indent, int height,
                List<AbstractWidget> widgets, AbstractWidget trailingButton) {
        this.label = label;
        this.header = header;
        this.indent = indent;
        this.height = height;
        this.widgets = widgets;
        this.trailingButton = trailingButton;
    }

    public static Row header(String label, int indent, AbstractWidget trailingButton) {
        return new Row(label, true, indent, HEADER_H, List.of(), trailingButton);
    }

    public static Row field(String label, int indent, List<AbstractWidget> widgets) {
        return new Row(label, false, indent, FIELD_H, widgets, null);
    }

    public static Row gap(int px) {
        return new Row(null, false, 0, px, List.of(), null);
    }
}
