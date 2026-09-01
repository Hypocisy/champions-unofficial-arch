package top.theillusivec4.champions.common.client.screen.editor;

import net.minecraft.client.Minecraft;
import top.theillusivec4.champions.common.client.screen.ChampionEditorScreen;
import top.theillusivec4.champions.common.network.EditorPayload;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Holds all mutable editor state. Because modal screens (pickers) replace the
 * editor screen, state must live outside the screen instance so the editor can
 * be re-created identically when a picker closes.
 */
public final class EditorSession {

    public enum Tab { ARCHETYPES, TIERS, MODIFIERS, CONFIG, PACKS }

    // ── Canonical data (id → pretty JSON) ────────────────────────────────────
    public final Map<String, String> archetypeJsons;
    public final Map<String, String> tierJsons;
    public final Map<String, String> configValues;
    public final Map<String, String> modifierJsons;

    /** ids from jar packs (colored blue, delete = disabled override). */
    public final Set<String> builtinIds;
    /** ids modified this session (shown with * prefix). */
    public final Set<String> dirtyIds = new LinkedHashSet<>();

    /** Datapack list from the server (Packs tab). */
    public volatile List<EditorPayload.PackInfo> packsSnapshot = null;

    // ── UI state, survives picker round-trips ────────────────────────────────
    public Tab activeTab = Tab.ARCHETYPES;
    public String selectedId = null;
    public boolean rawMode = false;
    public int listScroll = 0;
    public int formScrollY = 0;

    public final Consumer<EditorPayload> saveAction;

    public EditorSession(EditorPayload payload, Consumer<EditorPayload> saveAction) {
        this.archetypeJsons = new LinkedHashMap<>(payload.archetypeJsons());
        this.tierJsons      = new LinkedHashMap<>(payload.tierJsons());
        this.configValues   = new LinkedHashMap<>(payload.configValues());
        this.modifierJsons  = new LinkedHashMap<>(payload.modifierJsons());
        this.builtinIds     = new LinkedHashSet<>(payload.builtinIds());
        this.packsSnapshot  = payload.packs();
        this.saveAction     = saveAction;
    }
    public Map<String, String> currentMap() {
        return switch (activeTab) {
            case ARCHETYPES -> archetypeJsons;
            case TIERS      -> tierJsons;
            case MODIFIERS  -> modifierJsons;
            case CONFIG     -> configValues;
            case PACKS      -> packsMap();
        };
    }

    /** Packs tab pseudo-map: packId → "enabled"/"disabled". */
    public Map<String, String> packsMap() {
        Map<String, String> out = new LinkedHashMap<>();
        if (packsSnapshot != null) {
            for (var p : packsSnapshot) out.put(p.id(), p.enabled() ? "enabled" : "disabled");
        }
        return out;
    }

    public void commit(String id, String json) {
        currentMap().put(id, json);
        dirtyIds.add(id);
    }

    public void markDirty(String id) {
        dirtyIds.add(id);
    }

    /** Re-open the editor screen (after a picker or pack-action refresh). */
    public void reopen() {
        Minecraft.getInstance().setScreen(new ChampionEditorScreen(this));
    }

    public EditorPayload toPayload() {
        return new EditorPayload(
                Map.copyOf(tierJsons),
                Map.copyOf(archetypeJsons),
                Map.copyOf(configValues),
                Map.copyOf(modifierJsons),
                Set.copyOf(builtinIds),
                packsSnapshot == null ? java.util.List.of() : packsSnapshot);
    }
}
