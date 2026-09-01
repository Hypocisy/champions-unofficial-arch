package top.theillusivec4.champions.common.editor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import top.theillusivec4.champions.common.network.EditorPackActionPacket;
import top.theillusivec4.champions.common.network.EditorPayload;
import top.theillusivec4.champions.common.network.PacketHandler;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Server-side editor IO:
 *
 * <ul>
 *   <li>{@link #handleSave} — writes tier/archetype/modifier JSON to the world's
 *       {@code datapacks/champions_editor/} pack and reloads resources</li>
 *   <li>{@link #handlePackAction} — datapack enable/disable, zip export and
 *       zip import for the editor's Packs tab</li>
 * </ul>
 */
public final class DatapackEditorHandler {

    private DatapackEditorHandler() {}

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /** Datapack folder name written inside the world save. */
    private static final String PACK_FOLDER = "champions_editor";

    private static final String PACK_MCMETA = """
            {
              "pack": {
                "pack_format": 48,
                "description": "Champions Unofficial — in-game editor datapack"
              }
            }
            """;

    // ── Save ───────────────────────────────────────────────────────────────────

    /**
     * Called on the server when a client saves from the editor.
     * Requires the player to have permission level 2.
     */
    public static void handleSave(SaveEditorRequest request, ServerPlayer player) {
        if (!player.hasPermissions(2)) return;
        MinecraftServer server = player.getServer();
        if (server == null) return;

        try {
            Path packRoot = worldDir(server).resolve("datapacks").resolve(PACK_FOLDER);
            ensurePackMeta(packRoot);
            writeFiles(packRoot.resolve("data"), request.payload());

            var repo = server.getPackRepository();
            repo.reload();
            Set<String> available = new HashSet<>(repo.getAvailableIds());
            Set<String> selected  = new LinkedHashSet<>(repo.getSelectedIds());
            String packId = "file/" + PACK_FOLDER;
            if (available.contains(packId)) selected.add(packId);

            reload(server, player, selected, "Datapack saved and reloaded.");
        } catch (IOException e) {
            player.sendSystemMessage(Component.literal(
                    "[Champions] Save failed: " + e.getMessage()));
        }
    }

    // ── Pack actions (Packs tab) ───────────────────────────────────────────────

    /** Called on the server for toggle / export / import actions. Permission level 2. */
    public static void handlePackAction(EditorPackActionPacket packet, ServerPlayer player) {
        if (!player.hasPermissions(2)) return;
        MinecraftServer server = player.getServer();
        if (server == null) return;

        switch (packet.action()) {
            case EditorPackActionPacket.TOGGLE -> togglePack(server, player, packet.packId());
            case EditorPackActionPacket.EXPORT -> exportZip(server, player, packet.payload());
            case EditorPackActionPacket.IMPORT -> importZips(server, player);
            default -> player.sendSystemMessage(Component.literal(
                    "[Champions] Unknown pack action: " + packet.action()));
        }
    }

    private static void togglePack(MinecraftServer server, ServerPlayer player, String packId) {
        if (packId == null) return;
        var repo = server.getPackRepository();
        repo.reload();
        if (!repo.getAvailableIds().contains(packId)) {
            player.sendSystemMessage(Component.literal(
                    "[Champions] Unknown pack: " + packId));
            return;
        }
        Set<String> selected = new LinkedHashSet<>(repo.getSelectedIds());
        boolean enabling = !selected.contains(packId);
        if (enabling) selected.add(packId); else selected.remove(packId);
        reload(server, player, selected,
                (enabling ? "Enabled " : "Disabled ") + packId);
    }

    private static void exportZip(MinecraftServer server, ServerPlayer player,
                                  EditorPayload payload) {
        try {
            Path exports = worldDir(server).resolve("champions_exports");
            Files.createDirectories(exports);
            String stamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            Path zip = exports.resolve("champions_" + stamp + ".zip");
            writeExportZip(zip, payload);
            player.sendSystemMessage(Component.literal(
                    "[Champions] Exported " + zip + " — drop it into "
                    + "champions_imports/ (or datapacks/) to reuse."));
        } catch (IOException e) {
            player.sendSystemMessage(Component.literal(
                    "[Champions] Export failed: " + e.getMessage()));
        }
    }

    private static void importZips(MinecraftServer server, ServerPlayer player) {
        try {
            Path world = worldDir(server);
            Path imports = world.resolve("champions_imports");
            Path datapacks = world.resolve("datapacks");
            Files.createDirectories(imports);
            Files.createDirectories(datapacks);

            List<Path> zips;
            try (Stream<Path> s = Files.list(imports)) {
                zips = s.filter(p -> p.getFileName().toString().toLowerCase()
                        .endsWith(".zip")).toList();
            }
            if (zips.isEmpty()) {
                player.sendSystemMessage(Component.literal(
                        "[Champions] No zips in " + imports
                        + " — drop datapack zips there and retry."));
                return;
            }

            Set<String> baseNames = new HashSet<>();
            for (Path z : zips) {
                String name = z.getFileName().toString();
                Files.copy(z, datapacks.resolve(name), StandardCopyOption.REPLACE_EXISTING);
                baseNames.add(stripZipExt(name));
            }

            var repo = server.getPackRepository();
            repo.reload();
            Set<String> selected = new LinkedHashSet<>(repo.getSelectedIds());
            for (String id : repo.getAvailableIds()) {
                if (id.startsWith("file/")) {
                    String tail = id.substring("file/".length());
                    if (baseNames.contains(stripZipExt(tail)) || baseNames.contains(tail)) {
                        selected.add(id);
                    }
                }
            }
            reload(server, player, selected,
                    "Imported " + zips.size() + " pack(s) and enabled them.");
        } catch (IOException e) {
            player.sendSystemMessage(Component.literal(
                    "[Champions] Import failed: " + e.getMessage()));
        }
    }

    // ── Reload + push fresh editor state ───────────────────────────────────────

    private static void reload(MinecraftServer server, ServerPlayer player,
                               Set<String> selected, String message) {
        server.reloadResources(selected).thenRun(() -> {
            server.getPlayerList().getPlayers().forEach(p ->
                    PacketHandler.Holder.get().syncTiersToPlayer(p));
            player.sendSystemMessage(Component.literal("[Champions] " + message));
            // refresh the editor's pack list in place (no screen replace)
            PacketHandler.Holder.get().sendEditorToPlayer(player);
        }).exceptionally(e -> {
            player.sendSystemMessage(Component.literal(
                    "[Champions] Reload failed: " + e.getMessage()));
            return null;
        });
    }

    // ── Zip writing ────────────────────────────────────────────────────────────

    private static void writeExportZip(Path zip, EditorPayload payload) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zip))) {

            zos.putNextEntry(new ZipEntry("pack.mcmeta"));
            zos.write(PACK_MCMETA.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();

            zipEntries(zos, "champions/tier",      payload.tierJsons());
            zipEntries(zos, "champions/archetype", payload.archetypeJsons());
            zipEntries(zos, "modifier_setting",    payload.modifierJsons());
        }
    }

    private static void zipEntries(ZipOutputStream zos, String subfolder,
                                   Map<String, String> entries) throws IOException {
        for (Map.Entry<String, String> e : entries.entrySet()) {
            String key = e.getKey();
            String json = e.getValue();
            try {
                JsonParser.parseString(json);
            } catch (JsonSyntaxException ex) {
                throw new IOException("Invalid JSON for '" + key + "': " + ex.getMessage());
            }
            int colon = key.indexOf(':');
            String namespace = colon > 0 ? key.substring(0, colon) : "champions";
            String idPath    = colon > 0 ? key.substring(colon + 1) : key;

            zos.putNextEntry(new ZipEntry("data/" + namespace + "/" + subfolder
                    + "/" + idPath + ".json"));
            zos.write(GSON.toJson(JsonParser.parseString(json)).getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }
    }

    // ── File IO helpers ────────────────────────────────────────────────────────

    private static Path worldDir(MinecraftServer server) {
        return server.getWorldPath(LevelResource.ROOT);
    }

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
            String key = e.getKey();
            String json = e.getValue();
            try {
                JsonParser.parseString(json);
            } catch (JsonSyntaxException ex) {
                throw new IOException("Invalid JSON for '" + key + "': " + ex.getMessage());
            }
            int colon = key.indexOf(':');
            String namespace = colon > 0 ? key.substring(0, colon) : "champions";
            String idPath    = colon > 0 ? key.substring(colon + 1) : key;

            Path dir = dataRoot.resolve(namespace).resolve(subfolder);
            Files.createDirectories(dir);
            try (Writer writer = Files.newBufferedWriter(dir.resolve(idPath + ".json"))) {
                writer.write(GSON.toJson(JsonParser.parseString(json)));
            }
        }
    }

    private static void ensurePackMeta(Path packRoot) throws IOException {
        Files.createDirectories(packRoot);
        Path meta = packRoot.resolve("pack.mcmeta");
        if (!Files.exists(meta)) {
            try (Writer w = Files.newBufferedWriter(meta)) {
                w.write(PACK_MCMETA);
            }
        }
    }

    private static String stripZipExt(String name) {
        return name.toLowerCase().endsWith(".zip")
                ? name.substring(0, name.length() - 4) : name;
    }

    // ── Request wrapper ────────────────────────────────────────────────────────

    public record SaveEditorRequest(EditorPayload payload) {}
}
