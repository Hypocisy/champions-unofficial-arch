package top.theillusivec4.champions.common.editor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import top.theillusivec4.champions.common.network.EditorPayload;
import top.theillusivec4.champions.common.network.PacketHandler;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Handles editor save requests from the client.
 *
 * <p>Writes tier/archetype JSON to the world's custom datapack folder
 * ({@code saves/<world>/datapacks/champions_editor/}) then triggers
 * {@code /minecraft:reload} equivalent server-side so changes take effect immediately.</p>
 */
public final class DatapackEditorHandler {

    private DatapackEditorHandler() {}

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /** Datapack folder name written inside the world save. */
    private static final String PACK_FOLDER = "champions_editor";

    /**
     * Called on the server when a client saves from the editor.
     * Requires the player to have permission level 2.
     */
    public static void handleSave(SaveEditorRequest request, ServerPlayer player) {
        if (!player.hasPermissions(2)) return;
        MinecraftServer server = player.getServer();
        if (server == null) return;

        try {
            Path packRoot = server.getWorldPath(LevelResource.ROOT)
                    .resolve("datapacks")
                    .resolve(PACK_FOLDER);

            ensurePackMeta(packRoot);
            writeFiles(packRoot.resolve("data"), request.payload());

            // Rescan the pack repository so the newly written pack is discovered,
            // then add it to the enabled set before reloading resources.
            var repo = server.getPackRepository();
            repo.reload();
            Set<String> available = new HashSet<>(repo.getAvailableIds());
            Set<String> selected  = new HashSet<>(repo.getSelectedIds());
            String packId = "file/" + PACK_FOLDER;
            if (available.contains(packId)) {
                selected.add(packId);
            }

            server.reloadResources(selected).thenRun(() -> {
                // After reload, push updated tier list to every online player
                server.getPlayerList().getPlayers().forEach(p ->
                        PacketHandler.Holder.get().syncTiersToPlayer(p));
                player.sendSystemMessage(Component.literal(
                        "[Champions] Datapack saved and reloaded."));
            }).exceptionally(e -> {
                player.sendSystemMessage(Component.literal(
                        "[Champions] Reload failed: " + e.getMessage()));
                return null;
            });

        } catch (IOException e) {
            player.sendSystemMessage(Component.literal(
                    "[Champions] Save failed: " + e.getMessage()));
        }
    }

    // ── IO helpers ────────────────────────────────────────────────────────────

    private static void writeFiles(Path dataRoot, EditorPayload payload) throws IOException {
        writeDirContents(dataRoot, "champions/tier",      payload.tierJsons());
        writeDirContents(dataRoot, "champions/archetype", payload.archetypeJsons());
        writeDirContents(dataRoot, "modifier_setting",    payload.modifierJsons());
    }

    /**
     * Writes each entry as {@code <dataRoot>/<path>/<idPath>.json}.
     * The map key is a full ResourceLocation string (e.g. {@code "champions:tier_1"}).
     */
    private static void writeDirContents(Path dataRoot, String subfolder,
                                         Map<String, String> entries) throws IOException {
        for (Map.Entry<String, String> e : entries.entrySet()) {
            String key = e.getKey(); // e.g. "champions:tier_1"
            String json = e.getValue();

            // Validate it's parseable JSON before writing
            try {
                JsonParser.parseString(json);
            } catch (JsonSyntaxException ex) {
                throw new IOException("Invalid JSON for '" + key + "': " + ex.getMessage());
            }

            int colon = key.indexOf(':');
            String namespace = colon > 0 ? key.substring(0, colon) : "champions";
            String idPath    = colon > 0 ? key.substring(colon + 1) : key;

            Path dir  = dataRoot.resolve(namespace).resolve(subfolder);
            Files.createDirectories(dir);
            Path file = dir.resolve(idPath + ".json");

            try (Writer writer = Files.newBufferedWriter(file)) {
                // Pretty-print the JSON
                writer.write(GSON.toJson(JsonParser.parseString(json)));
            }
        }
    }

    private static void ensurePackMeta(Path packRoot) throws IOException {
        Files.createDirectories(packRoot);
        Path meta = packRoot.resolve("pack.mcmeta");
        if (!Files.exists(meta)) {
            try (Writer w = Files.newBufferedWriter(meta)) {
                w.write("""
                        {
                          "pack": {
                            "pack_format": 48,
                            "description": "Champions Unofficial — in-game editor datapack"
                          }
                        }
                        """);
            }
        }
    }

    // ── Request wrapper ───────────────────────────────────────────────────────

    public record SaveEditorRequest(EditorPayload payload) {}
}
