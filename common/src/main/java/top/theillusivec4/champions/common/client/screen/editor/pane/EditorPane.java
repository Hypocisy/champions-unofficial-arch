package top.theillusivec4.champions.common.client.screen.editor.pane;

import top.theillusivec4.champions.common.client.screen.editor.widget.FormBuilder;

/**
 * One editor tab's form definition. Panes are stateless — all state lives in the
 * {@link top.theillusivec4.champions.common.client.screen.editor.EditorSession}.
 */
public interface EditorPane {

    /** Build the form rows for the currently selected entry. */
    void buildForm(FormBuilder fb, String selectedId);

    /**
     * Id prefix used by the "New" button; {@code null} disables creation
     * (config / packs tabs).
     */
    default String newEntryPrefix() { return null; }
}
