package top.theillusivec4.champions.common.client.screen.editor.pane;

import top.theillusivec4.champions.common.client.screen.ChampionEditorScreen;
import top.theillusivec4.champions.common.client.screen.editor.EditorLang;
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
        fb.header(tr("header.import_export"));
        fb.action(tr("action.export"), 1, () ->
                ChampionEditorScreen.sendPackAction(
                        EditorPackActionPacket.export(fb.session().toPayload())));
        fb.hint(tr("hint.export_target"), 1);
        fb.hint(tr("hint.exports_dir"), 1);
        fb.gap();
        fb.action(tr("action.import"), 1, () ->
                ChampionEditorScreen.sendPackAction(EditorPackActionPacket.importPacks()));
        fb.hint(tr("hint.import_dir"), 1);
        fb.hint(tr("hint.import_copied"), 1);

        // ── World datapacks ───────────────────────────────────────────────────
        fb.header(tr("header.world_datapacks"));
        int enabled = 0;
        if (session.packsSnapshot != null) {
            enabled = (int) session.packsSnapshot.stream().filter(EditorPayload.PackInfo::enabled).count();
        }
        fb.hint(session.packsSnapshot == null
                ? tr("hint.no_packs") : tr("hint.packs_enabled", enabled, session.packsSnapshot.size()), 1);

        if (selectedId != null) {
            fb.header(tr("header.pack", selectedId), 1);
            boolean isEnabled = "enabled".equals(session.packsMap().get(selectedId));
            fb.cycle(tr("label.state"), "", isEnabled ? "enabled" : "disabled",
                    List.of(
                            new FormBuilder.CycleOption("enabled", tr("pack.enabled")),
                            new FormBuilder.CycleOption("disabled", tr("pack.disabled"))),
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
            fb.hint(tr("hint.reload_on_toggle"), 2);
        } else {
            fb.hint(tr("hint.select_pack"), 1);
        }
    }

    private static String tr(String key, Object... args) {
        return EditorLang.tr(key, args);
    }
}
