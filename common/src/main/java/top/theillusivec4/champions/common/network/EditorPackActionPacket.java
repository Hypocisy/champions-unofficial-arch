package top.theillusivec4.champions.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.champions.common.utils.Utils;

/**
 * C2S: pack-management actions from the editor's Packs tab.
 *
 * <ul>
 *   <li>{@code toggle} — enable/disable a datapack by id ({@link #packId()})</li>
 *   <li>{@code export} — write the attached editor payload as a datapack zip
 *       into {@code <world>/champions_exports/}</li>
 *   <li>{@code import} — copy zips from {@code <world>/champions_imports/} into
 *       {@code <world>/datapacks/}, enable them and reload</li>
 * </ul>
 */
public record EditorPackActionPacket(
        String action,
        String packId,
        EditorPayload payload
) {

    public static final String TOGGLE  = "toggle";
    public static final String EXPORT  = "export";
    public static final String IMPORT  = "import";

    public static final ResourceLocation ID = Utils.key("editor_pack_action");

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(action);
        buf.writeUtf(packId == null ? "" : packId);
        payload.encode(buf);
    }

    public static EditorPackActionPacket decode(FriendlyByteBuf buf) {
        String action = buf.readUtf();
        String packId = readId(buf);
        EditorPayload payload = EditorPayload.decode(buf);
        return new EditorPackActionPacket(action, packId, payload);
    }

    private static String readId(FriendlyByteBuf buf) {
        String s = buf.readUtf();
        return s.isEmpty() ? null : s;
    }

    // ── Factories ─────────────────────────────────────────────────────────────

    public static EditorPackActionPacket toggle(String packId) {
        return new EditorPackActionPacket(TOGGLE, packId, EditorPayload.empty());
    }

    public static EditorPackActionPacket export(EditorPayload payload) {
        return new EditorPackActionPacket(EXPORT, null, payload);
    }

    public static EditorPackActionPacket importPacks() {
        return new EditorPackActionPacket(IMPORT, null, EditorPayload.empty());
    }
}
