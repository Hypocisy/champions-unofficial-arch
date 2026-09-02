package top.theillusivec4.champions.common.client.screen.editor.pane;

import top.theillusivec4.champions.common.client.screen.editor.EditorLang;
import top.theillusivec4.champions.common.client.screen.editor.EditorSession;
import top.theillusivec4.champions.common.client.screen.editor.widget.FormBuilder;

/** Config tab — flat key/value fields (baked from the server config). */
public final class ConfigPane implements EditorPane {

    @Override
    public String newEntryPrefix() { return null; }

    @Override
    public void buildForm(FormBuilder fb, String selectedId) {
        fb.header(tr("header.server_config"));
        EditorSession session = fb.session();
        session.configValues.forEach((key, value) ->
                fb.direct(key, value, v -> {
                    session.configValues.put(key, v);
                    session.markDirty(key);
                }));
        fb.hint(tr("hint.config_reload"), 1);
    }

    private static String tr(String key, Object... args) {
        return EditorLang.tr(key, args);
    }
}
