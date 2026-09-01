package top.theillusivec4.champions.common.client.screen.editor.pane;

import top.theillusivec4.champions.common.client.screen.ChampionEditorScreen;
import top.theillusivec4.champions.common.client.screen.editor.EditorSession;
import top.theillusivec4.champions.common.client.screen.editor.widget.FormBuilder;
import top.theillusivec4.champions.common.network.EditorPackActionPacket;
import top.theillusivec4.champions.common.network.EditorPayload;

/**
 * Packs tab — manage the world's datapacks from inside the editor:
 * enable/disable packs, export the current editor content as a datapack zip,
 * and import zips dropped into {@code <world>/champions_imports/}.
 */
public final class PacksPane implements EditorPane {

    @Override
    public String newEntryPrefix() { return null; }

    @Override
    public void buildForm(FormBuilder fb, String selectedId) {
        EditorSession session = fb.session();

        fb.header("Datapacks");
        fb.hint("click a pack in the list, then toggle it", 1);

        if (selectedId != null) {
            fb.header("Pack: " + selectedId, 1);
            boolean enabled = "enabled".equals(session.packsMap().get(selectedId));
            fb.cycle("state", "", enabled ? "enabled" : "disabled",
                    java.util.List.of(
                            new FormBuilder.CycleOption("enabled", "§aenabled"),
                            new FormBuilder.CycleOption("disabled", "§cdisabled")),
                    2, newValue -> {
                        // optimistic local update
                        if (session.packsSnapshot != null) {
                            session.packsSnapshot = session.packsSnapshot.stream()
                                    .map(p -> p.id().equals(selectedId)
                                            ? new EditorPayload.PackInfo(p.id(), p.title(),
                                            p.source(), newValue.equals("enabled"))
                                            : p)
                                    .toList();
                        }
                        ChampionEditorScreen.sendPackAction(
                                EditorPackActionPacket.toggle(selectedId));
                        fb.rebuild();
                    });
            fb.hint("toggling reloads server resources", 2);
        }

        fb.header("Export", 1);
        fb.action("Export editor content → champions_exports/", 1, () -> {
            ChampionEditorScreen.sendPackAction(
                    EditorPackActionPacket.export(fb.session().toPayload()));
        });
        fb.hint("writes champions_<timestamp>.zip into", 2);
        fb.hint("<world>/champions_exports/", 2);

        fb.header("Import", 1);
        fb.action("Import zips from champions_imports/", 1, () ->
                ChampionEditorScreen.sendPackAction(EditorPackActionPacket.importPacks()));
        fb.hint("drop datapack zips into", 2);
        fb.hint("<world>/champions_imports/ then click", 2);
        fb.hint("they are copied into datapacks/ + enabled", 2);
    }
}
