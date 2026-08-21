package top.theillusivec4.champions.common.data;

import com.google.gson.JsonElement;
import net.minecraft.server.packs.resources.Resource;

/**
 * Shared helpers for the datapack reload listeners.
 *
 * <p>Two concerns are common to every loader:</p>
 * <ul>
 *   <li>{@link #isDisabled(JsonElement)} — the in-game editor writes a
 *       {@code {"disabled": true}} override to delete a built-in entry. Because
 *       {@code file/} packs win over jar packs, the loader sees the override and
 *       must skip the id entirely.</li>
 *   <li>{@link #isBuiltin(Resource)} — an entry is "built-in" when its backing
 *       resource comes from a jar pack rather than a {@code file/} pack. The
 *       editor colours these differently and treats delete as "write disabled
 *       override" instead of a raw file removal.</li>
 * </ul>
 */
public final class DataLoaders {

    private DataLoaders() {}

    /** Marker field written by the editor to soft-delete a built-in entry. */
    public static final String DISABLED_KEY = "disabled";

    /** True if the JSON root is an object carrying {@code "disabled": true}. */
    public static boolean isDisabled(JsonElement json) {
        return json != null
                && json.isJsonObject()
                && json.getAsJsonObject().has(DISABLED_KEY)
                && json.getAsJsonObject().get(DISABLED_KEY).getAsBoolean();
    }

    /**
     * True if the resource comes from a jar (built-in) pack.
     *
     * <p>World-save editor overrides live in a {@code file/} pack
     * ({@code file/champions_editor}); everything shipped inside the mod jar has a
     * source pack id that does not start with {@code file/}.</p>
     */
    public static boolean isBuiltin(Resource resource) {
        String source = resource.sourcePackId();
        return source == null || !source.startsWith("file/");
    }
}
