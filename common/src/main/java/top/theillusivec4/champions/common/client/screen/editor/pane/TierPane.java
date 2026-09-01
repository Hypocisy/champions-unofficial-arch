package top.theillusivec4.champions.common.client.screen.editor.pane;

import com.google.gson.JsonObject;
import top.theillusivec4.champions.common.client.screen.editor.json.JsonPathOps;
import top.theillusivec4.champions.common.client.screen.editor.widget.FormBuilder;

/** Tier tab — {@code champions/tier} files. */
public final class TierPane implements EditorPane {

    @Override
    public String newEntryPrefix() { return "champions:new_tier"; }

    @Override
    public void buildForm(FormBuilder fb, String selectedId) {
        JsonObject root = fb.root();
        fb.header("Tier");
        fb.text("level", "level", "1");
        fb.header("Display");
        fb.textRaw("color", "display.color", "#FFFFFF");
        fb.textRaw("icon", "display.icon", "champions:textures/gui/tier1.png");
        fb.hint("color: hex string · icon: texture path", 1);
    }
}
