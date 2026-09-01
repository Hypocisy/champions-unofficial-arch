package top.theillusivec4.champions.common.client.screen.editor.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.ArrayList;
import java.util.List;

/**
 * Robust path-based access into a live {@link JsonObject} tree used by the editor form.
 *
 * <p>Paths are dot-separated segments. Numeric segments index into {@link JsonArray}s
 * (e.g. {@code affix_pools.0.candidates.2.weight}); all other segments are object members.
 * Intermediate nodes are created on write.</p>
 */
public final class JsonPathOps {

    private JsonPathOps() {}

    public static String[] segs(String path) {
        return path.split("\\.");
    }

    public static String child(String base, String child) {
        return base.isEmpty() ? child : base + "." + child;
    }

    public static String index(String base, int idx) {
        return child(base, String.valueOf(idx));
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    public static JsonElement get(JsonObject root, String path) {
        JsonElement cur = root;
        for (String s : segs(path)) {
            if (cur == null || !cur.isJsonObject()) return null;
            cur = cur.getAsJsonObject().get(s);
        }
        return cur;
    }

    public static String str(JsonObject root, String path, String def) {
        JsonElement e = get(root, path);
        if (e == null || e.isJsonNull()) return def;
        if (e.isJsonPrimitive()) return e.getAsString();
        return def;
    }

    public static JsonObject obj(JsonObject root, String path) {
        JsonElement e = get(root, path);
        return (e != null && e.isJsonObject()) ? e.getAsJsonObject() : null;
    }

    public static JsonArray array(JsonObject root, String path) {
        JsonElement e = get(root, path);
        return (e != null && e.isJsonArray()) ? e.getAsJsonArray() : null;
    }

    /** Objects in the array at {@code path} (malformed entries skipped). */
    public static List<JsonObject> objectsIn(JsonObject root, String path) {
        List<JsonObject> out = new ArrayList<>();
        JsonArray arr = array(root, path);
        if (arr != null) {
            for (JsonElement e : arr) {
                if (e.isJsonObject()) out.add(e.getAsJsonObject());
            }
        }
        return out;
    }

    public static List<String> stringsIn(JsonObject root, String path) {
        List<String> out = new ArrayList<>();
        JsonArray arr = array(root, path);
        if (arr != null) {
            for (JsonElement e : arr) {
                if (e.isJsonPrimitive()) out.add(e.getAsString());
            }
        }
        return out;
    }

    public static int size(JsonObject root, String path) {
        JsonArray arr = array(root, path);
        return arr == null ? 0 : arr.size();
    }

    // ── Write ─────────────────────────────────────────────────────────────────

    /** Writes {@code value} at {@code path}, creating intermediate objects as needed. */
    public static void set(JsonObject root, String path, JsonElement value) {
        String[] p = segs(path);
        JsonObject o = root;
        for (int i = 0; i < p.length - 1; i++) {
            JsonElement next = o.get(p[i]);
            if (next == null || !next.isJsonObject()) {
                next = new JsonObject();
                o.add(p[i], next);
            }
            o = next.getAsJsonObject();
        }
        o.add(p[p.length - 1], value);
    }

    /** Writes a plain string value (never number-coerced). */
    public static void setString(JsonObject root, String path, String value) {
        set(root, path, new JsonPrimitive(value));
    }

    /** Writes a string array at {@code path}. */
    public static void setStringArray(JsonObject root, String path, List<String> values) {
        JsonArray arr = new JsonArray();
        values.forEach(arr::add);
        set(root, path, arr);
    }

    /**
     * Smart write: numbers as int/double, "true"/"false" as boolean, everything else string.
     * Matches how the JSON values are consumed by the codecs.
     */
    public static void setSmart(JsonObject root, String path, String raw) {
        String t = raw.trim();
        JsonElement v;
        try {
            v = new JsonPrimitive(Integer.parseInt(t));
        } catch (NumberFormatException e1) {
            try {
                v = new JsonPrimitive(Double.parseDouble(t));
            } catch (NumberFormatException e2) {
                if ("true".equalsIgnoreCase(t) || "false".equalsIgnoreCase(t)) {
                    v = new JsonPrimitive(Boolean.parseBoolean(t));
                } else {
                    v = new JsonPrimitive(raw);
                }
            }
        }
        set(root, path, v);
    }

    /** Appends {@code el} to the array at {@code path} (created if absent). */
    public static void append(JsonObject root, String path, JsonElement el) {
        String[] p = segs(path);
        JsonObject parent = root;
        for (int i = 0; i < p.length - 1; i++) {
            JsonElement next = parent.get(p[i]);
            if (next == null || !next.isJsonObject()) {
                next = new JsonObject();
                parent.add(p[i], next);
            }
            parent = next.getAsJsonObject();
        }
        String last = p[p.length - 1];
        JsonElement existing = parent.get(last);
        JsonArray arr = (existing != null && existing.isJsonArray())
                ? existing.getAsJsonArray() : new JsonArray();
        arr.add(el);
        parent.add(last, arr);
    }

    /**
     * Removes the node at {@code path}. Numeric final segment removes an array element
     * (shifting the rest); otherwise the object member is removed.
     */
    public static void remove(JsonObject root, String path) {
        String[] p = segs(path);
        JsonObject parent = root;
        for (int i = 0; i < p.length - 1; i++) {
            JsonElement next = parent.get(p[i]);
            if (next == null || !next.isJsonObject()) return;
            parent = next.getAsJsonObject();
        }
        String last = p[p.length - 1];
        JsonElement target = parent.get(last);
        if (target == null) return;
        // numeric segment → array element removal
        try {
            int idx = Integer.parseInt(last);
            JsonArray arr = parent.getAsJsonArray(last);
            if (arr != null) {
                arr.remove(idx);
                return;
            }
        } catch (NumberFormatException | IllegalStateException ignored) {}
        parent.remove(last);
    }

    /** Pads array with nulls helper (used when writing sparse indices). */
    public static void padArray(JsonArray arr, int upTo) {
        while (arr.size() <= upTo) arr.add(JsonNull.INSTANCE);
    }
}
