package top.theillusivec4.champions.common.client.screen.editor.pane;

import com.google.gson.JsonObject;
import top.theillusivec4.champions.common.client.screen.editor.EditorLang;
import top.theillusivec4.champions.common.client.screen.editor.json.JsonPathOps;
import top.theillusivec4.champions.common.client.screen.editor.widget.FormBuilder;

/** Tier tab — {@code champions/tier} files. */
public final class TierPane implements EditorPane {

    @Override
    public String newEntryPrefix() { return "champions:new_tier"; }

    @Override
    public void buildForm(FormBuilder fb, String selectedId) {
        JsonObject root = fb.root();
        fb.header(tr("header.tier"));
        fb.text(tr("label.level"), "level", "1");
        fb.header(tr("header.display"));
        fb.textRaw(tr("label.color"), "display.color", "#FFFFFF");
        fb.textRaw(tr("label.icon"), "display.icon", "champions:textures/gui/tier1.png");
        fb.hint(tr("hint.tier_display"), 1);
    }

    private static String tr(String key, Object... args) {
        return EditorLang.tr(key, args);
    }
}
