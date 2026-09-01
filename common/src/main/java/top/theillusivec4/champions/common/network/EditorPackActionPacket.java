package top.theillusivec4.champions.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

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
) implements CustomPacketPayload {

    public static final String TOGGLE  = "toggle";
    public static final String EXPORT  = "export";
    public static final String IMPORT  = "import";

    public static final Type<EditorPackActionPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath("champions", "editor_pack_action"));

    public static final StreamCodec<FriendlyByteBuf, EditorPackActionPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, p) -> {
                        buf.writeUtf(p.action);
                        buf.writeUtf(p.packId == null ? "" : p.packId);
                        EditorPayload.STREAM_CODEC.encode(buf, p.payload);
                    },
                    buf -> new EditorPackActionPacket(
                            buf.readUtf(),
                            readId(buf),
                            EditorPayload.STREAM_CODEC.decode(buf))
            );

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

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }
}
