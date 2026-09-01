package top.theillusivec4.champions.common.network;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import top.theillusivec4.champions.api.champion.ChampionTier;
import top.theillusivec4.champions.common.api.ChampionsRegistries;
import top.theillusivec4.champions.common.archetype.ChampionArchetype;
import top.theillusivec4.champions.common.data.ModifierSetting;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Serializable snapshot of all editor-relevant server data.
 *
 * <p>Sent server→client when the editor is opened; sent client→server on save
 * (only the changed JSON strings need to be present).</p>
 */
public record EditorPayload(
        /* tier id → pretty JSON */
        Map<String, String> tierJsons,
        /* archetype id → pretty JSON */
        Map<String, String> archetypeJsons,
        /* config key → string value */
        Map<String, String> configValues,
        /* modifier_setting id → pretty JSON */
        Map<String, String> modifierJsons,
        /* S2C only: ids from jar (built-in) packs, not file/ packs */
        Set<String> builtinIds,
        /* S2C only: datapack list for the Packs tab */
        List<PackInfo> packs
) {

    /** One datapack row in the Packs tab. */
    public record PackInfo(String id, String title, String source, boolean enabled) {}

    public static EditorPayload empty() {
        return new EditorPayload(Map.of(), Map.of(), Map.of(), Map.of(), Set.of(), List.of());
    }
    // ── StreamCodec ───────────────────────────────────────────────────────────

    private static final StreamCodec<FriendlyByteBuf, Map<String, String>> STRING_MAP_CODEC =
            StreamCodec.of(
                    (buf, map) -> {
                        buf.writeVarInt(map.size());
                        map.forEach((k, v) -> { buf.writeUtf(k); buf.writeUtf(v); });
                    },
                    buf -> {
                        int size = buf.readVarInt();
                        Map<String, String> map = new LinkedHashMap<>();
                        for (int i = 0; i < size; i++) map.put(buf.readUtf(), buf.readUtf());
                        return map;
                    }
            );

    private static final StreamCodec<FriendlyByteBuf, Set<String>> STRING_SET_CODEC =
            StreamCodec.of(
                    (buf, set) -> {
                        buf.writeVarInt(set.size());
                        set.forEach(buf::writeUtf);
                    },
                    buf -> {
                        int size = buf.readVarInt();
                        Set<String> set = new LinkedHashSet<>();
                        for (int i = 0; i < size; i++) set.add(buf.readUtf());
                        return set;
                    }
            );

    private static final StreamCodec<FriendlyByteBuf, List<PackInfo>> PACK_LIST_CODEC =
            StreamCodec.of(
                    (buf, list) -> {
                        buf.writeVarInt(list.size());
                        for (PackInfo p : list) {
                            buf.writeUtf(p.id());
                            buf.writeUtf(p.title());
                            buf.writeUtf(p.source());
                            buf.writeBoolean(p.enabled());
                        }
                    },
                    buf -> {
                        int size = buf.readVarInt();
                        List<PackInfo> list = new ArrayList<>();
                        for (int i = 0; i < size; i++) {
                            list.add(new PackInfo(buf.readUtf(), buf.readUtf(),
                                    buf.readUtf(), buf.readBoolean()));
                        }
                        return list;
                    }
            );

    public static final StreamCodec<FriendlyByteBuf, EditorPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buf, p) -> {
                        STRING_MAP_CODEC.encode(buf, p.tierJsons);
                        STRING_MAP_CODEC.encode(buf, p.archetypeJsons);
                        STRING_MAP_CODEC.encode(buf, p.configValues);
                        STRING_MAP_CODEC.encode(buf, p.modifierJsons);
                        STRING_SET_CODEC.encode(buf, p.builtinIds);
                        PACK_LIST_CODEC.encode(buf, p.packs);
                    },
                    buf -> new EditorPayload(
                            STRING_MAP_CODEC.decode(buf),
                            STRING_MAP_CODEC.decode(buf),
                            STRING_MAP_CODEC.decode(buf),
                            STRING_MAP_CODEC.decode(buf),
                            STRING_SET_CODEC.decode(buf),
                            PACK_LIST_CODEC.decode(buf)
                    )
            );

    // ── Factory: build from live server state ─────────────────────────────────

    public static EditorPayload fromServerState(net.minecraft.server.MinecraftServer server) {
        var gson = new GsonBuilder().setPrettyPrinting().create();

        // Tiers
        Map<String, String> tiers = new LinkedHashMap<>();
        ChampionsRegistries.tiers().getAll().forEach(tier -> {
            var json = serializeTier(tier);
            tiers.put(tier.id().toString(), gson.toJson(json));
        });

        // Archetypes
        Map<String, String> archetypes = new LinkedHashMap<>();
        ChampionsRegistries.archetypes().getAll().forEach(archetype -> {
            ChampionArchetype.CODEC.encodeStart(JsonOps.INSTANCE, archetype)
                    .resultOrPartial(e -> {})
                    .ifPresent(json -> archetypes.put(archetype.id().toString(), gson.toJson(json)));
        });

        // Modifiers
        Map<String, String> modifiers = new LinkedHashMap<>();
        ChampionsRegistries.modifiers().getLoadedData().forEach((id, setting) ->
                ModifierSetting.MAP_CODEC.codec().encodeStart(JsonOps.INSTANCE, setting)
                        .resultOrPartial(e -> {})
                        .ifPresent(json -> modifiers.put(id.toString(), gson.toJson(json))));

        // Built-in ids (from jar packs), merged across all three loaders
        Set<String> builtins = new LinkedHashSet<>();
        ChampionsRegistries.tiers().getBuiltinIds().forEach(id -> builtins.add(id.toString()));
        ChampionsRegistries.archetypes().getBuiltinIds().forEach(id -> builtins.add(id.toString()));
        ChampionsRegistries.modifiers().getBuiltinIds().forEach(id -> builtins.add(id.toString()));

        // Datapack list (Packs tab)
        List<PackInfo> packs = collectPacks(server);

        return new EditorPayload(tiers, archetypes, Map.of(), modifiers, builtins, packs);
    }

    private static List<PackInfo> collectPacks(net.minecraft.server.MinecraftServer server) {
        List<PackInfo> out = new ArrayList<>();
        if (server == null) return out;
        var repo = server.getPackRepository();
        Set<String> selected = Set.copyOf(repo.getSelectedIds());
        for (var pack : repo.getAvailablePacks()) {
            String id = pack.getId();
            // only world-folder packs — built-in packs from vanilla/mods are not
            // meaningfully toggleable from the editor and would flood the list
            if (!id.startsWith("file/")) continue;
            out.add(new PackInfo(
                    id,
                    pack.getTitle().getString(),
                    "world",
                    selected.contains(id)));
        }
        out.sort(java.util.Comparator.comparing(PackInfo::id));
        return out;
    }

    private static JsonObject serializeTier(ChampionTier tier) {
        JsonObject obj = new JsonObject();
        obj.addProperty("level", tier.level());
        JsonObject display = new JsonObject();
        display.addProperty("color", tier.display().color());
        display.addProperty("icon", tier.display().icon().toString());
        obj.add("display", display);
        return obj;
    }
}
