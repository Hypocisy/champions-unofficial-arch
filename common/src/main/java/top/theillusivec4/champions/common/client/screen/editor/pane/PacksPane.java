package top.theillusivec4.champions.common.client.screen.editor.pane;

import top.theillusivec4.champions.common.client.screen.ChampionEditorScreen;
import top.theillusivec4.champions.common.client.screen.editor.EditorSession;
import top.theillusivec4.champions.common.client.screen.editor.widget.FormBuilder;
import top.theillusivec4.champions.common.network.EditorPackActionPacket;
import top.theillusivec4.champions.common.network.EditorPayload;

import java.util.List;

/**
 * Packs tab — manage the world's champions-relevant datapacks from inside the
 * editor: enable/disable world packs, export the current editor content as a
 * datapack zip, and import zips dropped into {@code <world>/champions_imports/}.
 *
 * <p>Only {@code file/} (world folder) packs are listed — built-in packs from
 * vanilla/mods cannot be meaningfully toggled here.</p>
 */
public final class PacksPane implements EditorPane {

    @Override
    public String newEntryPrefix() { return null; }

    @Override
    public void buildForm(FormBuilder fb, String selectedId) {
        EditorSession session = fb.session();

        // ── Import / Export — always visible, first block ─────────────────────
        fb.header("Import / Export");
        fb.action("§bExport editor content → zip", 1, () ->
                ChampionEditorScreen.sendPackAction(
                        EditorPackActionPacket.export(fb.session().toPayload())));
        fb.hint("writes champions_<time>.zip into", 1);
        fb.hint("<world>/champions_exports/", 1);
        fb.gap();
        fb.action("§bImport zips from champions_imports/", 1, () ->
                ChampionEditorScreen.sendPackAction(EditorPackActionPacket.importPacks()));
        fb.hint("drop datapack zips into <world>/champions_imports/", 1);
        fb.hint("they are copied into datapacks/ and enabled", 1);

        // ── World datapacks ───────────────────────────────────────────────────
        fb.header("World Datapacks");
        int enabled = 0;
        if (session.packsSnapshot != null) {
            enabled = (int) session.packsSnapshot.stream().filter(EditorPayload.PackInfo::enabled).count();
        }
        fb.hint(session.packsSnapshot == null
                ? "no packs loaded" : enabled + " / " + session.packsSnapshot.size() + " enabled", 1);

        if (selectedId != null) {
            fb.header("Pack: " + selectedId, 1);
            boolean isEnabled = "enabled".equals(session.packsMap().get(selectedId));
            fb.cycle("state", "", isEnabled ? "enabled" : "disabled",
                    List.of(
                            new FormBuilder.CycleOption("enabled", "§a● enabled"),
                            new FormBuilder.CycleOption("disabled", "§c○ disabled")),
                    2, newValue -> {
                        // optimistic local update so the list flips instantly
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
        } else {
            fb.hint("select a pack in the list to enable/disable it", 1);
        }
    }
}
